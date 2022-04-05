package tyrian

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.scalajs.dom.document
import tyrian.TyrianAppF
import tyrian.runtime.RunWithCallback

import scala.scalajs.js.annotation._

/** The TyrianApp trait can be extended to conveniently prompt you for all the methods needed for a Tyrian app, as well
  * as providing a number of standard app launching methods.
  */
trait TyrianApp[Msg, Model] extends TyrianAppF[IO, Msg, Model]:

  protected val runner: RunWithCallback[IO, Msg] =
    task => cb => task.unsafeRunAsync(cb)
