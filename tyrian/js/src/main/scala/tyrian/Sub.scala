package tyrian

import cats.data.Kleisli
import cats.effect.kernel.Concurrent
import cats.effect.kernel.Sync
import cats.kernel.Monoid
import cats.syntax.all.*
import org.scalajs.dom
import org.scalajs.dom.EventTarget
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

  given CanEqual[Option[_], Option[_]] = CanEqual.derived
  given CanEqual[Sub[_, _], Sub[_, _]] = CanEqual.derived

  final def merge[F[_], Msg, LubMsg >: Msg](a: Sub[F, Msg], b: Sub[F, LubMsg]): Sub[F, LubMsg] =
    (a, b) match {
      case (Sub.Empty, Sub.Empty) => Sub.Empty
      case (Sub.Empty, s2)        => s2
      case (s1, Sub.Empty)        => s1
      case (s1, s2)               => Sub.Combine(s1, s2)
    }

  /** The empty subscription represents the absence of subscriptions */
  case object Empty extends Sub[Nothing, Nothing]:
    def map[OtherMsg](f: Nothing => OtherMsg): Empty.type = this

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
        Kleisli(toMsg).map(f).run
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
        val cancel = Kleisli((res: R) => Sync[F].delay(Option(release(res))))
        (Kleisli(acquire) andThen cancel).run
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
      val cancel = Kleisli((res: R) => Sync[F].delay(Option(release(res))))
      (Kleisli(acquire) andThen cancel).run
    }
    Observe[F, A, Msg](id, task, toMsg)

  /** Make a cancelable subscription that returns a value (to be mapped into a Msg) */
  def make[F[_]: Sync, A, R](id: String)(acquire: (Either[Throwable, A] => Unit) => F[R])(
      release: R => F[Unit]
  ): Sub[F, A] =
    make[F, A, A, R](id)(acquire)(release)(Option.apply)

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
  final case class Combine[F[_], +Msg](sub1: Sub[F, Msg], sub2: Sub[F, Msg]) extends Sub[F, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Sub[F, OtherMsg] = Combine(sub1.map(f), sub2.map(f))

  /** Treat many subscriptions as one */
  final case class Batch[F[_], Msg](subs: List[Sub[F, Msg]]) extends Sub[F, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Batch[F, OtherMsg] = this.copy(subs = subs.map(_.map(f)))
  object Batch:
    def apply[F[_], Msg](subs: Sub[F, Msg]*): Batch[F, Msg] =
      Batch(subs.toList)

  /** A subscription that emits a msg once. Identical to timeout with a duration of 0. */
  def emit[F[_]: Sync, Msg](msg: Msg): Sub[F, Msg] =
    timeout(FiniteDuration(0, TimeUnit.MILLISECONDS), msg, msg.toString)

  /** A subscription that produces a `msg` after a `duration`. */
  def timeout[F[_]: Sync, Msg](duration: FiniteDuration, msg: Msg, id: String): Sub[F, Msg] =
    val acquire: F[(Either[Throwable, Msg] => Unit) => Int] =
      Sync[F].delay { (callback: Either[Throwable, Msg] => Unit) =>
        dom.window.setTimeout(
          Functions.fun0(() => callback(Right(msg))),
          duration.toMillis.toDouble
        )
      }

    val release: F[Int => F[Option[F[Unit]]]] =
      Sync[F].delay { (handle: Int) =>
        Sync[F].delay {
          Option(Sync[F].delay(dom.window.clearTimeout(handle)))
        }
      }

    val task =
      for {
        a <- acquire
        r <- release
      } yield a andThen r

    Observe(id, task)

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

  given [F[_], Msg]: Monoid[Sub[F, Msg]] = new Monoid[Sub[F, Msg]] {
    def empty: Sub[F, Msg]                                   = Sub.Empty
    def combine(a: Sub[F, Msg], b: Sub[F, Msg]): Sub[F, Msg] = Sub.merge(a, b)
  }

  def combineAll[F[_], A](list: List[Sub[F, A]]): Sub[F, A] =
    Monoid[Sub[F, A]].combineAll(list)

end Sub
