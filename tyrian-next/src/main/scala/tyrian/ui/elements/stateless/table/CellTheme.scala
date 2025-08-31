package tyrian.ui.elements.stateless.table

import tyrian.Style
import tyrian.ui.datatypes.FontSize
import tyrian.ui.datatypes.FontWeight
import tyrian.ui.datatypes.Padding
import tyrian.ui.datatypes.RGBA

final case class CellTheme(
    background: Option[RGBA],
    fontWeight: Option[FontWeight],
    fontSize: Option[FontSize],
    padding: Option[Padding],
    textColor: Option[RGBA]
):

  def withBackground(color: RGBA): CellTheme =
    this.copy(background = Some(color))
  def noBackground: CellTheme =
    this.copy(background = None)

  def withFontWeight(weight: FontWeight): CellTheme =
    this.copy(fontWeight = Some(weight))
  def defaultFontWeight: CellTheme =
    this.copy(fontWeight = None)

  def withFontSize(size: FontSize): CellTheme =
    this.copy(fontSize = Some(size))
  def defaultFontSize: CellTheme =
    this.copy(fontSize = None)

  def withPadding(padding: Padding): CellTheme =
    this.copy(padding = Some(padding))
  def defaultPadding: CellTheme =
    this.copy(padding = None)

  def withTextColor(color: RGBA): CellTheme =
    this.copy(textColor = Some(color))
  def defaultTextColor: CellTheme =
    this.copy(textColor = None)

  def toStyle: Option[Style] =
    val baseStyles =
      for {
        fw <- fontWeight
        fs <- fontSize
      } yield Style(
        "font-weight" -> fw.toCSSValue,
        "font-size"   -> fs.toCSSValue
      )

    for {
      backgroundStyle <- background.map("background-color" -> _.toCSSValue)
      textColorStyle  <- textColor.map("color" -> _.toCSSValue)
      base            <- baseStyles
      p               <- padding.map(_.toStyle)
    } yield base |+| p |+| Style(backgroundStyle, textColorStyle)

object CellTheme:

  object Defaults:

    val header: CellTheme =
      CellTheme(
        background = Some(RGBA.fromHex("#f5f5f5")),
        fontWeight = Some(FontWeight.Bold),
        fontSize = Some(FontSize.Large),
        padding = None,
        textColor = Some(RGBA.fromHex("#333333"))
      )

    val cell: CellTheme =
      CellTheme(
        background = None,
        fontWeight = Some(FontWeight.Normal),
        fontSize = Some(FontSize.Medium),
        padding = None,
        textColor = Some(RGBA.fromHex("#333333"))
      )
