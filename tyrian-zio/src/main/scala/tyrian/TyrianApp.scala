package tyrian

import cats.effect.kernel.Resource
import cats.effect.unsafe.implicits.global
import org.scalajs.dom.document
import tyrian.TyrianAppF
import tyrian.runtime.TyrianRuntime
import zio.*
import zio.interop.catz.*

import scala.scalajs.js.annotation._

/** The TyrianApp trait can be extended to conveniently prompt you for all the methods needed for a Tyrian app, as well
  * as providing a number of standard app launching methods.
  */
trait TyrianApp[Msg, Model] extends TyrianAppF[Z.Task, Msg, Model]:

  val run: Resource[Z.Task, TyrianRuntime[Z.Task, Model, Msg]] => Unit = resource =>
    val runtime = Runtime.default
    
    val t = ZIO.runtime.flatMap { implicit r: Runtime[Any] =>
      resource.map(_.start()).useForever
    }

    runtime.run(t)

object Z:
  type Task[A] = ZIO[Any, Throwable, A]
