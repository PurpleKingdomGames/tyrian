package tyrian

import cats.effect.kernel.Concurrent
import cats.effect.kernel.Sync
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

object Sub:

  given CanEqual[Option[_], Option[_]] = CanEqual.derived
  given CanEqual[Sub[_, _], Sub[_, _]] = CanEqual.derived

  extension [F[_], Msg, LubMsg >: Msg](sub: Sub[F, Msg])
    def combine(other: Sub[F, LubMsg]): Sub[F, LubMsg] = Sub.merge(sub, other)
    def |+|(other: Sub[F, LubMsg]): Sub[F, LubMsg]     = Sub.merge(sub, other)

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
    *   Observable and cancellable/closable effect that produces notifications, encoded as a callback with an effect
    *   describing how to close the subscription.
    * @param toMsg
    *   a function that turns every notification value into a message
    * @tparam F
    *   type of the effect monad, must be a Cats Effect 3 Concurrent.
    * @tparam A
    *   type of notification values produced by the observable
    * @tparam Msg
    *   type of message produced by the subscription
    */
  final case class Observe[F[_], A, Msg](
      id: String,
      observable: F[(Either[Throwable, A] => Unit) => F[Unit]],
      toMsg: A => Msg
  ) extends Sub[F, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Sub[F, OtherMsg] =
      Observe(
        id,
        observable,
        toMsg andThen f
      )

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

  /** A subscription that notifies its subscribers with `msg` after a `duration`. */
  def timeout[F[_]: Sync, Msg](duration: FiniteDuration, msg: Msg, id: String): Sub[F, Msg] =
    val task =
      Sync[F].delay { (callback: Either[Throwable, Msg] => Unit) =>
        val handle =
          dom.window.setTimeout(
            Functions.fun0(() => callback(Right(msg))),
            duration.toMillis.toDouble
          )
        Sync[F].delay(dom.window.clearTimeout(handle))
      }

    Observe[F, Msg, Msg](id, task, identity)

  /** A subscription that notifies its subscribers with `msg` after a `duration`. */
  def timeout[F[_]: Sync, Msg](duration: FiniteDuration, msg: Msg): Sub[F, Msg] =
    timeout(duration, msg, "[tyrian-sub-every] " + duration.toString + msg.toString)

  /** A subscription that repeatedly notifies its subscribers with `msg` based on an `interval`. */
  def every[F[_]: Sync](interval: FiniteDuration, id: String): Sub[F, js.Date] =
    val task =
      Sync[F].delay { (callback: Either[Throwable, js.Date] => Unit) =>
        val handle =
          dom.window.setInterval(
            Functions.fun0(() => callback(Right(new js.Date()))),
            interval.toMillis.toDouble
          )
        Sync[F].delay(dom.window.clearTimeout(handle))
      }

    Observe[F, js.Date, js.Date](id, task, identity)

  /** A subscription that repeatedly notifies its subscribers with `msg` based on an `interval`. */
  def every[F[_]: Sync](interval: FiniteDuration): Sub[F, js.Date] =
    every(interval, "[tyrian-sub-every] " + interval.toString)

  /** A subscription that emtis a `msg` based on an a JavaScript event. */
  def fromEvent[F[_]: Sync, A, Msg](name: String, target: EventTarget)(extract: A => Option[Msg]): Sub[F, Msg] =
    val task =
      Sync[F].delay { (callback: Either[Throwable, Msg] => Unit) =>
        val listener = Functions.fun { (a: A) =>
          extract(a) match
            case Some(msg) => callback(Right(msg))
            case None      => ()
        }
        target.addEventListener(name, listener)
        Sync[F].delay(target.removeEventListener(name, listener))
      }

    Observe[F, Msg, Msg](name + target.hashCode, task, identity)

end Sub
