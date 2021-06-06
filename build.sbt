import scala.sys.process._
import scala.language.postfixOps

ThisBuild / versionScheme := Some("early-semver")

lazy val tyrian =
  (project in file("tyrian"))
    .enablePlugins(ScalaJSPlugin)
    .settings(
      scalaVersion := "3.0.0",
      version := "0.1.0-SNAPSHOT",
      name := "tyrian",
      organization := "io.indigoengine",
      libraryDependencies ++= Seq(
        ("org.scala-js" %%% "scalajs-dom" % "1.1.0").cross(CrossVersion.for3Use2_13),
        "org.typelevel" %%% "cats-core"   % "2.6.1",
        "org.scalameta" %%% "munit"       % "0.7.26" % Test
      ),
      testFrameworks += new TestFramework("munit.Framework"),
      scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
      publishTo := sonatypePublishTo.value
    )

lazy val tyrianProject =
  (project in file("."))
    .settings(
      code := { "code ." ! }
    )
    .enablePlugins(ScalaJSPlugin)
    .aggregate(tyrian)

lazy val code =
  taskKey[Unit]("Launch VSCode in the current directory")

lazy val publishSettings = {
  import xerial.sbt.Sonatype._
  Seq(
    publishTo := sonatypePublishToBundle.value,
    publishMavenStyle := true,
    sonatypeProfileName := "io.indigoengine",
    licenses := Seq("BSD-3-Clause" -> url("http://opensource.org/licenses/BSD-3-Clause")),
    sonatypeProjectHosting := Some(GitHubHosting("PurpleKingdomGames", "tyrian", "indigo@purplekingdomgames.com")),
    developers := List(
      Developer(
        id = "davesmith00000",
        name = "David Smith",
        email = "indigo@purplekingdomgames.com",
        url = url("https://github.com/davesmith00000")
      )
    )
  )
}
