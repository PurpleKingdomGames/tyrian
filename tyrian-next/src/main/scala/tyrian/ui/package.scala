package tyrian.ui

// ---- Data Types ----

type SpaceAlignment = datatypes.SpaceAlignment
val SpaceAlignment: datatypes.SpaceAlignment.type = datatypes.SpaceAlignment

type Justify = datatypes.Justify
val Justify: datatypes.Justify.type = datatypes.Justify

type Align = datatypes.Align
val Align: datatypes.Align.type = datatypes.Align

type FontFamily = datatypes.FontFamily
val FontFamily: datatypes.FontFamily.type = datatypes.FontFamily

type FontName = datatypes.FontName
val FontName: datatypes.FontName.type = datatypes.FontName

type FontStack = datatypes.FontStack
val FontStack: datatypes.FontStack.type = datatypes.FontStack

type FontSize = datatypes.FontSize
val FontSize: datatypes.FontSize.type = datatypes.FontSize

type FontWeight = datatypes.FontWeight
val FontWeight: datatypes.FontWeight.type = datatypes.FontWeight

type LayoutDirection = datatypes.LayoutDirection
val LayoutDirection: datatypes.LayoutDirection.type = datatypes.LayoutDirection

type LineHeight = datatypes.LineHeight
val LineHeight: datatypes.LineHeight.type = datatypes.LineHeight

type Ratio = datatypes.Ratio
val Ratio: datatypes.Ratio.type = datatypes.Ratio

type RGB = datatypes.RGB
val RGB: datatypes.RGB.type = datatypes.RGB

type RGBA = datatypes.RGBA
val RGBA: datatypes.RGBA.type = datatypes.RGBA

type Spacing = datatypes.Spacing
val Spacing: datatypes.Spacing.type = datatypes.Spacing

type TextAlignment = datatypes.TextAlignment
val TextAlignment: datatypes.TextAlignment.type = datatypes.TextAlignment

type TextDecoration = datatypes.TextDecoration
val TextDecoration: datatypes.TextDecoration.type = datatypes.TextDecoration

type TextStyle = datatypes.TextStyle
val TextStyle: datatypes.TextStyle.type = datatypes.TextStyle

// ---- Layout ----

val Column: layout.Column.type = layout.Column

type Container = layout.Container
val Container: layout.Container.type = layout.Container

type ContainerTheme = layout.ContainerTheme
val ContainerTheme: layout.ContainerTheme.type = layout.ContainerTheme

type Layout = layout.Layout
val Layout: layout.Layout.type = layout.Layout

val Row: layout.Row.type = layout.Row

// ---- Stateless elements ----

// HTML

type HtmlElement = elements.stateless.html.HtmlElement
val HtmlElement: elements.stateless.html.HtmlElement.type = elements.stateless.html.HtmlElement

// Image

type Image = elements.stateless.image.Image
val Image: elements.stateless.image.Image.type = elements.stateless.image.Image

type ObjectFit = datatypes.ImageFit
val ObjectFit: datatypes.ImageFit.type = datatypes.ImageFit

type Border = datatypes.Border
val Border: datatypes.Border.type = datatypes.Border

type BorderRadius = datatypes.BorderRadius
val BorderRadius: datatypes.BorderRadius.type = datatypes.BorderRadius

type BorderStyle = datatypes.BorderStyle
val BorderStyle: datatypes.BorderStyle.type = datatypes.BorderStyle

type BorderWidth = datatypes.BorderWidth
val BorderWidth: datatypes.BorderWidth.type = datatypes.BorderWidth

type BoxShadow = datatypes.BoxShadow
val BoxShadow: datatypes.BoxShadow.type = datatypes.BoxShadow

type Extent = datatypes.Extent
val Extent: datatypes.Extent.type = datatypes.Extent

type Opacity = datatypes.Opacity
val Opacity: datatypes.Opacity.type = datatypes.Opacity

// Link

type Link = elements.stateless.link.Link
val Link: elements.stateless.link.Link.type = elements.stateless.link.Link

type LinkTheme = elements.stateless.link.LinkTheme
val LinkTheme: elements.stateless.link.LinkTheme.type = elements.stateless.link.LinkTheme

type Target = datatypes.Target
val Target: datatypes.Target.type = datatypes.Target

// Text

type TextBlock = elements.stateless.text.TextBlock
val TextBlock: elements.stateless.text.TextBlock.type = elements.stateless.text.TextBlock

type TextTheme = elements.stateless.text.TextTheme
val TextTheme: elements.stateless.text.TextTheme.type = elements.stateless.text.TextTheme

type TextThemes = elements.stateless.text.TextThemes
val TextThemes: elements.stateless.text.TextThemes.type = elements.stateless.text.TextThemes

type TextVariant = elements.stateless.text.TextVariant
val TextVariant: elements.stateless.text.TextVariant.type = elements.stateless.text.TextVariant

// ---- Stateful elements ----

// Input

type Input = elements.stateful.input.Input
val Input: elements.stateful.input.Input.type = elements.stateful.input.Input

type InputTheme = elements.stateful.input.InputTheme
val InputTheme: elements.stateful.input.InputTheme.type = elements.stateful.input.InputTheme

// ---- Themes ----

type Theme = theme.Theme
val Theme: theme.Theme.type = theme.Theme

type ThemeColors = theme.ThemeColors
val ThemeColors: theme.ThemeColors.type = theme.ThemeColors

type ThemeFonts = theme.ThemeFonts
val ThemeFonts: theme.ThemeFonts.type = theme.ThemeFonts

type ThemeOverride = theme.ThemeOverride
val ThemeOverride: theme.ThemeOverride.type = theme.ThemeOverride

// ---- Utils ----

type Lens = utils.Lens
val Lens: utils.Lens.type = utils.Lens
