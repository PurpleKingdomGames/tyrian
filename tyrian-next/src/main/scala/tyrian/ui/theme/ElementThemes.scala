package tyrian.ui.theme

import tyrian.ui.elements.stateful.input.InputTheme
import tyrian.ui.elements.stateless.link.LinkTheme
import tyrian.ui.elements.stateless.table.TableTheme
import tyrian.ui.elements.stateless.text.TextThemes
import tyrian.ui.layout.*

final case class ElementThemes(
    canvas: ContainerTheme,
    container: ContainerTheme,
    image: ContainerTheme,
    input: InputTheme,
    link: LinkTheme,
    table: TableTheme,
    text: TextThemes
):

  def withCanvasTheme(canvas: ContainerTheme): ElementThemes =
    this.copy(canvas = canvas)

  def withContainerTheme(container: ContainerTheme): ElementThemes =
    this.copy(container = container)

  def withImageTheme(image: ContainerTheme): ElementThemes =
    this.copy(image = image)

  def withInputTheme(input: InputTheme): ElementThemes =
    this.copy(input = input)

  def withLinkTheme(link: LinkTheme): ElementThemes =
    this.copy(link = link)

  def withTableTheme(table: TableTheme): ElementThemes =
    this.copy(table = table)

  def withTextTheme(text: TextThemes): ElementThemes =
    this.copy(text = text)

  def withTextThemes(text: TextThemes): ElementThemes =
    this.copy(text = text)

object ElementThemes:
  val default: ElementThemes =
    ElementThemes(
      canvas = ContainerTheme.default,
      container = ContainerTheme.default,
      image = ContainerTheme.default,
      input = InputTheme.default,
      link = LinkTheme.default,
      table = TableTheme.default,
      text = TextThemes.default
    )
