package tyrian.cmds

import cats.effect.kernel.Async
import org.scalajs.dom.HTMLInputElement
import org.scalajs.dom.document
import tyrian.Cmd

/** Dom utilities */
object Dom:

  case object Success
  final case class NotFound(elementId: String)

  /** Focus (highlight) on a DOM input element */
  def focus[F[_]: Async, Msg](elementId: String)(resultToMessage: Either[NotFound, Success.type] => Msg): Cmd[F, Msg] =
    affectInputElement(elementId, _.focus(), resultToMessage)

  /** Blur (deselect) a DOM input element */
  def blur[F[_]: Async, Msg](elementId: String)(resultToMessage: Either[NotFound, Success.type] => Msg): Cmd[F, Msg] =
    affectInputElement(elementId, _.blur(), resultToMessage)

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  private def affectInputElement[F[_]: Async, Msg](
      elementId: String,
      modifier: HTMLInputElement => Unit,
      resultToMessage: Either[NotFound, Success.type] => Msg
  ): Cmd[F, Msg] =
    val task =
      Async[F].delay {
        val node = document.getElementById(elementId)
        if node != null then
          modifier(node.asInstanceOf[HTMLInputElement])
          Right(Success)
        else Left(NotFound(elementId))
      }
    Cmd.Run(task, resultToMessage)
