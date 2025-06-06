package tyrian

import cats.Functor
import cats.effect.kernel.Async
import cats.effect.kernel.Fiber
import cats.effect.kernel.Sync
import cats.kernel.Eq
import cats.kernel.Monoid
import cats.syntax.all.*
import fs2.Stream
import org.scalajs.dom
import org.scalajs.dom.EventTarget
import tyrian.runtime.SubHelper
import util.Functions

import java.util.concurrent.TimeUnit
import scala.annotation.nowarn
import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js

/** A subscription describes a resource that an application is interested in.
  *
  * Examples:
  *
  *   - a timeout notifies its subscribers when it expires,
  *   - a video being played notifies its subscribers with subtitles.
  *
  * @tparam Msg
  *   Type of message produced by the resource
  */
sealed trait Sub[+F[_], +Msg]:

  /** Transforms the type of messages produced by the subscription */
  def map[OtherMsg](f: Msg => OtherMsg): Sub[F, OtherMsg]

  /** Infix operation for combining two Subs into one. */
  def combine[F2[x] >: F[x], LubMsg >: Msg](other: Sub[F2, LubMsg]): Sub[F2, LubMsg] =
    Sub.merge(this, other)

  /** Infix operator for combining two Subs into one. */
  def |+|[F2[x] >: F[x], LubMsg >: Msg](other: Sub[F2, LubMsg]): Sub[F2, LubMsg] =
    Sub.merge(this, other)

