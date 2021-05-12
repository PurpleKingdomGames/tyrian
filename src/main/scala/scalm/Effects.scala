package scalm

import cats.{ApplicativeError, MonoidK}
import org.scalajs.dom
import org.scalajs.dom.EventTarget
import scalm.Task.Observable
import util.Functions
import util.Functions.fun

import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js

import scala.annotation.nowarn

/**
  * A subscription describes a resource that an application is interested in.
  *
  * Examples:
  *
  * - a timeout notifies its subscribers when it expires,
  * - a video being played notifies its subscribers with subtitles.
  *
  * @tparam Msg Type of message produced by the resource
  */
sealed trait Sub[+Msg] { sub1 =>

  /** Transforms the type of messages produced by the subscription */
  def map[OtherMsg](f: Msg => OtherMsg): Sub[OtherMsg]

  /** Merges the notifications of this subscription and `sub2` */
  final def combine[LubMsg >: Msg](sub2: Sub[LubMsg]): Sub[LubMsg] =
    (sub1, sub2) match {
      case (Sub.Empty, Sub.Empty) => Sub.Empty
      case (Sub.Empty, s2)        => s2
      case (s1       , Sub.Empty) => s1
      case (s1       , s2)        => Sub.Combine(s1, s2)
    }

}

object Sub {

  /** The empty subscription represents the absence of subscriptions */
  case object Empty extends Sub[Nothing] {
    def map[OtherMsg](f: Nothing => OtherMsg): Sub[OtherMsg] = this
  }

  /**
    * A subscription that forwards the notifications produced by the given `observable`
    * @param id Globally unique identifier for this subscription
    * @param observable Observable that produces notifications
    * @param f a function that turns every notification into a message
    * @tparam Err type of errors produced by the observable
    * @tparam Value type of notifications produced by the observable
    * @tparam Msg type of message produced by the subscription
    */
  // FIXME Use Task instead of Observable, at this level
  case class OfObservable[Err, Value, Msg](id: String, observable: Observable[Err, Value], f: Either[Err, Value] => Msg) extends Sub[Msg] {
    def map[OtherMsg](g: Msg => OtherMsg): Sub[OtherMsg] = OfObservable(id, observable, f andThen g)
  }

  /** Merge two subscriptions into a single one */
  case class Combine[+Msg](sub1: Sub[Msg], sub2: Sub[Msg]) extends Sub[Msg] {
    def map[OtherMsg](f: Msg => OtherMsg): Sub[OtherMsg] = Combine(sub1.map(f), sub2.map(f))
  }

  /**
    * Same as `OfObservable` but when the observable never fails
    *
    * @param id Subscription id
    * @param observable Source of messages that never produces errors
    * @return A subscription to the messages source
    */
  @nowarn // Supposedly can never fail, but is doing an unsafe projection.
  def ofTotalObservable[Msg](id: String, observable: Observable[Nothing, Msg]): Sub[Msg] =
    OfObservable[Nothing, Msg, Msg](id, observable, _.right.get)

  implicit val monoidKSub: MonoidK[Sub] =
    new MonoidK[Sub] {
      def empty[A]: Sub[A] = Sub.Empty
      def combineK[A](sub1: Sub[A], sub2: Sub[A]): Sub[A] = sub1.combine(sub2)
    }

  /**
    * @return A subscription that notifies its subscribers with `msg` after `duration`.
    * @param duration Duration of the timeout
    * @param msg Message produced by the timeout
    * @param id Globally unique identifier for this subscription
    * @tparam Msg Type of message
    */
  def timeout[Msg](duration: FiniteDuration, msg: Msg, id: String): Sub[Msg] =
    ofTotalObservable[Msg](id, { observer =>
      val handle =
        dom.window.setTimeout(Functions.fun0(() => observer.onNext(msg)), duration.toMillis.toDouble)
      () => dom.window.clearTimeout(handle)
    })

  def every(interval: FiniteDuration, id: String): Sub[js.Date] =
    ofTotalObservable[js.Date](id, { observer =>
      val handle =
        dom.window.setInterval(Functions.fun0(() => observer.onNext(new js.Date())), interval.toMillis.toDouble)
      () => dom.window.clearInterval(handle)
    })

  def fromEvent[A,B](name: String, target: EventTarget)(extract: A => Option[B]):Sub[B] =
    Sub.ofTotalObservable[B](name + target.hashCode, { observer =>
      val listener = fun { (a: A) =>
        extract(a) match {
          case Some(b) => observer.onNext(b)
          case None => ()
        }
      }
      target.addEventListener(name, listener)
      () => target.removeEventListener(name, listener)
    })

}

