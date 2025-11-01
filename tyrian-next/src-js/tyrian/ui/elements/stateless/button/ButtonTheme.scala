package tyrian.ui.elements.stateless.button

import tyrian.Style
import tyrian.ui.elements.stateless.text.TextTheme
import tyrian.ui.layout.ContainerTheme
import tyrian.ui.theme.Theme

final case class ButtonTheme(
    container: Option[ContainerTheme],
    text: Option[TextTheme]
):

  def withContainerTheme(container: ContainerTheme): ButtonTheme =
    this.copy(container = Some(container))
  def clearContainerTheme: ButtonTheme =
    this.copy(container = None)

  def withTextTheme(text: TextTheme): ButtonTheme =
    this.copy(text = Some(text))
  def clearTextTheme: ButtonTheme =
    this.copy(text = None)

  def toStyles(theme: Theme): Style =
    theme match
      case Theme.None =>
        Style.empty

      case t: Theme.Default =>
        container.fold(Style.empty)(_.toStyle) |+|
          text.fold(Style.empty)(_.toStyles(theme))

object ButtonTheme:

  val default: ButtonTheme =
    ButtonTheme(None, None)
