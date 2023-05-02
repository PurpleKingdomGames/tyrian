package tyrian

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.unsafe.implicits.global
import org.scalajs.dom.Element
import org.scalajs.dom.document
import tyrian.TyrianAppF
import tyrian.runtime.TyrianRuntime

import scala.scalajs.js.annotation._

/** The TyrianApp trait can be extended to conveniently prompt you for all the methods needed for a Tyrian app, as well
  * as providing a number of standard app launching methods.
  */
trait TyrianApp[Msg, Model] extends TyrianAppF[IO, Msg, Model]:

  val run: Resource[IO, TyrianRuntime[IO, Model, Msg]] => Unit =
    _.map(_.start()).useForever.unsafeRunAndForget()

  def ready(node: Element, flags: Map[String, String]): Unit =
    run(
      Tyrian.start(
        node,
        init(flags),
        update,
        view,
        subscriptions,
        MaxConcurrentTasks
      )
    )
