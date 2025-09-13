package tyrian.ui.theme

import tyrian.ui.elements.stateful.input.InputTheme
import tyrian.ui.elements.stateless.link.LinkTheme
import tyrian.ui.elements.stateless.table.TableTheme
import tyrian.ui.elements.stateless.text.TextThemes
import tyrian.ui.elements.stateful.button.ButtonTheme
import tyrian.ui.layout.ContainerTheme

final case class ElementThemes(
    button: ButtonTheme,
    container: ContainerTheme,
    image: ContainerTheme,
    input: InputTheme,
    link: LinkTheme,
    table: TableTheme,
    text: TextThemes
):

  def withButtonTheme(value: ButtonTheme): ElementThemes =
    this.copy(button = value)
  def modifyButtonTheme(f: ButtonTheme => ButtonTheme): ElementThemes =
    withButtonTheme(f(button))

  def withContainerTheme(value: ContainerTheme): ElementThemes =
    this.copy(container = value)
  def modifyContainerTheme(f: ContainerTheme => ContainerTheme): ElementThemes =
    withContainerTheme(f(container))

  def withInputTheme(value: InputTheme): ElementThemes =
    this.copy(input = value)
  def modifyInputTheme(f: InputTheme => InputTheme): ElementThemes =
    withInputTheme(f(input))

  def withImageTheme(value: ContainerTheme): ElementThemes =
    this.copy(image = value)
  def modifyImageTheme(f: ContainerTheme => ContainerTheme): ElementThemes =
    withImageTheme(f(image))

  def withLinkTheme(value: LinkTheme): ElementThemes =
    this.copy(link = value)
  def modifyLinkTheme(f: LinkTheme => LinkTheme): ElementThemes =
    withLinkTheme(f(link))

  def withTableTheme(value: TableTheme): ElementThemes =
    this.copy(table = value)
  def modifyTableTheme(f: TableTheme => TableTheme): ElementThemes =
    withTableTheme(f(table))

  def withTextThemes(value: TextThemes): ElementThemes =
    this.copy(text = value)
  def modifyTextThemes(f: TextThemes => TextThemes): ElementThemes =
    withTextThemes(f(text))

object ElementThemes:

  val default: ElementThemes =
    ElementThemes(
      ButtonTheme.default,
      ContainerTheme.default,
      ContainerTheme.default,
      InputTheme.default,
      LinkTheme.default,
      TableTheme.default,
      TextThemes.default
    )
