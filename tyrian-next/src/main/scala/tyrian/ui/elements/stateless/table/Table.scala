package tyrian.ui.elements.stateless.table

import tyrian.next.GlobalMsg
import tyrian.ui.UIElement
import tyrian.ui.datatypes.DataSet
import tyrian.ui.theme.Theme
import tyrian.ui.theme.ThemeOverride
import tyrian.ui.utils.Lens

final case class Table(
    dataset: DataSet[?],
    classNames: Set[String],
    themeOverride: ThemeOverride[TableTheme]
) extends UIElement[Table, TableTheme]:

  def withDataSet(newDataSet: DataSet[?]): Table =
    this.copy(dataset = newDataSet)

  def withClassNames(classes: Set[String]): Table =
    this.copy(classNames = classes)

  def themeLens: Lens[Theme.Default, TableTheme] =
    Lens(
      _.table,
      (t, table) => t.copy(table = table)
    )

  def withThemeOverride(value: ThemeOverride[TableTheme]): Table =
    this.copy(themeOverride = value)

  def view: Theme ?=> tyrian.Elem[GlobalMsg] =
    Table.View.toHtml(this)

object Table:

  def apply(dataset: DataSet[?]): Table =
    Table(dataset, Set(), ThemeOverride.NoOverride)

  object View:

    import tyrian.Html.*
    import tyrian.EmptyAttribute

    // TODO: Should use the main theme font.
    // TODO: More border control at the row / cell level?
    def toHtml(table: Table): Theme ?=> tyrian.Elem[GlobalMsg] =
      val theme = summon[Theme]

      val classAttribute =
        if table.classNames.isEmpty then EmptyAttribute
        else cls := table.classNames.mkString(" ")

      val tableStyleAttribute =
        theme match
          case Theme.None =>
            EmptyAttribute

          case t: Theme.Default =>
            style(t.table.toTableStyles)

      val headerStyleAttribute =
        theme match
          case Theme.None =>
            EmptyAttribute

          case t: Theme.Default =>
            t.table.toHeaderStyles match
              case Some(s) => style(s)
              case None    => EmptyAttribute

      val cellStyleAttribute =
        theme match
          case Theme.None =>
            EmptyAttribute

          case t: Theme.Default =>
            t.table.toCellStyles match
              case Some(s) => style(s)
              case None    => EmptyAttribute

      val headerRow =
        thead(headerStyleAttribute)(
          tr(
            table.dataset.headers.map(v => th(v)).toList
          )
        )

      val bodyRows =
        tbody(
          table.dataset.rows.zipWithIndex.map { case (row, index) =>
            val rowStyles =
              theme match
                case Theme.None =>
                  EmptyAttribute

                case t: Theme.Default =>
                  style(t.table.toRowStyles(index % 2 == 1))

            tr(rowStyles)(
              row.map { cellData =>
                td(cellStyleAttribute)(cellData)
              }.toList
            )
          }.toList
        )

      tyrian.Html.table(
        tableStyleAttribute,
        classAttribute
      )(
        headerRow,
        bodyRows
      )
