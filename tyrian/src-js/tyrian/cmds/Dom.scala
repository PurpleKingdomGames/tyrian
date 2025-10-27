package tyrian.cmds

import cats.effect.kernel.Async
import org.scalajs.dom.HTMLInputElement
import org.scalajs.dom.document
import tyrian.Cmd

/** Dom utilities */
object Dom:

  case object Success
  final case class NotFound(elementId: String)

  /** Focus (highlight) on a DOM input element, emit message on complete */
  def focus[F[_]: Async, Msg](elementId: String)(resultToMessage: Either[NotFound, Success.type] => Msg): Cmd[F, Msg] =
    affectInputElement(elementId, _.focus(), resultToMessage)

  /** Focus (highlight) on a DOM input element, side effect only, use form `focus(id)(resultToMessage)` to emit a
    * message on completion.
    */
  def focus[F[_]: Async, Msg](elementId: String): Cmd[F, Msg] =
    affectInputElementNoResult(elementId, _.focus())

  /** Blur (deselect) a DOM input element, emit message on complete */
  def blur[F[_]: Async, Msg](elementId: String)(resultToMessage: Either[NotFound, Success.type] => Msg): Cmd[F, Msg] =
    affectInputElement(elementId, _.blur(), resultToMessage)

  /** Blur (deselect) a DOM input element, side effect only, use form `blur(id)(resultToMessage)` to emit a message on
    * completion.
    */
  def blur[F[_]: Async, Msg](elementId: String): Cmd[F, Msg] =
    affectInputElementNoResult(elementId, _.blur())

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

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  private def affectInputElementNoResult[F[_]: Async, Msg](
      elementId: String,
      modifier: HTMLInputElement => Unit
  ): Cmd[F, Msg] =
    Cmd.SideEffect {
      val node = document.getElementById(elementId)
      if node != null then modifier(node.asInstanceOf[HTMLInputElement])
    }
