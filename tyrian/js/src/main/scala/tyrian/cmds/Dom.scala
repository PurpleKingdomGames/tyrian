package tyrian.cmds

import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLInputElement
import tyrian.Cmd

object Dom:

  final case class NotFound(elementId: String)

  def focus[Msg](elementId: String)(resultToMessage: Either[NotFound, Unit] => Msg): Cmd[Msg] =
    affectInputElement(elementId, _.focus(), resultToMessage)

  def blur[Msg](elementId: String)(resultToMessage: Either[NotFound, Unit] => Msg): Cmd[Msg] =
    affectInputElement(elementId, _.blur(), resultToMessage)

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  private def affectInputElement[Msg](
      elementId: String,
      modifier: HTMLInputElement => Unit,
      resultToMessage: Either[NotFound, Unit] => Msg
  ): Cmd[Msg] =
    Cmd
      .Run[NotFound, Unit] { observer =>
        val node = document.getElementById(elementId)

        if node != null then observer.onNext(modifier(node.asInstanceOf[HTMLInputElement]))
        else observer.onError(NotFound(elementId))

        () => ()
      }
      .attempt(resultToMessage)
