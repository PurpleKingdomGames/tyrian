package tyrian

import cats.ApplicativeError

import scala.annotation.nowarn

/** A task describes some side-effect to perform.
  *
  * Examples:
  *
  *   - An XHR
  *
  * @tparam Err
  *   Type of error produced the task
  * @tparam Success
  *   Type of successful value produced by the task
  */
sealed trait Task[+Err, +Success]:

  /** Transforms successful values */
  def map[Success2](f: Success => Success2): Task[Err, Success2] =
    Task.Mapped(this, f)

  /** Combines to tasks in parallel */
  def product[Success2, Err2 >: Err](that: Task[Err2, Success2]): Task[Err2, (Success, Success2)] =
    Task.Multiplied(this, that)

  /** Sequentially applies this task and then another task given by the function `f` */
  def flatMap[Success2, Err2 >: Err](f: Success => Task[Err2, Success2]): Task[Err2, Success2] =
    Task.FlatMapped(this, f)

  /** Turns this task into a command by transforming errors and successful values according to the `f` function
    */
  def attempt[Msg](toMessage: Either[Err, Success] => Msg): Cmd[Msg] =
    Cmd.RunTask(this, toMessage)

  /** Turns this task (that never fails) into a command
    */
  @nowarn // Can never fail, but is doing an unsafe projection.
  def perform[Err2 >: Err](using Err2 =:= Nothing): Cmd[Success] =
    Cmd.RunTask[Err, Success, Success](this, _.toOption.get)

object Task:

  /** Something that produces successful values of type `Value` and error values of type `Err`
    */
  trait Observable[Err, Value] {

    /** Run this observable and attaches the given `observer` to its notifications.
      * @return
      *   a cancelable for this observable
      */
    def run(observer: Observer[Err, Value]): Cancelable
  }

  /** An observer of successful values of type `Value` and errors of type `Err`
    */
  trait Observer[Err, Value] {
    def onNext(value: Value): Unit
    def onError(error: Err): Unit
  }

  trait Cancelable {
    def cancel(): Unit
  }

  /** A task that is a fire and forget side effect */
  final case class SideEffect(thunk: () => Unit) extends Task[Nothing, Unit]

  /** A task that succeeded with the given `value` */
  final case class Succeeded[A](value: A) extends Task[Nothing, A]

  /** A task that failed with the given `error` */
  final case class Failed[A](error: A) extends Task[A, Nothing]

  /** A task that runs the given `observable` */
  final case class RunObservable[Err, Success](observable: Observable[Err, Success]) extends Task[Err, Success]

  final case class Recovered[Err, Success](task: Task[Err, Success], recoverWith: Err => Task[Err, Success])
      extends Task[Err, Success]

  final case class Mapped[Err, Success, Success2](task: Task[Err, Success], f: Success => Success2)
      extends Task[Err, Success2]

  final case class Multiplied[Err, Success, Success2](task1: Task[Err, Success], task2: Task[Err, Success2])
      extends Task[Err, (Success, Success2)]

  final case class FlatMapped[Err, Success, Success2](task: Task[Err, Success], f: Success => Task[Err, Success2])
      extends Task[Err, Success2]

  given [Err]: ApplicativeError[Task[Err, *], Err] =
    new ApplicativeError[Task[Err, *], Err] {
      def pure[A](x: A)                                                   = Succeeded(x)
      def raiseError[A](e: Err): Task[Err, A]                             = Failed(e)
      def ap[A, B](ff: Task[Err, A => B])(fa: Task[Err, A]): Task[Err, B] = ff.product(fa).map { case (f, a) => f(a) }
      def handleErrorWith[A](fa: Task[Err, A])(f: Err => Task[Err, A]): Task[Err, A] = Recovered(fa, f)
    }
