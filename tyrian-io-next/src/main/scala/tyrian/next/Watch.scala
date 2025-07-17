package tyrian.next

import cats.effect.IO
import fs2.Stream
import org.scalajs.dom.EventTarget
import tyrian.Sub

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js

/** A watcher describes a resource that an application is interested in.
  *
  * Examples:
  *
  *   - a timeout notifies its subscribers when it expires,
  *   - a video being played notifies its subscribers with subtitles.
  *
  * Watch instances are Sub's with the `F` type fixed to a known effect type, like `IO` or `Task`, and the `Msg` type
  * fixed to `GlobalMsg`.
  */
sealed trait Watch:

  /** Transforms the type of messages produced by the watcher */
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
      case (s1, s2)                 => Watch.Many(s1, s2)
    }

  /** The empty watcher represents the absence of watchers */
  case object None extends Watch:
    def map(f: GlobalMsg => GlobalMsg): None.type = this

    def toSub: Sub[IO, GlobalMsg] =
      Sub.None

  /** A watcher that forwards the notifications produced by the given `observable`
    * @param id
    *   Globally unique identifier for this watcher
    * @param observable
    *   Observable and cancellable/closable effect that produces notifications. Encoded as a callback with an effect
    *   describing how to optionally close the watcher.
    * @param toMsg
    *   a function that turns every notification value into a possible message
    * @tparam IO
    *   type of the effect monad, must be a Cats Effect 3 Concurrent.
    * @tparam A
    *   type of notification values produced by the observable
    * @tparam GlobalMsg
    *   type of message produced by the watcher
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

    /** Construct a cancelable observable watcher by describing how to acquire and release the resource, and optionally
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

    /** Construct a cancelable observable watcher of a value */
    def apply[A](id: String, toMsg: A => Option[GlobalMsg])(
        observable: IO[(Either[Throwable, A] => Unit) => IO[Option[IO[Unit]]]]
    ): Watch =
      Observe(id, observable, toMsg)

  /** Make a cancelable watcher that produces an optional message */
  def make[A, R](id: String)(acquire: (Either[Throwable, A] => Unit) => IO[R])(
      release: R => IO[Unit]
  )(toMsg: A => Option[GlobalMsg]): Watch =
    Watch.fromSub(
      Sub.make[IO, A, GlobalMsg, R](id)(acquire)(release)(toMsg)
    )

  /** Make a watcher based on an fs2.Stream. The stream is cancelled when it it removed from the list of watchers.
    */
  def make[A](id: String, stream: Stream[IO, A])(
      toMsg: A => GlobalMsg
  ): Watch =
    Watch.fromSub(
      Sub.make[IO, A](id, stream).map(toMsg)
    )

  /** Make a watcher based on an fs2.Stream with additional custom clean up. The stream itself is always cancelled when
    * it it removed from the list of watchers, even if no particular clean up is defined.
    */
  def make[A](id: String)(stream: Stream[IO, A])(cleanUp: IO[Unit])(
      toMsg: A => GlobalMsg
  ): Watch =
    Watch.fromSub(
      Sub.make[IO, A](id)(stream)(cleanUp).map(toMsg)
    )

  /** Make an uncancelable watcher that produces am optional message */
  def forever[A](acquire: (Either[Throwable, A] => Unit) => Unit)(
      toMsg: A => Option[GlobalMsg]
  ): Watch =
    Watch.fromSub(
      Sub.forever[IO, A, GlobalMsg](acquire)(toMsg)
    )

  /** Treat many watchers as one */
  final case class Many(watchers: List[Watch]) extends Watch:
    def map(f: GlobalMsg => GlobalMsg): Many = this.copy(watchers = watchers.map(_.map(f)))
    def ++(other: Many): Many                = Many(watchers ++ other.watchers)
    def ::(watcher: Watch): Many             = Many(watcher :: watchers)
    def +:(watcher: Watch): Many             = Many(watcher +: watchers)
    def :+(watcher: Watch): Many             = Many(watchers :+ watcher)

    def toSub: Sub[IO, GlobalMsg] =
      Sub.Batch(watchers.map(_.toSub))

  object Many:
    def apply(watchers: Watch*): Many =
      Many(watchers.toList)

  /** A watcher that emits a msg once. Identical to timeout with a duration of 0. */
  def emit(msg: GlobalMsg): Watch =
    timeout(FiniteDuration(0, TimeUnit.MILLISECONDS), msg, msg.toString)

  /** A watcher that produces a `msg` after a `duration`. */
  def timeout(duration: FiniteDuration, msg: GlobalMsg, id: String): Watch =
    Watch.fromSub(
      Sub.timeout[IO, GlobalMsg](duration, msg, id)
    )

  /** A watcher that produces a `msg` after a `duration`. */
  def timeout(duration: FiniteDuration, msg: GlobalMsg): Watch =
    timeout(duration, msg, "[tyrian-watcher-timout] " + duration.toString + msg.toString)

  /** A watcher that repeatedly produces a `msg` based on an `interval`. */
  def every(interval: FiniteDuration, id: String, toMsg: js.Date => GlobalMsg): Watch =
    Watch.fromSub(
      Sub.every[IO](interval, id).map(toMsg)
    )

  /** A watcher that repeatedly produces a `msg` based on an `interval`. */
  def every(interval: FiniteDuration, toMsg: js.Date => GlobalMsg): Watch =
    every(interval, "[tyrian-watcher-every] " + interval.toString, toMsg)

  /** A watcher that emits a `msg` based on an a JavaScript event. */
  def fromEvent[A](name: String, target: EventTarget)(extract: A => Option[GlobalMsg]): Watch =
    Watch.fromSub(
      Sub.fromEvent[IO, A, GlobalMsg](name, target)(extract)
    )

  /** A watcher that emits a `msg` based on the running time in seconds whenever the browser renders an animation frame.
    */
  def animationFrameTick(id: String)(toMsg: Double => GlobalMsg): Watch =
    Watch.fromSub(
      Sub.animationFrameTick[IO, GlobalMsg](id)(toMsg)
    )

  def combineAll(list: Batch[Watch]): Watch =
    Watch.fromSub(
      Sub.combineAll[IO, GlobalMsg](list.toList.map(_.toSub))
    )

  def fromSub(watcher: Sub[IO, GlobalMsg]): Watch =
    watcher match
      case Sub.None =>
        Watch.None

      case Sub.Observe(id, observable, toMsg) =>
        Watch.Observe(id, observable, toMsg)

      case Sub.Combine(a, b) =>
        Watch.Many(fromSub(a), fromSub(b))

      case Sub.Batch(watchers) =>
        Watch.Many(watchers.map(fromSub))
