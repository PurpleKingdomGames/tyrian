package tyrian

import cats.MonoidK
import org.scalajs.dom
import org.scalajs.dom.EventTarget
import tyrian.Task.Observable
import util.Functions

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
sealed trait Sub[+Msg]:

  /** Transforms the type of messages produced by the subscription */
  def map[OtherMsg](f: Msg => OtherMsg): Sub[OtherMsg]

  /** Merges the notifications of this subscription and `sub2` */
  final def combine[LubMsg >: Msg](sub2: Sub[LubMsg]): Sub[LubMsg] =
    (this, sub2) match {
      case (Sub.Empty, Sub.Empty) => Sub.Empty
      case (Sub.Empty, s2)        => s2
      case (s1, Sub.Empty)        => s1
      case (s1, s2)               => Sub.Combine(s1, s2)
    }
  final def |+|[LubMsg >: Msg](sub2: Sub[LubMsg]): Sub[LubMsg] =
    combine(sub2)

object Sub:

  given CanEqual[Option[_], Option[_]] = CanEqual.derived
  given CanEqual[Sub[_], Sub[_]]       = CanEqual.derived

  /** The empty subscription represents the absence of subscriptions */
  case object Empty extends Sub[Nothing]:
    def map[OtherMsg](f: Nothing => OtherMsg): Sub[OtherMsg] = this

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
  // FIXME Use Task instead of Observable, at this level
  case class OfObservable[Err, Value, Msg](id: String, observable: Observable[Err, Value], f: Either[Err, Value] => Msg)
      extends Sub[Msg]:
    def map[OtherMsg](g: Msg => OtherMsg): Sub[OtherMsg] = OfObservable(id, observable, f andThen g)

  /** Merge two subscriptions into a single one */
  case class Combine[+Msg](sub1: Sub[Msg], sub2: Sub[Msg]) extends Sub[Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Sub[OtherMsg] = Combine(sub1.map(f), sub2.map(f))

  /** Same as `OfObservable` but when the observable never fails
    *
    * @param id
    *   Subscription id
    * @param observable
    *   Source of messages that never produces errors
    * @return
    *   A subscription to the messages source
    */
  @nowarn // Supposedly can never fail, but is doing an unsafe projection.
  def ofTotalObservable[Msg](id: String, observable: Observable[Nothing, Msg]): Sub[Msg] =
    OfObservable[Nothing, Msg, Msg](id, observable, _.toOption.get)

  given MonoidK[Sub] =
    new MonoidK[Sub]:
      def empty[A]: Sub[A]                                = Sub.Empty
      def combineK[A](sub1: Sub[A], sub2: Sub[A]): Sub[A] = sub1.combine(sub2)

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
  def timeout[Msg](duration: FiniteDuration, msg: Msg, id: String): Sub[Msg] =
    ofTotalObservable[Msg](
      id,
      { observer =>
        val handle =
          dom.window.setTimeout(Functions.fun0(() => observer.onNext(msg)), duration.toMillis.toDouble)
        () => dom.window.clearTimeout(handle)
      }
    )

  def every(interval: FiniteDuration, id: String): Sub[js.Date] =
    ofTotalObservable[js.Date](
      id,
      { observer =>
        val handle =
          dom.window.setInterval(Functions.fun0(() => observer.onNext(new js.Date())), interval.toMillis.toDouble)
        () => dom.window.clearInterval(handle)
      }
    )

  def fromEvent[A, B](name: String, target: EventTarget)(extract: A => Option[B]): Sub[B] =
    Sub.ofTotalObservable[B](
      name + target.hashCode,
      { observer =>
        val listener = Functions.fun { (a: A) =>
          extract(a) match {
            case Some(b) => observer.onNext(b)
            case None    => ()
          }
        }
        target.addEventListener(name, listener)
        () => target.removeEventListener(name, listener)
      }
    )

end Sub
