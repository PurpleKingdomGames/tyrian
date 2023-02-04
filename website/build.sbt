Global / onChangedBuildSource := ReloadOnSourceChanges

val scala3Version = "3.2.1"

lazy val tyrianSite =
  (project in file("."))
    .enablePlugins(GhpagesPlugin)
    .settings(
      name                     := "indigo site publisher",
      version                  := "0.0.1",
      scalaVersion             := scala3Version,
      organization             := "io.indigo",
      siteSourceDirectory      := target.value / ".." / "public",
      makeSite / includeFilter := "*",
      makeSite / excludeFilter := ".DS_Store",
      git.remoteRepo           := "git@github.com:PurpleKingdomGames/tyrian.git",
      ghpagesNoJekyll          := true
    )

addCommandAlias(
  "publishTyrianSite",
  List(
    "makeSite",
    "ghpagesPushSite"
  ).mkString(";", ";", "")
)
