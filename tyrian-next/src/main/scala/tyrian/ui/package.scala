package tyrian.ui

// ---- Data Types ----

type Align = datatypes.Align
val Align: datatypes.Align.type = datatypes.Align

type BackgroundMode = datatypes.BackgroundMode
val BackgroundMode: datatypes.BackgroundMode.type = datatypes.BackgroundMode

type BackgroundRepeat = datatypes.BackgroundRepeat
val BackgroundRepeat: datatypes.BackgroundRepeat.type = datatypes.BackgroundRepeat

type BackgroundSize = datatypes.BackgroundSize
val BackgroundSize: datatypes.BackgroundSize.type = datatypes.BackgroundSize

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

type Degrees = datatypes.Degrees
val Degrees: datatypes.Degrees.type = datatypes.Degrees

type Extent = datatypes.Extent
val Extent: datatypes.Extent.type = datatypes.Extent

type Fill = datatypes.Fill
val Fill: datatypes.Fill.type = datatypes.Fill

type FontFamily = datatypes.FontFamily
val FontFamily: datatypes.FontFamily.type = datatypes.FontFamily

type FontName = datatypes.FontName
val FontName: datatypes.FontName.type = datatypes.FontName

type FontSize = datatypes.FontSize
val FontSize: datatypes.FontSize.type = datatypes.FontSize

type FontStack = datatypes.FontStack
val FontStack: datatypes.FontStack.type = datatypes.FontStack

type FontWeight = datatypes.FontWeight
val FontWeight: datatypes.FontWeight.type = datatypes.FontWeight

type ImageFit = datatypes.ImageFit
val ImageFit: datatypes.ImageFit.type = datatypes.ImageFit

type Justify = datatypes.Justify
val Justify: datatypes.Justify.type = datatypes.Justify

type LayoutDirection = datatypes.LayoutDirection
val LayoutDirection: datatypes.LayoutDirection.type = datatypes.LayoutDirection

type LineHeight = datatypes.LineHeight
val LineHeight: datatypes.LineHeight.type = datatypes.LineHeight

type Opacity = datatypes.Opacity
val Opacity: datatypes.Opacity.type = datatypes.Opacity

type Padding = datatypes.Padding
val Padding: datatypes.Padding.type = datatypes.Padding

type Position = datatypes.Position
val Position: datatypes.Position.type = datatypes.Position

type Radians = datatypes.Radians
val Radians: datatypes.Radians.type = datatypes.Radians

type Ratio = datatypes.Ratio
val Ratio: datatypes.Ratio.type = datatypes.Ratio

type RGB = datatypes.RGB
val RGB: datatypes.RGB.type = datatypes.RGB

type RGBA = datatypes.RGBA
val RGBA: datatypes.RGBA.type = datatypes.RGBA

type SpaceAlignment = datatypes.SpaceAlignment
val SpaceAlignment: datatypes.SpaceAlignment.type = datatypes.SpaceAlignment

type Spacing = datatypes.Spacing
val Spacing: datatypes.Spacing.type = datatypes.Spacing

type Target = datatypes.Target
val Target: datatypes.Target.type = datatypes.Target

type TextAlignment = datatypes.TextAlignment
val TextAlignment: datatypes.TextAlignment.type = datatypes.TextAlignment

type TextDecoration = datatypes.TextDecoration
val TextDecoration: datatypes.TextDecoration.type = datatypes.TextDecoration

type TextStyle = datatypes.TextStyle
val TextStyle: datatypes.TextStyle.type = datatypes.TextStyle

type Wrapping = datatypes.Wrapping
val Wrapping: datatypes.Wrapping.type = datatypes.Wrapping

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

// Link

type Link = elements.stateless.link.Link
val Link: elements.stateless.link.Link.type = elements.stateless.link.Link

type LinkTheme = elements.stateless.link.LinkTheme
val LinkTheme: elements.stateless.link.LinkTheme.type = elements.stateless.link.LinkTheme

// Spacer

type Spacer = elements.stateless.spacer.Spacer
val Spacer: elements.stateless.spacer.Spacer.type = elements.stateless.spacer.Spacer

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
