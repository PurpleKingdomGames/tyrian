package tyrian.cmds

import cats.effect.IO
import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLInputElement
import tyrian.Cmd

object Dom:

  case object Success
  final case class NotFound(elementId: String)

  def focus[Msg](elementId: String)(resultToMessage: Either[NotFound, Success.type] => Msg): Cmd[Msg] =
    affectInputElement(elementId, _.focus(), resultToMessage)

  def blur[Msg](elementId: String)(resultToMessage: Either[NotFound, Success.type] => Msg): Cmd[Msg] =
    affectInputElement(elementId, _.blur(), resultToMessage)

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  private def affectInputElement[Msg](
      elementId: String,
      modifier: HTMLInputElement => Unit,
      resultToMessage: Either[NotFound, Success.type] => Msg
  ): Cmd[Msg] =
    val task =
      IO.delay {
        val node = document.getElementById(elementId)
        if node != null then
          modifier(node.asInstanceOf[HTMLInputElement])
          Right(Success)
        else Left(NotFound(elementId))
      }
    Cmd.Run(task, resultToMessage)
