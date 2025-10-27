package tyrian.ui.elements.stateless.spacer

import tyrian.next.GlobalMsg
import tyrian.ui.UIElement
import tyrian.ui.datatypes.Extent
import tyrian.ui.theme.Theme
import tyrian.ui.theme.ThemeOverride
import tyrian.ui.utils.Lens

enum Spacer extends UIElement[Spacer, Unit]:
  case Fixed(width: Option[Extent], height: Option[Extent], classNames: Set[String], id: Option[String])
  case Fill(classNames: Set[String], id: Option[String])

  def withClassNames(classes: Set[String]): Spacer =
    this match
      case s: Fixed =>
        s.copy(classNames = classes)

      case s: Fill =>
        s.copy(classNames = classes)

  def withId(id: String): Spacer =
    this match
      case s: Fixed =>
        s.copy(id = Some(id))

      case s: Fill =>
        s.copy(id = Some(id))

  val themeOverride: ThemeOverride[Unit]                    = ThemeOverride.NoOverride
  val themeLens: Lens[Theme.Default, Unit]                  = Lens.unit
  def withThemeOverride(value: ThemeOverride[Unit]): Spacer = this

  def view: Theme ?=> tyrian.Elem[GlobalMsg] =
    Spacer.View.view(this)

object Spacer:

  def gap: Spacer =
    Fixed(Some(Extent.px(16)), Some(Extent.px(16)), Set(), None)

  def horizontal(width: Extent): Spacer =
    Fixed(Some(width), None, Set(), None)

  def vertical(height: Extent): Spacer =
    Fixed(None, Some(height), Set(), None)

  def fill: Spacer =
    Fill(Set(), None)

  def empty: Spacer =
    fill

  object View:
    import tyrian.Html
    import tyrian.Html.*
    import tyrian.Style
    import tyrian.EmptyAttribute

    private def idAttr(_id: Option[String]) =
      _id match
        case None        => EmptyAttribute
        case Some(value) => id := value

    private def classAttr(classes: Set[String]) =
      if classes.isEmpty then EmptyAttribute else cls := classes.mkString(" ")

    def view(spacer: Spacer): Html[GlobalMsg] =
      spacer match
        case Fixed(width, height, classNames, id) =>
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

          div(idAttr(id), classAttr(classNames), styles)()

        case Fill(classNames, id) =>
          div(idAttr(id), classAttr(classNames), style(Style("flex" -> "1")))()
