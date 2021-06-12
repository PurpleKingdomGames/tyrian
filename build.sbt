import scala.sys.process._
import scala.language.postfixOps

ThisBuild / versionScheme := Some("early-semver")

lazy val tyrianVersion = "0.2.0-SNAPSHOT"

val scala3Version = "3.0.0"

lazy val commonSettings: Seq[sbt.Def.Setting[_]] = Seq(
  version := tyrianVersion,
  scalaVersion := scala3Version,
  crossScalaVersions := Seq(scala3Version),
  organization := "io.indigoengine",
  libraryDependencies ++= Seq(
    "org.scalameta" %%% "munit" % "0.7.26" % Test
  ),
  testFrameworks += new TestFramework("munit.Framework"),
  Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  scalacOptions ++= Seq("-language:strictEquality"),
  crossScalaVersions := Seq(scala3Version)
)

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

lazy val tyrian =
  project
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(publishSettings: _*)
    .settings(
      name := "tyrian",
      libraryDependencies ++= Seq(
        ("org.scala-js" %%% "scalajs-dom" % "1.1.0").cross(CrossVersion.for3Use2_13),
        "org.typelevel" %%% "cats-core"   % "2.6.1"
      )
    )

lazy val sandbox = 
  project
    .dependsOn(tyrian)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      scalaVersion := "3.0.0",
      name := "sandbox",
      scalaJSUseMainModuleInitializer := true,
      scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
    )

lazy val tyrianProject =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      code := { "code ." ! },
    )
    .enablePlugins(ScalaJSPlugin)
    .aggregate(tyrian, sandbox)

lazy val code =
  taskKey[Unit]("Launch VSCode in the current directory")

addCommandAlias(
  "sandboxBuild",
  List(
    "sandbox/clean",
    "sandbox/test",
    "sandbox/fastOptJS"
  ).mkString("", ";", ";")
)
