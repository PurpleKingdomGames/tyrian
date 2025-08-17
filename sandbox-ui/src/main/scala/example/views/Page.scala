package example.views

import tyrian.next.HtmlFragment
import tyrian.ui.*

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
            TextBlock("Content goes here")
          ).overrideTheme(
            _.withBackgroundColor(RGBA.White)
          )
        )
      ).withSpacing(Spacing.Large)
    )
