package tyrian.ui.datatypes

final case class FontStack(primary: FontName, fallbacks: List[FontName]):
  def toList: List[FontName] =
    primary :: fallbacks

  def toCSSValue: String =
    (primary :: fallbacks).map(_.toCSSValue).mkString(", ")

object FontStack:
  def apply(primary: FontName): FontStack =
    FontStack(primary, Nil)

  def apply(primary: FontName, fallbacks: FontName*): FontStack =
    FontStack(primary, fallbacks.toList)

  val sansSerif: FontStack =
    FontStack(
      FontName.systemUi,
      FontName.uiSansSerif,
      FontName.appleSystem,
      FontName.segoeUiVariable,
      FontName.segoeUi,
      FontName.roboto,
      FontName.ubuntu,
      FontName.cantarell,
      FontName.notoSans,
      FontName.helveticaNeue,
      FontName.arial,
      FontName.appleColorEmoji,
      FontName.segoeUiEmoji,
      FontName.notoColorEmoji,
      FontName.sansSerif
    )

  val serif: FontStack =
    FontStack(
      FontName.uiSerif,
      FontName.georgia,
      FontName.cambria,
      FontName.timesNewRoman,
      FontName.times,
      FontName.notoSerif,
      FontName.liberationSerif,
      FontName.serif
    )

  val monospace: FontStack =
    FontStack(
      FontName.uiMonospace,
      FontName.sfMonoRegular,
      FontName.menlo,
      FontName.monaco,
      FontName.consolas,
      FontName.cascadiaMono,
      FontName.liberationMono,
      FontName.dejavuSansMono,
      FontName.notoMono,
      FontName.courierNew,
      FontName.monospace
    )

/* Sans-serif
font-family:
  system-ui,
  -apple-system, /* macOS / iOS */
  "Segoe UI",    /* Windows */
  Roboto,        /* Android / ChromeOS */
  Ubuntu,
  "Noto Sans",
  "Helvetica Neue",
  Arial,
  "Apple Color Emoji", "Segoe UI Emoji", "Noto Color Emoji",
  sans-serif;
 */

/*Serif
font-family:
  ui-serif,
  Georgia,
  Cambria,
  "Times New Roman",
  Times,
  "Noto Serif",
  serif;
 */

/*Monospace
font-family:
  ui-monospace,
  SFMono-Regular, /* macOS */
  Menlo,
  Monaco,
  Consolas,
  "Liberation Mono",
  "DejaVu Sans Mono",
  "Noto Mono",
  "Courier New",
  monospace;
 */
