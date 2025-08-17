package example.views

import tyrian.next.HtmlFragment
import tyrian.next.MarkerId
import tyrian.ui.*
import tyrian.ui.layout.Placeholder

object Page:

  def page(using Theme): HtmlFragment =
    HtmlFragment(
      Column(
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
        ),
        Row(
          Container(
            Placeholder(MarkerId("page-content"))
          ).withPadding(Padding.Medium)
        )
      ).withSpacing(Spacing.Large)
    )
