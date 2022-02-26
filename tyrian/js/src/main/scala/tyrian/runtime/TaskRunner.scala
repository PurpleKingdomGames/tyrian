package tyrian.runtime

import tyrian.Task

import scala.util.control.NonFatal

object TaskRunner:

  def asObserver[Err, Success](notifier: Either[Err, Success] => Unit): Task.Observer[Err, Success] =
    new Task.Observer[Err, Success] {
      def onNext(value: Success): Unit = notifier(Right(value))
      def onError(error: Err): Unit    = notifier(Left(error))
    }

  def execTask[Err, Success](task: Task[Err, Success], notifier: Either[Err, Success] => Unit): Unit =
    task match
      case Task.SideEffect(thunk)         => thunk()
      case Task.Delay(thunk)              => notifier(try { Right(thunk()) } catch { case NonFatal(e) => Left(e.getMessage) })
      case Task.Succeeded(value)          => notifier(Right(value))
      case Task.Failed(error)             => notifier(Left(error))
      case Task.RunObservable(observable) => observable.run(asObserver(notifier))
      case t @ Task.Mapped(_, _)          => execTaskMapped(t, notifier)
      case t @ Task.Recovered(_, _)       => execTaskRecovered(t, notifier)
      case t @ Task.Multiplied(_, _)      => execTaskMultiplied(t, notifier)
      case t @ Task.FlatMapped(_, _)      => execTaskFlatMapped(t, notifier)

  def execTaskMapped[Err, Success, Success2](
      mapped: Task.Mapped[Err, Success, Success2],
      notify: Either[Err, Success2] => Unit
  ): Unit =
    execTask[Err, Success](mapped.task, msg => notify(msg.map(mapped.f)))

  def execTaskRecovered[Err, Success](
      recovered: Task.Recovered[Err, Success],
      notify: Either[Err, Success] => Unit
  ): Unit =
    execTask[Err, Success](
      recovered.task,
      {
        case Left(err)      => execTask[Err, Success](recovered.recoverWith(err), notify)
        case Right(success) => notify(Right(success))
      }
    )

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  def execTaskMultiplied[Err, Success1, Success2](
      multiplied: Task.Multiplied[Err, Success1, Success2],
      notify: Either[Err, (Success1, Success2)] => Unit
  ): Unit = {
    type Result1 = Either[Err, Success1]
    type Result2 = Either[Err, Success2]
    var r1: Option[Result1] = None
    var r2: Option[Result2] = None
    def notifyProduct(): Unit =
      (r1, r2) match {
        case (Some(Right(s1)), Some(Right(s2))) => notify(Right((s1, s2)))
        case (Some(Left(e)), _)                 => notify(Left(e))
        case (_, Some(Left(e)))                 => notify(Left(e))
        case (_, _)                             => ()
      }
    execTask[Err, Success1](
      multiplied.task1,
      result =>
        if (r2.forall(_.isRight)) {
          r1 = Some(result)
          notifyProduct()
        }
    )
    execTask[Err, Success2](
      multiplied.task2,
      result =>
        if (r1.forall(_.isRight)) {
          r2 = Some(result)
          notifyProduct()
        }
    )
  }

  def execTaskFlatMapped[Err, Success, Success2](
      flatMapped: Task.FlatMapped[Err, Success, Success2],
      notify: Either[Err, Success2] => Unit
  ): Unit =
    execTask[Err, Success](
      flatMapped.task,
      _.foreach(success => execTask(flatMapped.f(success), notify))
    )
