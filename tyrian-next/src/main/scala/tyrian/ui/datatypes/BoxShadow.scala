package tyrian.ui.datatypes

import tyrian.Style

final case class BoxShadow(
    offsetX: Extent,
    offsetY: Extent,
    blurRadius: Option[Extent],
    spreadRadius: Option[Extent],
    color: RGBA,
    isInset: Boolean
) derives CanEqual:

  def withOffsetX(offsetX: Extent): BoxShadow =
    this.copy(offsetX = offsetX)

  def withOffsetY(offsetY: Extent): BoxShadow =
    this.copy(offsetY = offsetY)

  def withOffset(offsetX: Extent, offsetY: Extent): BoxShadow =
    this.copy(offsetX = offsetX, offsetY = offsetY)

  def withBlurRadius(blurRadius: Extent): BoxShadow =
    this.copy(blurRadius = Some(blurRadius))

  def withSpreadRadius(spreadRadius: Extent): BoxShadow =
    this.copy(spreadRadius = Some(spreadRadius))

  def withColor(color: RGBA): BoxShadow =
    this.copy(color = color)

  def withInset(inset: Boolean): BoxShadow =
    this.copy(isInset = inset)

  def asInset: BoxShadow  = withInset(true)
  def asOutset: BoxShadow = withInset(false)

  def toCSSValue: String =
    val insetPart  = if isInset then "inset " else ""
    val offsetPart = s"${offsetX.toCSSValue} ${offsetY.toCSSValue}"
    val blurPart   = blurRadius.map(b => s" ${b.toCSSValue}").getOrElse("")
    val spreadPart = spreadRadius.map(s => s" ${s.toCSSValue}").getOrElse("")
    val colorPart  = s" ${color.toCSSValue}"

    s"$insetPart$offsetPart$blurPart$spreadPart$colorPart"

  def toStyle: Style =
    Style("box-shadow", toCSSValue)

object BoxShadow:

  val none: BoxShadow =
    BoxShadow(
      offsetX = Extent.Relative(0),
      offsetY = Extent.Relative(0),
      blurRadius = None,
      spreadRadius = None,
      color = RGBA.None,
      isInset = false
    )

  def apply(offsetX: Extent, offsetY: Extent, color: RGBA): BoxShadow =
    BoxShadow(offsetX, offsetY, None, None, color, false)

  def apply(offsetX: Extent, offsetY: Extent, blurRadius: Extent, color: RGBA): BoxShadow =
    BoxShadow(offsetX, offsetY, Some(blurRadius), None, color, false)

  def apply(offsetX: Extent, offsetY: Extent, blurRadius: Extent, spreadRadius: Extent, color: RGBA): BoxShadow =
    BoxShadow(offsetX, offsetY, Some(blurRadius), Some(spreadRadius), color, false)

  def small(color: RGBA): BoxShadow =
    BoxShadow(
      Extent.Relative(0),
      Extent.Relative(0.0625),
      Some(Extent.Relative(0.1875)),
      Some(Extent.Relative(0)),
      color,
      false
    )
  val small: BoxShadow =
    small(RGBA.Black)

  def medium(color: RGBA): BoxShadow =
    BoxShadow(
      Extent.Relative(0),
      Extent.Relative(0.125),
      Some(Extent.Relative(0.25)),
      Some(Extent.Relative(0)),
      color,
      false
    )
  val medium: BoxShadow =
    medium(RGBA.Black)

  def large(color: RGBA): BoxShadow =
    BoxShadow(
      Extent.Relative(0),
      Extent.Relative(0.25),
      Some(Extent.Relative(0.5)),
      Some(Extent.Relative(0)),
      color,
      false
    )
  val large: BoxShadow =
    large(RGBA.Black)

  def extraLarge(color: RGBA): BoxShadow =
    BoxShadow(
      Extent.Relative(0),
      Extent.Relative(0.5),
      Some(Extent.Relative(1)),
      Some(Extent.Relative(0)),
      color,
      false
    )
  val extraLarge: BoxShadow =
    extraLarge(RGBA.Black)
