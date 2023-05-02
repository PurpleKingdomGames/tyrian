package tyrian

import cats.effect.Async
import cats.effect.kernel.Resource
import cats.effect.unsafe.implicits.global
import org.scalajs.dom.document
import org.scalajs.dom.Element
import tyrian.TyrianAppF
import tyrian.runtime.TyrianRuntime
import zio.Runtime
import zio.Task
import zio.Unsafe

import scala.scalajs.js.annotation._

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
