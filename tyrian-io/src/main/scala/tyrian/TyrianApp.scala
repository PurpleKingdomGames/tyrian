package tyrian

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.unsafe.implicits.global
import tyrian.TyrianAppF
import tyrian.runtime.TyrianRuntime

/** The TyrianApp trait can be extended to conveniently prompt you for all the methods needed for a Tyrian app, as well
  * as providing a number of standard app launching methods.
  */
trait TyrianApp[Msg, Model] extends TyrianAppF[IO, Msg, Model]:

  val run: Resource[IO, TyrianRuntime[IO, Model, Msg]] => Unit =
    _.map(_.start()).useForever.unsafeRunAndForget()
