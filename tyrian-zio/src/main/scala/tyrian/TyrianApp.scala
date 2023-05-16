package tyrian

import cats.effect.Async
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import tyrian.TyrianAppF
import tyrian.runtime.TyrianRuntime
import zio.Runtime
import zio.Task
import zio.Unsafe

/** The TyrianApp trait can be extended to conveniently prompt you for all the methods needed for a Tyrian app, as well
  * as providing a number of standard app launching methods.
  */
trait TyrianApp[Msg, Model](using Async[Task]) extends TyrianAppF[Task, Msg, Model]:

  val run: Resource[Task, TyrianRuntime[Task, Model, Msg]] => Unit = res =>
    val runtime  = Runtime.default
    val runnable = res.map(_.start()).useForever

    Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe.run(runnable).getOrThrowFiberFailure()
    }

object TyrianApp:
  def onLoad[F[_] : Async](appDirectory: (String, TyrianAppF[F, _, _])*): Unit =
    TyrianAppF.onLoad(appDirectory: _*)

  def launch[F[_] : Async](appDirectory: (String, TyrianAppF[F, _, _])*): Unit =
    TyrianAppF.launch(appDirectory: _*)
