package tyrian.ui.datatypes

import tyrian.Style

final case class BackgroundMode(size: BackgroundSize, repeat: BackgroundRepeat) derives CanEqual:
  def withSize(s: BackgroundSize): BackgroundMode =
    this.copy(size = s)

  def withRepeat(r: BackgroundRepeat): BackgroundMode =
    this.copy(repeat = r)

  def toStyle: Style =
    Style(
      "background-repeat" -> repeat.toCSSValue,
      "background-size"   -> size.toCSSValue
    )

object BackgroundMode:

  val default: BackgroundMode =
    BackgroundMode(BackgroundSize.Cover, BackgroundRepeat.NoRepeat)

  val coverNoRepeat: BackgroundMode =
    BackgroundMode(BackgroundSize.Cover, BackgroundRepeat.NoRepeat)

  val containNoRepeat: BackgroundMode =
    BackgroundMode(BackgroundSize.Contain, BackgroundRepeat.NoRepeat)

  val fillNoRepeat: BackgroundMode =
    BackgroundMode(BackgroundSize.Fill, BackgroundRepeat.NoRepeat)

  val autoNoRepeat: BackgroundMode =
    BackgroundMode(BackgroundSize.Auto, BackgroundRepeat.NoRepeat)

  val autoRepeat: BackgroundMode =
    BackgroundMode(BackgroundSize.Auto, BackgroundRepeat.Repeat)

  val autoRepeatX: BackgroundMode =
    BackgroundMode(BackgroundSize.Auto, BackgroundRepeat.RepeatX)

  val autoRepeatY: BackgroundMode =
    BackgroundMode(BackgroundSize.Auto, BackgroundRepeat.RepeatY)
