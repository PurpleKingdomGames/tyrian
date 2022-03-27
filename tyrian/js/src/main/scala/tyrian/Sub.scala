package tyrian

import cats.effect.IO
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
  final case class OfObservable[A, Msg](
      id: String,
      observable: IO[(Either[Throwable, A] => Unit) => IO[Unit]],
      toMsg: A => Msg
  ) extends Sub[Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Sub[OtherMsg] =
      OfObservable(
        id,
        observable,
        toMsg andThen f
      )

  /** Merge two subscriptions into a single one */
  final case class Combine[+Msg](sub1: Sub[Msg], sub2: Sub[Msg]) extends Sub[Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Sub[OtherMsg] = Combine(sub1.map(f), sub2.map(f))

  /** Treat many subscriptions as one */
  final case class Batch[Msg](subs: List[Sub[Msg]]) extends Sub[Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Batch[OtherMsg] = this.copy(subs = subs.map(_.map(f)))
  object Batch:
    def apply[Msg](subs: Sub[Msg]*): Batch[Msg] =
      Batch(subs.toList)

  /** @return
    *   A subscription that emits a msg once. Identical to timeout with a duration of 0.
    * @param msg
    *   Message immediately produced
    * @tparam Msg
    *   Type of message
    */
  def emit[Msg](msg: Msg): Sub[Msg] =
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
  def timeout[Msg](duration: FiniteDuration, msg: Msg, id: String): Sub[Msg] =
    // val acquire: IO[Int] =
    //   Dispatcher[IO].use { dispatcher =>
    //     IO.delay {
    //       dom.window.setTimeout(
    //         Functions.fun0(() => dispatcher.unsafeRunAndForget(IO(msg))),
    //         duration.toMillis.toDouble
    //       )
    //     }
    //   }

    // IO.async { callback =>
    //   IO {
    //     dom.window.setTimeout(
    //       Functions.fun0(() => callback(Right(msg))),
    //       duration.toMillis.toDouble
    //     )
    //     None
    //   }
    // }

    // IO {
    //   dom.window.setTimeout(
    //     Functions.fun0(() => msg),
    //     duration.toMillis.toDouble
    //   )
    // }

    // val release = (handler: Int) =>
    //   IO {
    //     dom.window.clearTimeout(handler)
    //   }

    // val res =
    //   Resource.make(acquire)(release)

    // val task: IO[Msg] =
    //   IO.async { callback =>
    //     IO {
    //       val handle =
    //         dom.window.setTimeout(
    //           Functions.fun0(() => callback(Right(msg))),
    //           duration.toMillis.toDouble
    //         )
    //       Some(IO(dom.window.clearTimeout(handle)))
    //     }
    //   }

    // val task = IO.fromFuture(
    //   IO {
    //     val p = Promise[Msg]()
    //     val handle =
    //       dom.window.setTimeout(Functions.fun0(() => p.success(msg)), duration.toMillis.toDouble)
    //     p.future.map(m => (m, handle))
    //   }
    // )

    // val sub: IO[Msg] =
    //   for {
    //     res         <- task
    //     cancellable <- IO(res._1).onCancel(IO(dom.window.clearTimeout(res._2)))
    //   } yield cancellable

    // IO.delay {
    //   val handle =
    //     dom.window.setTimeout(Functions.fun0(() => msg), duration.toMillis.toDouble)
    // }

    val task =
      // Dispatcher[IO].use { dispatcher =>
      IO.delay { (callback: Either[Throwable, Msg] => Unit) =>
        val handle =
          dom.window.setTimeout(
            Functions.fun0(() => callback(Right(msg))),
            duration.toMillis.toDouble
          )
        IO(dom.window.clearTimeout(handle))
      }
    // }
    // Dispatcher[IO].use { dispatcher =>
    //   IO.delay { (callback: Either[Throwable, Msg] => Unit) =>
    //     val handle =
    //       dom.window.setTimeout(
    //         Functions.fun0(() => dispatcher.unsafeRunAndForget(IO(callback(Right(msg))))),
    //         duration.toMillis.toDouble
    //       )
    //     IO(dom.window.clearTimeout(handle))
    //   }
    // }

    OfObservable[Msg, Msg](id, task, identity) // TODO, the toMsg function...

  def every(interval: FiniteDuration, id: String): Sub[js.Date] =
    // IO.async { callback =>
    //   IO {
    //     val handle =
    //       dom.window.setInterval(
    //         Functions.fun0(() => callback(Right(new js.Date()))),
    //         interval.toMillis.toDouble
    //       )
    //     Some(IO(dom.window.clearTimeout(handle)))
    //   }
    // }
    // val task = IO.fromFuture(
    //   IO {
    //     val p = Promise[js.Date]()
    //     val handle =
    //       dom.window.setInterval(Functions.fun0(() => p.success(new js.Date())), interval.toMillis.toDouble)
    //     p.future.map(m => (m, handle))
    //   }
    // )

    // val sub: IO[js.Date] =
    //   for {
    //     res         <- task
    //     cancellable <- IO(res._1).onCancel(IO(dom.window.clearTimeout(res._2)))
    //   } yield cancellable

    val task =
      // Dispatcher[IO].use { dispatcher =>
        IO.delay { (callback: Either[Throwable, js.Date] => Unit) =>
          val handle =
            dom.window.setInterval(
              Functions.fun0(() => callback(Right(new js.Date()))),
              interval.toMillis.toDouble
            )
          IO(dom.window.clearTimeout(handle))
        }
      // }

    OfObservable[js.Date, js.Date](id, task, identity) // TODO, the toMsg function...

  def fromEvent[A, Msg](name: String, target: EventTarget)(extract: A => Option[Msg]): Sub[Msg] =
    val task =
      // IO.async { callback =>
      //   IO {
      //     val listener = Functions.fun { (a: A) =>
      //       extract(a) match
      //         case Some(msg) =>
      //           println("calling the callback with " + msg.toString)
      //           callback(Right(msg))
      //         case None => ()
      //     }
      //     target.addEventListener(name, listener)
      //     Some(IO(target.removeEventListener(name, listener)))
      //   }
      // }
      // Dispatcher[IO].use { dispatcher =>
        IO.delay { (callback: Either[Throwable, Msg] => Unit) =>
          val listener = Functions.fun { (a: A) =>
            extract(a) match
              case Some(msg) =>
                println("calling the callback with " + msg.toString)
                callback(Right(msg))
              case None => ()
          }
          target.addEventListener(name, listener)
          IO(target.removeEventListener(name, listener))
        }
      // }

    // val task = IO.fromFuture(
    //   IO {
    //     val p = Promise[Msg]()
    //     val listener = Functions.fun { (a: A) =>
    //       extract(a) match
    //         case Some(msg) => p.success(msg)
    //         case None      => ()
    //     }
    //     target.addEventListener(name, listener)
    //     p.future.map(m => (m, listener))
    //   }
    // )

    // val sub: IO[Msg] =
    //   for {
    //     res         <- task
    //     cancellable <- IO(res._1).onCancel(IO(target.removeEventListener(name, res._2)))
    //   } yield cancellable

    OfObservable[Msg, Msg](name + target.hashCode, task, identity) // TODO, the toMsg function...

end Sub
