package tyrian.cmds

import cats.effect.kernel.Async
import org.scalajs.dom
import org.scalajs.dom.Event
import org.scalajs.dom.document
import org.scalajs.dom.html
import tyrian.Cmd

import scala.concurrent.Promise

object File:

  def select[F[_]: Async, Msg](fileTypes: List[String])(
      resultToMessage: dom.File => Msg
  ): Cmd[F, Msg] =
    selectFiles(fileTypes, false)(files => resultToMessage(files.head))

  def select[F[_]: Async, Msg](fileTypes: String*)(
      resultToMessage: dom.File => Msg
  ): Cmd[F, Msg] =
    selectFiles(fileTypes.toList, false)(files => resultToMessage(files.head))

  def selectMultiple[F[_]: Async, Msg](fileTypes: List[String])(
      resultToMessage: List[dom.File] => Msg
  ): Cmd[F, Msg] =
    selectFiles(fileTypes, true)(resultToMessage)

  def selectMultiple[F[_]: Async, Msg](fileTypes: String*)(
      resultToMessage: List[dom.File] => Msg
  ): Cmd[F, Msg] =
    selectFiles(fileTypes.toList, true)(resultToMessage)

  private def selectFiles[F[_]: Async, Msg](fileTypes: List[String], multiple: Boolean)(
      resultToMessage: List[dom.File] => Msg
  ): Cmd[F, Msg] =
    val task = Async[F].delay {
      val input = document.createElement("input").asInstanceOf[html.Input];
      val p     = Promise[List[dom.File]]()
      input.setAttribute("type", "file")
      input.setAttribute("accept", fileTypes.mkString(","))

      if multiple then input.setAttribute("multiple", "multiple")

      input.addEventListener(
        "change",
        (e: Event) =>
          e.target match {
            case elem: html.Input => if elem.files.length > 0 then p.success(elem.files.toList) else ()
            case _                => ()
          }
      )

      input.click();
      p.future
    }

    Cmd.Run(Async[F].fromFuture(task), resultToMessage)