object Sub:

  given CanEqual[Option[?], Option[?]] = CanEqual.derived
  given CanEqual[Sub[?, ?], Sub[?, ?]] = CanEqual.derived

  final def merge[F[_], Msg, LubMsg >: Msg](a: Sub[F, Msg], b: Sub[F, LubMsg]): Sub[F, LubMsg] =
    (a, b) match {
      case (Sub.None, Sub.None) => Sub.None
      case (Sub.None, s2)       => s2
      case (s1, Sub.None)       => s1
      case (s1, s2)             => Sub.Combine(s1, s2)
    }

  /** The empty subscription represents the absence of subscriptions */
  case object None extends Sub[Nothing, Nothing]:
    def map[OtherMsg](f: Nothing => OtherMsg): None.type = this

  /** A subscription that forwards the notifications produced by the given `observable`
    * @param id
    *   Globally unique identifier for this subscription
    * @param observable
    *   Observable and cancellable/closable effect that produces notifications. Encoded as a callback with an effect
    *   describing how to optionally close the subscription.
    * @param toMsg
    *   a function that turns every notification value into a possible message
    * @tparam F
    *   type of the effect monad, must be a Cats Effect 3 Concurrent.
    * @tparam A
    *   type of notification values produced by the observable
    * @tparam Msg
    *   type of message produced by the subscription
    */
  final case class Observe[F[_], A, Msg](
      id: String,
      observable: F[(Either[Throwable, A] => Unit) => F[Option[F[Unit]]]],
      toMsg: A => Option[Msg]
  ) extends Sub[F, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Sub[F, OtherMsg] =
      Observe(
        id,
        observable,
        toMsg.andThen(_.map(f))
      )

  object Observe:

    /** Construct a cancelable observable sub by describing how to acquire and release the resource, and optionally
      * produce a message
      */
    def apply[F[_]: Sync, A, Msg, R](
        id: String,
        acquire: (Either[Throwable, A] => Unit) => F[R],
        release: R => F[Unit],
        toMsg: A => Option[Msg]
    ): Sub[F, Msg] =
      val task = Sync[F].delay {
        def cancel(res: R) = Option(release(res))
        (cb: Either[Throwable, A] => Unit) => acquire(cb).map(cancel)
      }
      Observe[F, A, Msg](id, task, toMsg)

    /** Construct a cancelable observable sub of a value */
    def apply[F[_], A](id: String, observable: F[(Either[Throwable, A] => Unit) => F[Option[F[Unit]]]]): Sub[F, A] =
      Observe(id, observable, Option.apply)

  /** Make a cancelable subscription that produces an optional message */
  def make[F[_]: Sync, A, Msg, R](id: String)(acquire: (Either[Throwable, A] => Unit) => F[R])(
      release: R => F[Unit]
  )(toMsg: A => Option[Msg]): Sub[F, Msg] =
    val task = Sync[F].delay {
      def cancel(res: R) = Option(release(res))
      (cb: Either[Throwable, A] => Unit) => acquire(cb).map(cancel)
    }
    Observe[F, A, Msg](id, task, toMsg)

  /** Make a cancelable subscription that returns a value (to be mapped into a Msg) */
  def make[F[_]: Sync, A, R](id: String)(acquire: (Either[Throwable, A] => Unit) => F[R])(
      release: R => F[Unit]
  ): Sub[F, A] =
    make[F, A, A, R](id)(acquire)(release)(Option.apply)

  /** Make a subscription based on an fs2.Stream. The stream is cancelled when it it removed from the list of
    * subscriptions.
    */
  def make[F[_]: Async, A](id: String, stream: Stream[F, A]): Sub[F, A] =
    make[F, A, Fiber[F, Throwable, Unit]](id) { cb =>
      Async[F].start(stream.attempt.foreach(result => Async[F].delay(cb(result))).compile.drain)
    }(_.cancel)

  /** Make a subscription based on an fs2.Stream with additional custom clean up. The stream itself is always cancelled
    * when it it removed from the list of subscriptions, even if no particular clean up is defined.
    */
  def make[F[_]: Async, A](id: String)(stream: Stream[F, A])(cleanUp: F[Unit]): Sub[F, A] =
    make[F, A, Fiber[F, Throwable, Unit]](id) { cb =>
      Async[F].start(stream.attempt.foreach(result => Async[F].delay(cb(result))).compile.drain)
    }(_.cancel.flatMap(_ => cleanUp))

  private def _forget[F[_]: Sync]: Unit => F[Option[F[Unit]]] =
    (_: Unit) => Sync[F].delay(Option(Sync[F].delay(())))

  /** Make an uncancelable subscription that produces am optional message */
  def forever[F[_]: Sync, A, Msg](acquire: (Either[Throwable, A] => Unit) => Unit)(
      toMsg: A => Option[Msg]
  ): Sub[F, Msg] =
    val task =
      Sync[F].delay(acquire andThen _forget)

    Observe[F, A, Msg]("<none>", task, toMsg)

  /** Merge two subscriptions into a single one */
  final case class Combine[F[_], Msg](sub1: Sub[F, Msg], sub2: Sub[F, Msg]) extends Sub[F, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Sub[F, OtherMsg] = Combine(sub1.map(f), sub2.map(f))
    def toBatch: Sub.Batch[F, Msg]                          = Sub.Batch(List(sub1, sub2))

  /** Treat many subscriptions as one */
  final case class Batch[F[_], Msg](subs: List[Sub[F, Msg]]) extends Sub[F, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Batch[F, OtherMsg] = this.copy(subs = subs.map(_.map(f)))
    def ++(other: Batch[F, Msg]): Batch[F, Msg]               = Batch(subs ++ other.subs)
    def ::(sub: Sub[F, Msg]): Batch[F, Msg]                   = Batch(sub :: subs)
    def +:(sub: Sub[F, Msg]): Batch[F, Msg]                   = Batch(sub +: subs)
    def :+(sub: Sub[F, Msg]): Batch[F, Msg]                   = Batch(subs :+ sub)

  object Batch:
    def apply[F[_], Msg](subs: Sub[F, Msg]*): Batch[F, Msg] =
      Batch(subs.toList)

  /** A subscription that emits a msg once. Identical to timeout with a duration of 0. */
  def emit[F[_]: Sync, Msg](msg: Msg): Sub[F, Msg] =
    timeout(FiniteDuration(0, TimeUnit.MILLISECONDS), msg, msg.toString)

  /** A subscription that produces a `msg` after a `duration`. */
  def timeout[F[_]: Sync, Msg](duration: FiniteDuration, msg: Msg, id: String): Sub[F, Msg] =
    def task(callback: Either[Throwable, Msg] => Unit): F[Option[F[Unit]]] =
      val handle = dom.window.setTimeout(
        Functions.fun0(() => callback(Right(msg))),
        duration.toMillis.toDouble
      )
      Sync[F].delay {
        Option(Sync[F].delay(dom.window.clearTimeout(handle)))
      }

    Observe(id, Sync[F].pure(task))

  /** A subscription that produces a `msg` after a `duration`. */
  def timeout[F[_]: Sync, Msg](duration: FiniteDuration, msg: Msg): Sub[F, Msg] =
    timeout(duration, msg, "[tyrian-sub-every] " + duration.toString + msg.toString)

  /** A subscription that repeatedly produces a `msg` based on an `interval`. */
  def every[F[_]: Sync](interval: FiniteDuration, id: String): Sub[F, js.Date] =
    Sub.make[F, js.Date, Int](id) { callback =>
      Sync[F].delay {
        dom.window.setInterval(
          Functions.fun0(() => callback(Right(new js.Date()))),
          interval.toMillis.toDouble
        )
      }
    } { handle =>
      Sync[F].delay(dom.window.clearTimeout(handle))
    }

  /** A subscription that repeatedly produces a `msg` based on an `interval`. */
  def every[F[_]: Sync](interval: FiniteDuration): Sub[F, js.Date] =
    every(interval, "[tyrian-sub-every] " + interval.toString)

  /** A subscription that emits a `msg` based on an a JavaScript event. */
  def fromEvent[F[_]: Sync, A, Msg](name: String, target: EventTarget)(extract: A => Option[Msg]): Sub[F, Msg] =
    Sub.make[F, A, Msg, js.Function1[A, Unit]](name + target.hashCode) { callback =>
      Sync[F].delay {
        val listener = Functions.fun { (a: A) =>
          callback(Right(a))
        }
        target.addEventListener(name, listener)
        listener
      }
    } { listener =>
      Sync[F].delay(target.removeEventListener(name, listener))
    }(extract)

  /** A subscription that emits a `msg` based on the running time in seconds whenever the browser renders an animation
    * frame.
    */
  @nowarn("msg=unused")
  def animationFrameTick[F[_]: Async, Msg](id: String)(toMsg: Double => Msg): Sub[F, Msg] =
    Sub.make(
      id,
      Stream.repeatEval {
        Async[F].async_[Msg] { cb =>
          dom.window.requestAnimationFrame { t =>
            cb(Right(toMsg(t / 1000)))
          }
          ()
        }
      }
    )

  def combineAll[F[_], A](list: List[Sub[F, A]]): Sub[F, A] =
    Monoid[Sub[F, A]].combineAll(list)

  // Cats' typeclass instances

  given [F[_], Msg]: Monoid[Sub[F, Msg]] with
    def empty: Sub[F, Msg]                                   = Sub.None
    def combine(a: Sub[F, Msg], b: Sub[F, Msg]): Sub[F, Msg] = Sub.merge(a, b)

  given [F[_], Msg]: Eq[Sub[F, Msg]] with
    def eqv(x: Sub[F, Msg], y: Sub[F, Msg]): Boolean = (x, y) match {
      case (Sub.None, Sub.None)                             => true
      case (Sub.Observe(id1, _, _), Sub.Observe(id2, _, _)) => id1 === id2
      case (xs, ys) =>
        SubHelper.flatten(xs).zip(SubHelper.flatten(ys)).forall((x, y) => eqv(x, y))
    }

  given [F[_]]: Functor[Sub[F, *]] with
    def map[A, B](fa: Sub[F, A])(f: A => B): Sub[F, B] = fa.map(f)

end Sub
