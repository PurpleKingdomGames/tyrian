package example

import cats.effect.IO
import example.views.Page
import tyrian.*
import tyrian.next.*
import tyrian.ui.*
// import components.TopNav

final case class Model(
    // topNav: TopNav,
    nameInput: Input
):

  def update: GlobalMsg => Outcome[Model] =
    case AppEvent.NoOp =>
      Outcome(this)

    case AppEvent.FollowLink(href) =>
      Outcome(this)
        .addActions(Nav.loadUrl[IO](href))

    case e =>
      for {
        // tn <- topNav.update(e)
        ni <- nameInput.update(e)
      } yield this.copy(
        // topNav = tn,
        nameInput = ni
      )

  def view(using Theme): HtmlFragment =
    Page.page |+|
      Model.tmpView(this)

object Model:
  val init: Model =
    Model(
      // components.TopNav.initial,
      Input(UIKey("name-input"))
        .withPlaceholder("Type here...")
        .overrideTheme(
          _.withTextColor(RGBA.fromHex("#1f2937"))
            .withBackgroundColor(RGBA.fromHex("#f9fafb"))
            .solidBorder(BorderWidth.Thin, RGBA.fromHex("#d1d5db"))
            .rounded
            .withPadding(Padding.Small)
        )
    )

  def tmpView(m: Model)(using Theme): HtmlFragment =
    HtmlFragment.insert(
      MarkerId("page-content"),
      Row(
        Column(
          TextBlock("Welcome to Tyrian UI!").toHeading1
            .overrideTheme(_.withTextColor(RGBA.fromHex("#2563eb")).withLineHeight(LineHeight.Loose)),
          Row(
            Column(
              TextBlock("Your name:"),
              m.nameInput,
              TextBlock("Reversed: " + m.nameInput.value.reverse)
            )
          ).withSpacing(Spacing.Small),
          Row(
            TextBlock("Hello, Tyrian!").overrideTheme(_.withTextColor(RGBA.Blue)),
            TextBlock("More text").overrideTheme(_.withTextColor(RGBA.Red.mix(RGBA.Blue)))
          )
            .withSpacing(Spacing.Medium),
          TextBlock("This is just some text")
            .overrideTheme(_.withTextColor(RGBA.fromHex("#6b7280"))),
          HtmlElement(
            tyrian.Html.div(
              tyrian.Html.style := "border: 2px dashed #ccc; padding: 1rem; border-radius: 4px; margin: 1rem 0;"
            )(
              tyrian.Html.p("This is arbitrary HTML embedded within the UI component system!"),
              tyrian.Html.strong("Bold text"),
              tyrian.Html.text(" and "),
              tyrian.Html.em("italic text")
            )
          )
        ),
        Column(
          Container(
            TextBlock("This is some more text.")
          ).middle.center
            .withPadding(Padding.Large)
            .overrideTheme(
              _.rounded
                .solidBorder(BorderWidth.Medium, RGBA.fromHex("#10b981"))
                .shadowMedium(RGBA.fromHex("#00000040"))
                .withBackgroundColor(RGBA.fromHex("#ecfdf5"))
                .withOpacity(Opacity.High)
            ),
          Image(
            "https://raw.githubusercontent.com/PurpleKingdomGames/roguelike-starterkit/417f4e372b4792972ef62aea0c917088a9fc82fd/roguelike.gif",
            "Roguelike"
          ).withSize(Extent.px(300), Extent.px(100))
            .scaleDown
            .overrideTheme(
              _.rounded
                .solidBorder(BorderWidth.Medium, RGBA.fromHex("#2563eb"))
                .shadowLarge(RGBA.fromHex("#00000080"))
                .withBackgroundColor(RGBA.fromHex("#fbbf24"))
                .withOpacity(Opacity.Medium)
            )
        )
      )
        .withSpacing(Spacing.Large)
    )
