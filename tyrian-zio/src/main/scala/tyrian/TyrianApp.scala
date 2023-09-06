package tyrian

import cats.effect.kernel.Async
import zio.Runtime
import zio.Task
import zio.Unsafe

import scala.annotation.nowarn

/** The TyrianApp trait can be extended to conveniently prompt you for all the methods needed for a Tyrian app, as well
  * as providing a number of standard app launching methods.
  */
trait TyrianApp[Msg, Model](using Async[Task]) extends TyrianAppF[Task, Msg, Model]:

  private val runtime = Runtime.default

  @nowarn("msg=discarded")
  val run: Task[Nothing] => Unit = runnable =>
    Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe.fork(runnable)
    }

object TyrianApp:
  def onLoad(appDirectory: (String, TyrianAppF[Task, _, _])*)(using Async[Task]): Unit =
    TyrianAppF.onLoad(appDirectory: _*)

  def launch(appDirectory: Map[String, TyrianAppF[Task, _, _]])(using Async[Task]): Unit =
    TyrianAppF.launch(appDirectory)
