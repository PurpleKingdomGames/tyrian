package tyrian

import cats.effect.IO
import cats.effect.unsafe.implicits.global

/** The TyrianApp trait can be extended to conveniently prompt you for all the methods needed for a Tyrian app, as well
  * as providing a number of standard app launching methods.
  */
trait TyrianIOApp[Msg, Model] extends TyrianAppF[IO, Msg, Model]:

  val run: IO[Nothing] => Unit = _.unsafeRunAndForget()

object TyrianIOApp:
  def onLoad(appDirectory: (String, TyrianAppF[IO, _, _])*): Unit =
    TyrianAppF.onLoad(appDirectory: _*)

  def launch(appDirectory: Map[String, TyrianAppF[IO, _, _]]): Unit =
    TyrianAppF.launch(appDirectory)
