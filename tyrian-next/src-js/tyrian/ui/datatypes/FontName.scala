package tyrian.ui.datatypes

final case class FontName private (name: String, safeName: String):
  def toCSSValue: String =
    safeName

object FontName:

  def apply(name: String): FontName =
    FontName(name, makeSafe(name))

  private def makeSafe(s: String): String =
    val hasNonStandardChars = """^[a-zA-Z0-9\-]+$""".r
    if hasNonStandardChars.findFirstIn(s).isEmpty then s"'${s.replace("'", "\\'")}'"
    else s

  // Sans-serif
  val systemUi: FontName        = FontName("system-ui")
  val uiSansSerif: FontName     = FontName("ui-sans-serif")
  val appleSystem: FontName     = FontName("-apple-system") // macOS / iOS
  val segoeUiVariable: FontName = FontName("Segoe UI Variable")
  val segoeUi: FontName         = FontName("Segoe UI")      // Windows
  val roboto: FontName          = FontName("Roboto")        // Android / ChromeOS
  val ubuntu: FontName          = FontName("Ubuntu")
  val cantarell: FontName       = FontName("Cantarell")
  val notoSans: FontName        = FontName("Noto Sans")
  val helveticaNeue: FontName   = FontName("Helvetica Neue")
  val helvetica: FontName       = FontName("Helvetica")     // Older macOS
  val liberationSans: FontName  = FontName("Liberation Sans")
  val arial: FontName           = FontName("Arial")
  val appleColorEmoji: FontName = FontName("Apple Color Emoji")
  val segoeUiEmoji: FontName    = FontName("Segoe UI Emoji")
  val notoColorEmoji: FontName  = FontName("Noto Color Emoji")
  val sansSerif: FontName       = FontName("sans-serif")

  // Serif
  val uiSerif: FontName         = FontName("ui-serif")
  val georgia: FontName         = FontName("Georgia")
  val cambria: FontName         = FontName("Cambria")
  val timesNewRoman: FontName   = FontName("Times New Roman")
  val times: FontName           = FontName("Times")
  val notoSerif: FontName       = FontName("Noto Serif")
  val liberationSerif: FontName = FontName("Liberation Serif")
  val serif: FontName           = FontName("serif")

  // Monospace
  val uiMonospace: FontName    = FontName("ui-monospace")
  val sfMonoRegular: FontName  = FontName("SFMono-Regular") /* macOS */
  val menlo: FontName          = FontName("Menlo")
  val monaco: FontName         = FontName("Monaco")
  val consolas: FontName       = FontName("Consolas")
  val cascadiaMono: FontName   = FontName("Cascadia Mono")
  val liberationMono: FontName = FontName("Liberation Mono")
  val dejavuSansMono: FontName = FontName("DejaVu Sans Mono")
  val notoMono: FontName       = FontName("Noto Mono")
  val courierNew: FontName     = FontName("Courier New")
  val monospace: FontName      = FontName("monospace")
