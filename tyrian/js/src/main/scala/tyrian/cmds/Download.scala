package tyrian.cmds

import cats.effect.kernel.Sync
import org.scalajs.dom.document
import org.scalajs.dom.html
import tyrian.Cmd

import java.util.Base64

object Download:
  def fromByteArray[F[_]: Sync](fileName: String, mimeType: String, data: Array[Byte]): Cmd.SideEffect[F] =
    fromBase64String(fileName, mimeType, Base64.getEncoder().encodeToString(data))

  def fromString[F[_]: Sync](fileName: String, mimeType: String, data: String): Cmd.SideEffect[F] =
    fromByteArray(fileName, mimeType, data.getBytes())

  def fromBase64String[F[_]: Sync](fileName: String, mimeType: String, data: String): Cmd.SideEffect[F] =
    Cmd.SideEffect {
      val link = document.createElement("a").asInstanceOf[html.Anchor];
      link.setAttribute("download", fileName)
      link.href = s"""data:${mimeType};base64,${data}"""

      link.click();
    }
