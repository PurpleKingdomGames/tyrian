package tyrian.next

import cats.effect.IO
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
  * Watcher instances are Sub's with the `F` type fixed to a known effect type, like `IO` or `Task`, and the `Msg` type
  * fixed to `GlobalMsg`. However, they're a cut down version of Sub, exposing only common functions. If you need to
  * make a complex custom Watcher, then you'll need to make a Sub and wrap it in a Watcher instance.
  */
sealed trait Watcher:

  /** Transforms the type of messages produced by the watcher */
  def map(f: GlobalMsg => GlobalMsg): Watcher

  /** Converts a Watcher into a Sub */
  def toSub: Sub[IO, GlobalMsg]

object Watcher:

  given CanEqual[Option[?], Option[?]] = CanEqual.derived
  given CanEqual[Watcher, Watcher]     = CanEqual.derived

  /** The empty watcher that produces no messages. */
  def none: Watcher =
    Watcher.None

  /** Converts a Sub into a Watcher. */
  def fromSub(sub: Sub[IO, GlobalMsg]): Watcher =
    sub match
      case Sub.None =>
        Watcher.None

      case Sub.Observe(id, observable, toMsg) =>
        Watcher.Observe(id, observable, toMsg)

      case Sub.Combine(a, b) =>
        Watcher.Many(fromSub(a), fromSub(b))

      case Sub.Batch(watchers) =>
        Watcher.Many(Batch.fromList(watchers).map(fromSub))

  /** Creates a watcher from a Sub. */
  def apply(sub: Sub[IO, GlobalMsg]): Watcher =
    fromSub(sub)

  /** Creates a watcher that emits a message immediately. */
  def emit(msg: GlobalMsg): Watcher =
    timeout(FiniteDuration(0, TimeUnit.MILLISECONDS), msg, msg.toString)

  /** Creates a watcher that emits a message after a specified duration. */
  def timeout(duration: FiniteDuration, msg: GlobalMsg, id: String): Watcher =
    Watcher.fromSub(
      Sub.timeout[IO, GlobalMsg](duration, msg, id)
    )

  /** Creates a watcher that emits a message after a specified duration. */
  def timeout(duration: FiniteDuration, msg: GlobalMsg): Watcher =
    timeout(duration, msg, "[tyrian-watcher-timout] " + duration.toString + msg.toString)

  /** Creates a watcher that repeatedly emits messages at regular intervals. */
  def every(interval: FiniteDuration, id: String, toMsg: js.Date => GlobalMsg): Watcher =
    Watcher.fromSub(
      Sub.every[IO](interval, id).map(toMsg)
    )

  /** Creates a watcher that repeatedly emits messages at regular intervals. */
  def every(interval: FiniteDuration, toMsg: js.Date => GlobalMsg): Watcher =
    every(interval, "[tyrian-watcher-every] " + interval.toString, toMsg)

  /** Creates a watcher that listens for JavaScript events and emits messages based on them. */
  def fromEvent[A](name: String, target: EventTarget)(extract: A => Option[GlobalMsg]): Watcher =
    Watcher.fromSub(
      Sub.fromEvent[IO, A, GlobalMsg](name, target)(extract)
    )

  /** Creates a watcher that emits messages on each animation frame with elapsed time in seconds. */
  def animationFrameTick(id: String)(toMsg: Double => GlobalMsg): Watcher =
    Watcher.fromSub(
      Sub.animationFrameTick[IO, GlobalMsg](id)(toMsg)
    )

  /** The empty watcher represents the absence of watchers */
  private[next] case object None extends Watcher:
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
  private[next] final case class Observe[A](
      id: String,
      observable: IO[(Either[Throwable, A] => Unit) => IO[Option[IO[Unit]]]],
      toMsg: A => Option[GlobalMsg]
  ) extends Watcher:
    def map(f: GlobalMsg => GlobalMsg): Watcher =
      Observe(
        id,
        observable,
        toMsg.andThen(_.map(f))
      )

    def toSub: Sub[IO, GlobalMsg] =
      Sub.Observe[IO, A, GlobalMsg](id, observable, toMsg)

  private[next] object Observe:

    /** Construct a cancelable observable watcher by describing how to acquire and release the resource, and optionally
      * produce a message
      */
    def apply[A, R](
        id: String,
        acquire: (Either[Throwable, A] => Unit) => IO[R],
        release: R => IO[Unit],
        toMsg: A => Option[GlobalMsg]
    ): Watcher =
      val task = IO.delay {
        def cancel(res: R) = Option(release(res))
        (cb: Either[Throwable, A] => Unit) => acquire(cb).map(cancel)
      }
      Observe[A](id, task, toMsg)

    /** Construct a cancelable observable watcher of a value */
    def apply[A](id: String, toMsg: A => Option[GlobalMsg])(
        observable: IO[(Either[Throwable, A] => Unit) => IO[Option[IO[Unit]]]]
    ): Watcher =
      Observe(id, observable, toMsg)

  /** Treat many watchers as one */
  private[next] final case class Many(watchers: Batch[Watcher]) extends Watcher:
    def map(f: GlobalMsg => GlobalMsg): Many = this.copy(watchers = watchers.map(_.map(f)))
    def ++(other: Many): Many                = Many(watchers ++ other.watchers)
    def ::(watcher: Watcher): Many           = Many(watcher :: watchers)
    def +:(watcher: Watcher): Many           = Many(watcher +: watchers)
    def :+(watcher: Watcher): Many           = Many(watchers :+ watcher)

    def toSub: Sub[IO, GlobalMsg] =
      Sub.Batch(watchers.map(_.toSub).toList)

  private[next] object Many:
    def apply(watchers: Watcher*): Many =
      Many(Batch.fromSeq(watchers))
