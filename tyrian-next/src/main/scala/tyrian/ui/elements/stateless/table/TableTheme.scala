package tyrian.ui.elements.stateless.table

import tyrian.Style
import tyrian.ui.BorderWidth
import tyrian.ui.datatypes.Border
import tyrian.ui.datatypes.BorderCollapse
import tyrian.ui.datatypes.RGBA

final case class TableTheme(
    background: Option[RGBA],
    border: Option[Border],
    borderCollapse: BorderCollapse,
    row: RowTheme,
    header: CellTheme,
    cell: CellTheme
):
  def withBackground(color: RGBA): TableTheme =
    this.copy(background = Some(color))
  def noBackground: TableTheme =
    this.copy(background = None)

  def withBorder(border: Border): TableTheme =
    this.copy(border = Some(border))
  def noBorder: TableTheme =
    this.copy(border = None)

  def withRowTheme(theme: RowTheme): TableTheme =
    this.copy(row = theme)
  def modifyRowTheme(f: RowTheme => RowTheme): TableTheme =
    this.copy(row = f(this.row))

  def withHeaderTheme(theme: CellTheme): TableTheme =
    this.copy(header = theme)
  def modifyHeaderTheme(f: CellTheme => CellTheme): TableTheme =
    this.copy(header = f(this.header))

  def withCellTheme(theme: CellTheme): TableTheme =
    this.copy(cell = theme)
  def modifyCellTheme(f: CellTheme => CellTheme): TableTheme =
    this.copy(cell = f(this.cell))

  def toTableStyles: Style =
    val baseStyles =
      for {
        backgroundStyle <- background.map("background-color" -> _.toCSSValue)
        borderStyle     <- border.map(_.toStyle)
      } yield Style(
        "border-collapse" -> borderCollapse.toCSSValue,
        "width"           -> "100%",
        "overflow"        -> "hidden",
        backgroundStyle
      ) |+| borderStyle

    baseStyles.getOrElse(Style.empty)

  def toRowStyles(isAlternate: Boolean): Style =
    if isAlternate then
      row.alternative
        .orElse(row.background)
        .map(bg => Style("background-color" -> bg.toCSSValue))
        .getOrElse(Style.empty)
    else
      row.background
        .map(bg => Style("background-color" -> bg.toCSSValue))
        .getOrElse(Style.empty)

  def toHeaderStyles: Option[Style] =
    header.toStyle

  def toCellStyles: Option[Style] =
    cell.toStyle

object TableTheme:

  val default: TableTheme =
    TableTheme(
      background = Some(RGBA.fromHex("#ffffff")),
      border = Some(Border.solid(BorderWidth.px(1), RGBA.fromHex("#333333")).rounded),
      borderCollapse = BorderCollapse.Separate,
      row = RowTheme.default,
      header = CellTheme.Defaults.header,
      cell = CellTheme.Defaults.cell
    )
