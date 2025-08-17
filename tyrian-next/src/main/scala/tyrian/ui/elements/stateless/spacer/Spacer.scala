package tyrian.ui.elements.stateless.spacer

import tyrian.next.GlobalMsg
import tyrian.ui.UIElement
import tyrian.ui.datatypes.Extent
import tyrian.ui.theme.Theme
import tyrian.ui.theme.ThemeOverride
import tyrian.ui.utils.Lens

enum Spacer extends UIElement[Spacer, Unit]:
  case Fixed(width: Option[Extent], height: Option[Extent], classNames: Set[String])
  case Fill(classNames: Set[String])

  def withClassNames(classes: Set[String]): Spacer =
    this match
      case s: Fixed =>
        s.copy(classNames = classes)

      case s: Fill =>
        s.copy(classNames = classes)

  val themeOverride: ThemeOverride[Unit]                    = ThemeOverride.NoOverride
  val themeLens: Lens[Theme.Default, Unit]                  = Lens.unit
  def withThemeOverride(value: ThemeOverride[Unit]): Spacer = this

  def view: Theme ?=> tyrian.Elem[GlobalMsg] =
    Spacer.View.view(this)

object Spacer:

  def gap: Spacer =
    Fixed(Some(Extent.px(16)), Some(Extent.px(16)), Set())

  def horizontal(width: Extent): Spacer =
    Fixed(Some(width), None, Set())

  def vertical(height: Extent): Spacer =
    Fixed(None, Some(height), Set())

  def fill: Spacer =
    Fill(Set())

  def empty: Spacer =
    fill

  object View:
    import tyrian.Html
    import tyrian.Html.*
    import tyrian.Style
    import tyrian.EmptyAttribute

    private def classAttr(classes: Set[String]) =
      if classes.isEmpty then EmptyAttribute else cls := classes.mkString(" ")

    def view(spacer: Spacer): Html[GlobalMsg] =
      spacer match
        case Fixed(width, height, classNames) =>
          val styles =
            (width, height) match
              case (None, None) =>
                EmptyAttribute

              case (Some(w), None) =>
                style := Style("width" -> w.toCSSValue)

              case (None, Some(h)) =>
                style := Style("height" -> h.toCSSValue)

              case (Some(w), Some(h)) =>
                style := Style("width" -> w.toCSSValue, "height" -> h.toCSSValue)

          div(classAttr(classNames), styles)()

        case Fill(classNames) =>
          div(classAttr(classNames), style(Style("flex" -> "1")))()
