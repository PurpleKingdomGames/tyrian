package tyrian

import cats.effect.kernel.Async
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
sealed trait Sub[F[_]: Async, +Msg]:

  /** Transforms the type of messages produced by the subscription */
  def map[OtherMsg](f: Msg => OtherMsg): Sub[F, OtherMsg]

  /** Merges the notifications of this subscription and `sub2` */
  final def combine[LubMsg >: Msg](sub2: Sub[F, LubMsg]): Sub[F, LubMsg] =
    (this, sub2) match {
      case (Sub.Empty(), Sub.Empty()) => Sub.Empty()
      case (Sub.Empty(), s2)          => s2
      case (s1, Sub.Empty())          => s1
      case (s1, s2)                   => Sub.Combine(s1, s2)
    }
  final def |+|[LubMsg >: Msg](sub2: Sub[F, LubMsg]): Sub[F, LubMsg] =
    combine(sub2)

object Sub:

  given CanEqual[Option[_], Option[_]] = CanEqual.derived
  given CanEqual[Sub[_, _], Sub[_, _]] = CanEqual.derived

  /** The empty subscription represents the absence of subscriptions */
  final case class Empty[F[_]: Async]() extends Sub[F, Nothing]:
    def map[OtherMsg](f: Nothing => OtherMsg): Sub[F, OtherMsg] = this

  /** A subscription that forwards the notifications produced by the given `observable`
    * @param id
    *   Globally unique identifier for this subscription
    * @param observable
    *   Observable that produces notifications
    * @param f
    *   a function that turns every notification into a message
    * @tparam Err
    *   type of errors produced by the observable
    * @tparam Value
    *   type of notifications produced by the observable
    * @tparam Msg
    *   type of message produced by the subscription
    */
  final case class OfObservable[F[_]: Async, A, Msg](
      id: String,
      observable: F[(Either[Throwable, A] => Unit) => F[Unit]],
      toMsg: A => Msg
  ) extends Sub[F, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Sub[F, OtherMsg] =
      OfObservable(
        id,
        observable,
        toMsg andThen f
      )

  /** Merge two subscriptions into a single one */
  final case class Combine[F[_]: Async, +Msg](sub1: Sub[F, Msg], sub2: Sub[F, Msg]) extends Sub[F, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Sub[F, OtherMsg] = Combine(sub1.map(f), sub2.map(f))

  /** Treat many subscriptions as one */
  final case class Batch[F[_]: Async, Msg](subs: List[Sub[F, Msg]]) extends Sub[F, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Batch[F, OtherMsg] = this.copy(subs = subs.map(_.map(f)))
  object Batch:
    def apply[F[_]: Async, Msg](subs: Sub[F, Msg]*): Batch[F, Msg] =
      Batch(subs.toList)
// enum Sub[F[_]: Async, +Msg]:

//   /** Transforms the type of messages produced by the subscription */
//   def map[OtherMsg](f: Msg => OtherMsg): Sub[F, OtherMsg] =
//     this match
//       case s: Sub.Empty[F]              => s.map(f)
//       case s: Sub.OfObservable[F, _, _] => s.map(f)
//       case s: Sub.Combine[F, _]         => s.map(f)
//       case s: Sub.Batch[F, _]           => s.map(f)

//   /** Merges the notifications of this subscription and `sub2` */
//   def combine[LubMsg >: Msg](sub2: Sub[F, LubMsg]): Sub[F, LubMsg] =
//     (this, sub2) match {
//       case (Sub.Empty(), Sub.Empty()) => Sub.Empty()
//       case (Sub.Empty(), s2)          => s2
//       case (s1, Sub.Empty())          => s1
//       case (s1, s2)                   => Sub.Combine(s1, s2)
//     }
//   def |+|[LubMsg >: Msg](sub2: Sub[F, LubMsg]): Sub[F, LubMsg] =
//     combine(sub2)

//   /** The empty subscription represents the absence of subscriptions */
//   case Empty[F[_]: Async]() extends Sub[F, Nothing]
//   extension [F[_]: Async](sub: Empty[F]) def map[OtherMsg](f: Nothing => OtherMsg): Sub[F, OtherMsg] = sub

//   /** A subscription that forwards the notifications produced by the given `observable`
//     * @param id
//     *   Globally unique identifier for this subscription
//     * @param observable
//     *   Observable that produces notifications
//     * @param f
//     *   a function that turns every notification into a message
//     * @tparam Err
//     *   type of errors produced by the observable
//     * @tparam Value
//     *   type of notifications produced by the observable
//     * @tparam Msg
//     *   type of message produced by the subscription
//     */
//   case OfObservable[F[_]: Async, A, Msg](
//       id: String,
//       observable: F[(Either[Throwable, A] => Unit) => F[Unit]],
//       toMsg: A => Msg
//   ) extends Sub[F, Msg]
//   extension [F[_]: Async, A, Msg](sub: OfObservable[F, A, Msg])
//     def map[OtherMsg](f: Msg => OtherMsg): Sub[F, OtherMsg] =
//       OfObservable(
//         sub.id,
//         sub.observable,
//         sub.toMsg andThen f
//       )

//   /** Merge two subscriptions into a single one */
//   case Combine[F[_]: Async, +Msg](sub1: Sub[F, Msg], sub2: Sub[F, Msg]) extends Sub[F, Msg]
//   extension [F[_]: Async, Msg](sub: Combine[F, Msg])
//     def map[OtherMsg](f: Msg => OtherMsg): Sub[F, OtherMsg] = Combine(sub.sub1.map(f), sub.sub2.map(f))

//   /** Treat many subscriptions as one */
//   case Batch[F[_]: Async, Msg](subs: List[Sub[F, Msg]]) extends Sub[F, Msg]
//   extension [F[_]: Async, Msg](sub: Batch[F, Msg])
//     def map[OtherMsg](f: Msg => OtherMsg): Batch[F, OtherMsg] = sub.copy(subs = sub.subs.map(_.map(f)))

// object Sub:

//   given CanEqual[Option[_], Option[_]] = CanEqual.derived
//   given CanEqual[Sub[_, _], Sub[_, _]] = CanEqual.derived

  def empty[F[_]: Async]: Sub.Empty[F] = Sub.Empty[F]()
//   def batch[F[_]: Async, Msg](subs: Sub[F, Msg]*): Batch[F, Msg] = Sub.Batch(subs.toList)

  /** @return
    *   A subscription that emits a msg once. Identical to timeout with a duration of 0.
    * @param msg
    *   Message immediately produced
    * @tparam Msg
    *   Type of message
    */
  def emit[F[_]: Async, Msg](msg: Msg): Sub[F, Msg] =
    timeout(FiniteDuration(0, TimeUnit.MILLISECONDS), msg, msg.toString)

  /** @return
    *   A subscription that notifies its subscribers with `msg` after `duration`.
    * @param duration
    *   Duration of the timeout
    * @param msg
    *   Message produced by the timeout
    * @param id
    *   Globally unique identifier for this subscription
    * @tparam Msg
    *   Type of message
    */
  def timeout[F[_]: Async, Msg](duration: FiniteDuration, msg: Msg, id: String): Sub[F, Msg] =
    val task =
      Async[F].delay { (callback: Either[Throwable, Msg] => Unit) =>
        val handle =
          dom.window.setTimeout(
            Functions.fun0(() => callback(Right(msg))),
            duration.toMillis.toDouble
          )
        Async[F].delay(dom.window.clearTimeout(handle))
      }

    Sub.OfObservable[F, Msg, Msg](id, task, identity) // TODO, the toMsg function...

  def every[F[_]: Async](interval: FiniteDuration, id: String): Sub[F, js.Date] =
    val task =
      Async[F].delay { (callback: Either[Throwable, js.Date] => Unit) =>
        val handle =
          dom.window.setInterval(
            Functions.fun0(() => callback(Right(new js.Date()))),
            interval.toMillis.toDouble
          )
        Async[F].delay(dom.window.clearTimeout(handle))
      }

    Sub.OfObservable[F, js.Date, js.Date](id, task, identity) // TODO, the toMsg function...

  def fromEvent[F[_]: Async, A, Msg](name: String, target: EventTarget)(extract: A => Option[Msg]): Sub[F, Msg] =
    val task =
      Async[F].delay { (callback: Either[Throwable, Msg] => Unit) =>
        val listener = Functions.fun { (a: A) =>
          extract(a) match
            case Some(msg) =>
              println("calling the callback with " + msg.toString)
              callback(Right(msg))
            case None => ()
        }
        target.addEventListener(name, listener)
        Async[F].delay(target.removeEventListener(name, listener))
      }

    Sub.OfObservable[F, Msg, Msg](name + target.hashCode, task, identity) // TODO, the toMsg function...
