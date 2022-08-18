package tyrian

import cats.effect.kernel.Resource
import cats.effect.unsafe.implicits.global
import org.scalajs.dom.document
import tyrian.TyrianAppF
import tyrian.runtime.TyrianRuntime
import zio.*
import zio.interop.catz.*
import zio.interop.catz.implicits.*

import scala.scalajs.js.annotation._

/** The TyrianApp trait can be extended to conveniently prompt you for all the methods needed for a Tyrian app, as well
  * as providing a number of standard app launching methods.
  */
trait TyrianApp[Msg, Model] extends TyrianAppF[Task, Msg, Model]:

  val run: Resource[Task, TyrianRuntime[Task, Model, Msg]] => Unit = res =>
    val runtime  = Runtime.default
    val runnable = res.map(_.start()).useForever

    Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe.run(runnable).getOrThrowFiberFailure()
    }
