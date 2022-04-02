package tyrian

import cats.effect.IO
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.effect.unsafe.implicits.global
import org.scalajs.dom.document

import scala.scalajs.js.annotation._

trait TyrianApp[Msg, Model]:

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg])

  def update(msg: Msg, model: Model): (Model, Cmd[IO, Msg])

  def view(model: Model): Html[Msg]

  def subscriptions(model: Model): Sub[IO, Msg]

  @JSExport
  def launch(containerId: String): Unit =
    ready(containerId, Map[String, String]())

  @JSExport
  def launch(containerId: String, flags: scala.scalajs.js.Dictionary[String]): Unit =
    ready(containerId, flags.toMap)

  def launch(containerId: String, flags: Map[String, String]): Unit =
    ready(containerId, flags)

  // @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def ready(parentElementId: String, flags: Map[String, String]): Unit =
    // This appears to be no better than the commented out version below.
    // val acquire =
    //   Tyrian
    //     .start[IO, Model, Msg](
    //       document.getElementById(parentElementId),
    //       init(flags),
    //       update,
    //       view,
    //       subscriptions
    //     )

    // val res: Resource[IO, Unit] =
    //   Resource.make(acquire)(_ => IO(()))

    // res.useForever.unsafeRunAndForget()
    Tyrian
      .start[IO, Model, Msg](
        document.getElementById(parentElementId),
        init(flags),
        update,
        view,
        subscriptions
      )
      .unsafeRunAndForget()
