package tyrian

import cats.effect.IO
import cats.effect.kernel.Async
import cats.effect.kernel.Fiber
import cats.kernel.Eq
import cats.kernel.Monoid
import fs2.Stream
import org.scalajs.dom
import org.scalajs.dom.EventTarget
import util.Functions

import java.util.concurrent.TimeUnit
import scala.annotation.nowarn
import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js

// TODO: I think these should delegate to Sub a lot more, rather than duplicating logic.

/** A subscription describes a resource that an application is interested in.
  *
  * Examples:
  *
  *   - a timeout notifies its subscribers when it expires,
  *   - a video being played notifies its subscribers with subtitles.
  */
sealed trait Watch:

  /** Transforms the type of messages produced by the subscription */
  def map(f: GlobalMsg => GlobalMsg): Watch

  def toSub: Sub[IO, GlobalMsg]

  /** Infix operation for combining two Subs into one. */
  def combine(other: Watch): Watch =
    Watch.merge(this, other)

  /** Infix operator for combining two Subs into one. */
  def |+|(other: Watch): Watch =
    Watch.merge(this, other)

object Watch:

  given CanEqual[Option[?], Option[?]] = CanEqual.derived
  given CanEqual[Watch, Watch]         = CanEqual.derived

  final def merge(a: Watch, b: Watch): Watch =
    (a, b) match {
      case (Watch.None, Watch.None) => Watch.None
      case (Watch.None, s2)         => s2
      case (s1, Watch.None)         => s1
      case (s1, s2)                 => Watch.Combine(s1, s2)
    }

  /** The empty subscription represents the absence of subscriptions */
  case object None extends Watch:
    def map(f: GlobalMsg => GlobalMsg): None.type = this

    def toSub: Sub[IO, GlobalMsg] =
      Sub.None

  /** A subscription that forwards the notifications produced by the given `observable`
    * @param id
    *   Globally unique identifier for this subscription
    * @param observable
    *   Observable and cancellable/closable effect that produces notifications. Encoded as a callback with an effect
    *   describing how to optionally close the subscription.
    * @param toMsg
    *   a function that turns every notification value into a possible message
    * @tparam IO
    *   type of the effect monad, must be a Cats Effect 3 Concurrent.
    * @tparam A
    *   type of notification values produced by the observable
    * @tparam GlobalMsg
    *   type of message produced by the subscription
    */
  final case class Observe[A](
      id: String,
      observable: IO[(Either[Throwable, A] => Unit) => IO[Option[IO[Unit]]]],
      toMsg: A => Option[GlobalMsg]
  ) extends Watch:
    def map(f: GlobalMsg => GlobalMsg): Watch =
      Observe(
        id,
        observable,
        toMsg.andThen(_.map(f))
      )

    def toSub: Sub[IO, GlobalMsg] =
      Sub.Observe[IO, A, GlobalMsg](id, observable, toMsg)

  object Observe:

    /** Construct a cancelable observable sub by describing how to acquire and release the resource, and optionally
      * produce a message
      */
    def apply[A, R](
        id: String,
        acquire: (Either[Throwable, A] => Unit) => IO[R],
        release: R => IO[Unit],
        toMsg: A => Option[GlobalMsg]
    ): Watch =
      val task = IO.delay {
        def cancel(res: R) = Option(release(res))
        (cb: Either[Throwable, A] => Unit) => acquire(cb).map(cancel)
      }
      Observe[A](id, task, toMsg)

    /** Construct a cancelable observable sub of a value */
    // def apply[A](id: String, observable: IO[(Either[Throwable, A] => Unit) => IO[Option[IO[Unit]]]]): Watch =
    //   Observe(id, observable, Option.apply)

  /** Make a cancelable subscription that produces an optional message */
  def make[A, R](id: String)(acquire: (Either[Throwable, A] => Unit) => IO[R])(
      release: R => IO[Unit]
  )(toMsg: A => Option[GlobalMsg]): Watch =
    val task = IO.delay {
      def cancel(res: R) = Option(release(res))
      (cb: Either[Throwable, A] => Unit) => acquire(cb).map(cancel)
    }
    Observe[A](id, task, toMsg)

  /** Make a cancelable subscription that returns a value (to be mapped into a GlobalMsg) */
  // def make[A, R](id: String)(acquire: (Either[Throwable, A] => Unit) => IO[R])(
  //     release: R => IO[Unit]
  // ): Watch =
  //   make[A, R](id)(acquire)(release)(Option.apply)

  /** Make a subscription based on an fs2.Stream. The stream is cancelled when it it removed from the list of
    * subscriptions.
    */
  def make[A](id: String, stream: Stream[IO, A])(
      toMsg: A => Option[GlobalMsg]
  ): Watch =
    make[A, Fiber[IO, Throwable, Unit]](id) { cb =>
      Async[IO].start(stream.attempt.foreach(result => IO.delay(cb(result))).compile.drain)
    }(_.cancel)(toMsg)

  /** Make a subscription based on an fs2.Stream with additional custom clean up. The stream itself is always cancelled
    * when it it removed from the list of subscriptions, even if no particular clean up is defined.
    */
  def make[A](id: String)(stream: Stream[IO, A])(cleanUp: IO[Unit])(
      toMsg: A => Option[GlobalMsg]
  ): Watch =
    make[A, Fiber[IO, Throwable, Unit]](id) { cb =>
      Async[IO].start(stream.attempt.foreach(result => IO.delay(cb(result))).compile.drain)
    }(_.cancel.flatMap(_ => cleanUp))(toMsg)

  private def _forget: Unit => IO[Option[IO[Unit]]] =
    (_: Unit) => IO.delay(Option(IO.delay(())))

  /** Make an uncancelable subscription that produces am optional message */
  def forever[A](acquire: (Either[Throwable, A] => Unit) => Unit)(
      toMsg: A => Option[GlobalMsg]
  ): Watch =
    val task =
      IO.delay(acquire andThen _forget)

    Observe[A]("<none>", task, toMsg)

  /** Merge two subscriptions into a single one */
  final case class Combine(sub1: Watch, sub2: Watch) extends Watch:
    def map(f: GlobalMsg => GlobalMsg): Watch = Combine(sub1.map(f), sub2.map(f))
    def toBatch: Watch.Batch                  = Watch.Batch(List(sub1, sub2))

    def toSub: Sub[IO, GlobalMsg] =
      Sub.Combine(sub1.toSub, sub2.toSub)

  /** Treat many subscriptions as one */
  final case class Batch(subs: List[Watch]) extends Watch:
    def map(f: GlobalMsg => GlobalMsg): Batch = this.copy(subs = subs.map(_.map(f)))
    def ++(other: Batch): Batch               = Batch(subs ++ other.subs)
    def ::(sub: Watch): Batch                 = Batch(sub :: subs)
    def +:(sub: Watch): Batch                 = Batch(sub +: subs)
    def :+(sub: Watch): Batch                 = Batch(subs :+ sub)

    def toSub: Sub[IO, GlobalMsg] =
      Sub.Batch(subs.map(_.toSub))

  object Batch:
    def apply(subs: Watch*): Batch =
      Batch(subs.toList)

  /** A subscription that emits a msg once. Identical to timeout with a duration of 0. */
  def emit(msg: GlobalMsg): Watch =
    timeout(FiniteDuration(0, TimeUnit.MILLISECONDS), msg, msg.toString)

  /** A subscription that produces a `msg` after a `duration`. */
  def timeout(duration: FiniteDuration, msg: GlobalMsg, id: String): Watch =
    def task(callback: Either[Throwable, GlobalMsg] => Unit): IO[Option[IO[Unit]]] =
      val handle = dom.window.setTimeout(
        Functions.fun0(() => callback(Right(msg))),
        duration.toMillis.toDouble
      )
      IO.delay {
        Option(IO.delay(dom.window.clearTimeout(handle)))
      }

    Observe(id, IO.pure(task), Option.apply)

  /** A subscription that produces a `msg` after a `duration`. */
  def timeout(duration: FiniteDuration, msg: GlobalMsg): Watch =
    timeout(duration, msg, "[tyrian-sub-every] " + duration.toString + msg.toString)

  /** A subscription that repeatedly produces a `msg` based on an `interval`. */
  def every(interval: FiniteDuration, id: String, toMsg: js.Date => GlobalMsg): Watch =
    Watch.make[js.Date, Int](id) { callback =>
      IO.delay {
        dom.window.setInterval(
          Functions.fun0(() => callback(Right(new js.Date()))),
          interval.toMillis.toDouble
        )
      }
    } { handle =>
      IO.delay(dom.window.clearTimeout(handle))
    }(d => Option(toMsg(d)))

  /** A subscription that repeatedly produces a `msg` based on an `interval`. */
  def every(interval: FiniteDuration, toMsg: js.Date => GlobalMsg): Watch =
    every(interval, "[tyrian-sub-every] " + interval.toString, toMsg)

  /** A subscription that emits a `msg` based on an a JavaScript event. */
  def fromEvent[A](name: String, target: EventTarget)(extract: A => Option[GlobalMsg]): Watch =
    Watch.make[A, js.Function1[A, Unit]](name + target.hashCode) { callback =>
      IO.delay {
        val listener = Functions.fun { (a: A) =>
          callback(Right(a))
        }
        target.addEventListener(name, listener)
        listener
      }
    } { listener =>
      IO.delay(target.removeEventListener(name, listener))
    }(extract)

  /** A subscription that emits a `msg` based on the running time in seconds whenever the browser renders an animation
    * frame.
    */
  @nowarn("msg=unused")
  def animationFrameTick(id: String)(toMsg: Double => GlobalMsg): Watch =
    val stream =
      Stream.repeatEval {
        IO.async_ { (cb: Either[Throwable, Double] => Unit) =>
          dom.window.requestAnimationFrame { t =>
            cb(Right(t / 1000))
          }
          ()
        }
      }

    Watch.make(id, stream)(t => Option(toMsg(t)))

  def combineAll(list: List[Watch]): Watch =
    Monoid[Watch].combineAll(list)

  // Cats' typeclass instances

  given Monoid[Watch] with
    def empty: Watch                       = Watch.None
    def combine(a: Watch, b: Watch): Watch = Watch.merge(a, b)

  given (using subEq: Eq[Sub[IO, GlobalMsg]]): Eq[Watch] with
    def eqv(x: Watch, y: Watch): Boolean =
      subEq.eqv(x.toSub, y.toSub)

  def fromSub(sub: Sub[IO, GlobalMsg]): Watch =
    sub match
      case Sub.None =>
        Watch.None

      case Sub.Observe(id, observable, toMsg) =>
        Watch.Observe(id, observable, toMsg)

      case Sub.Combine(x, y) =>
        Watch.Combine(fromSub(x), fromSub(y))

      case Sub.Batch(subs) =>
        Watch.Batch(subs.map(fromSub))
