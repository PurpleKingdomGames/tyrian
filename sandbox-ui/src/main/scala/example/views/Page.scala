package example.views

import tyrian.next.*
import tyrian.ui.*

object Page:

  def page(using Theme): HtmlFragment =
    HtmlFragment(
      Column(
        titleBar,
        addPlaceholder(MarkerId("top-nav")),
        addPlaceholder(MarkerId("page-content"))
      ).withSpacing(Spacing.Large)
    )

  def addPlaceholder(markerId: MarkerId): Container =
    Container(
      Placeholder(markerId)
    ).withPadding(Padding.Medium)

  def titleBar: UIElement[Layout, Unit] =
    Column(
      Row(
        Container(
          TextBlock("Tyrian-UI")
            .overrideTheme(
              _.withTextColor(RGBA.White)
                .withFontWeight(FontWeight.ExtraBold)
                .withFontSize(FontSize.XLarge)
            )
        )
          .withPadding(Padding(Spacing.Large))
          .withJustify(Justify.Center)
          .overrideTheme(
            _.withBackgroundFill(
              Fill.RadialGradient(
                Position.TopLeft,
                RGBA.Magenta.mix(RGBA.Black),
                RGBA.Blue.mix(RGBA.Purple).mix(RGBA.Black),
                RGBA.Cyan.mix(RGBA.Blue).mix(RGBA.Black)
              )
            )
          )
      ),
      Row(
        Container(
          Spacer.vertical(Extent.px(2))
        ).overrideTheme(
          _.withBackgroundColor(RGBA.Cyan)
            .withBoxShadow(BoxShadow.large)
        )
      )
    )