/**
  * A task describes some side-effect to perform.
  *
  * Examples:
  *
  * - An XHR
  *
  * @tparam Err Type of error produced the task
  * @tparam Success Type of successful value produced by the task
  */
// TODO Cancellation support
sealed trait Task[+Err, +Success] {

  /** Transforms successful values */
  def map[Success2](f: Success => Success2): Task[Err, Success2] = Task.Mapped(this, f)

  /** Combines to tasks in parallel */
  def product[Success2, Err2 >: Err](that: Task[Err2, Success2]): Task[Err2, (Success, Success2)] = Task.Multiplied(this, that)

  /** Sequentially applies this task and then another task given by the function `f` */
  def flatMap[Success2, Err2 >: Err](f: Success => Task[Err2, Success2]): Task[Err2, Success2] = Task.FlatMapped(this, f)

  /**
    * Turns this task into a command by transforming errors and successful values according to the `f` function
    */
  def attempt[Msg](f: Either[Err, Success] => Msg): Cmd[Msg] = Cmd.RunTask(this, f)

  /**
    * Turns this task (that never fails) into a command
    */
  @nowarn // Supposedly can never fail, but is doing an unsafe projection.
  def perform[Err2 >: Err](implicit ev: Err2 =:= Nothing): Cmd[Success] = Cmd.RunTask[Err, Success, Success](this, _.right.get)

}

object Task {

  /**
    * Something that produces successful values of type `Value` and error values of type `Err`
    */
  trait Observable[Err, Value] {
    /**
      * Run this observable and attaches the given `observer` to its notifications.
      * @return a cancelable for this observable
      */
    def run(observer: Observer[Err, Value]): Cancelable
  }

  /**
    * An observer of successful values of type `Value` and errors of type `Err`
    */
  trait Observer[Err, Value] {
    def onNext(value: Value): Unit
    def onError(error: Err): Unit
  }

  trait Cancelable {
    def cancel(): Unit
  }

  /** A task that succeeded with the given `value` */
  case class Succeeded[A](value: A) extends Task[Nothing, A]
  /** A task that failed with the given `error` */
  case class Failed[A](error: A) extends Task[A, Nothing]
  /** A task that runs the given `observable` */
  case class RunObservable[Err, Success](observable: Observable[Err, Success]) extends Task[Err, Success]
  case class Recovered[Err, Success](task: Task[Err, Success], f: Err => Task[Err, Success]) extends Task[Err, Success]
  case class Mapped[Err, Success, Success2](task: Task[Err, Success], f: Success => Success2) extends Task[Err, Success2]
  case class Multiplied[Err, Success, Success2](task1: Task[Err, Success], task2: Task[Err, Success2]) extends Task[Err, (Success, Success2)]
  case class FlatMapped[Err, Success, Success2](task: Task[Err, Success], f: Success => Task[Err, Success2]) extends Task[Err, Success2]

  implicit def applicativeTask[Err]: ApplicativeError[Task[Err, *], Err] =
    new ApplicativeError[Task[Err, *], Err] {
      def pure[A](x: A) = Succeeded(x)
      def raiseError[A](e: Err): Task[Err, A] = Failed(e)
      def ap[A, B](ff: Task[Err, A => B])(fa: Task[Err, A]): Task[Err, B] = ff.product(fa).map { case (f, a) => f(a) }
      def handleErrorWith[A](fa: Task[Err, A])(f: Err => Task[Err, A]): Task[Err, A] = Recovered(fa, f)
    }

}

/**
  * A command describes some side-effect to perform.
  *
  * The difference with a `Task` is that a command never
  * produces error values.
  */
// FIXME Unify Cmd and Task (Cmd is just a Task[Nothing, ?])
sealed trait Cmd[+Msg] {
  def map[OtherMsg](f: Msg => OtherMsg): Cmd[OtherMsg]
}

object Cmd {
  case class RunTask[Err, Success, Msg](task: Task[Err, Success], f: Either[Err, Success] => Msg) extends Cmd[Msg] {
    def map[OtherMsg](g: Msg => OtherMsg): Cmd[OtherMsg] = RunTask(task, f andThen g)
  }
  case object Empty extends Cmd[Nothing] {
    def map[OtherMsg](f: Nothing => OtherMsg): Cmd[OtherMsg] = this
  }
}
