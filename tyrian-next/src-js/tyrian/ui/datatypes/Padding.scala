package tyrian.ui.datatypes

import tyrian.Style

final case class Padding(top: Spacing, right: Spacing, bottom: Spacing, left: Spacing):

  def withTop(value: Spacing): Padding =
    this.copy(top = value)

  def withRight(value: Spacing): Padding =
    this.copy(right = value)

  def withBottom(value: Spacing): Padding =
    this.copy(bottom = value)

  def withLeft(value: Spacing): Padding =
    this.copy(left = value)

  def withVertical(value: Spacing): Padding =
    this.copy(top = value, bottom = value)

  def withHorizontal(value: Spacing): Padding =
    this.copy(right = value, left = value)

  def toStyle: Style =
    Style(
      "padding" -> s"${top.toCSSValue} ${right.toCSSValue} ${bottom.toCSSValue} ${left.toCSSValue}"
    )

object Padding:

  def zero: Padding =
    Padding(Spacing.zero, Spacing.zero, Spacing.zero, Spacing.zero)
  def default: Padding =
    zero

  def apply(value: Spacing): Padding =
    Padding(value, value, value, value)

  def vertical(value: Spacing): Padding =
    Padding(value, Spacing.zero, value, Spacing.zero)

  def horizontal(value: Spacing): Padding =
    Padding(Spacing.zero, value, Spacing.zero, value)

  def topLeft(value: Spacing): Padding =
    Padding(value, Spacing.zero, Spacing.zero, value)

  def topRight(value: Spacing): Padding =
    Padding(value, value, Spacing.zero, Spacing.zero)

  def bottomLeft(value: Spacing): Padding =
    Padding(Spacing.zero, Spacing.zero, value, value)

  def bottomRight(value: Spacing): Padding =
    Padding(Spacing.zero, value, value, Spacing.zero)

  def left(value: Spacing): Padding =
    Padding(Spacing.zero, Spacing.zero, Spacing.zero, value)

  def right(value: Spacing): Padding =
    Padding(Spacing.zero, value, Spacing.zero, Spacing.zero)

  def top(value: Spacing): Padding =
    Padding(value, Spacing.zero, Spacing.zero, Spacing.zero)

  def bottom(value: Spacing): Padding =
    Padding(Spacing.zero, Spacing.zero, value, Spacing.zero)

  val XSmall: Padding =
    Padding(Spacing.XSmall, Spacing.XSmall, Spacing.XSmall, Spacing.XSmall)
  val Small: Padding =
    Padding(Spacing.Small, Spacing.Small, Spacing.Small, Spacing.Small)
  val Medium: Padding =
    Padding(Spacing.Medium, Spacing.Medium, Spacing.Medium, Spacing.Medium)
  val Large: Padding =
    Padding(Spacing.Large, Spacing.Large, Spacing.Large, Spacing.Large)
  val XLarge: Padding =
    Padding(Spacing.XLarge, Spacing.XLarge, Spacing.XLarge, Spacing.XLarge)
  val XXLarge: Padding =
    Padding(Spacing.XXLarge, Spacing.XXLarge, Spacing.XXLarge, Spacing.XXLarge)
