import scala.sys.process._
import scala.language.postfixOps

val Http4sVersion          = "0.23.6"
val CirceVersion           = "0.14.1"
val MunitVersion           = "0.7.29"
val LogbackVersion         = "1.2.6"
val MunitCatsEffectVersion = "1.0.6"

lazy val tyrianVersion = TyrianVersion.getVersion
lazy val scala3Version = "3.1.0"

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"

lazy val commonSettings: Seq[sbt.Def.Setting[_]] = Seq(
  version      := "0.0.1-SNAPSHOT",
  organization := "com.example",
  scalaVersion := scala3Version,
  scalacOptions ++= Seq("-source:future"),
  libraryDependencies ++= Seq(
    "org.scalameta" %% "munit" % MunitVersion % Test
  ),
  testFrameworks += new TestFramework("munit.Framework")
)

lazy val server =
  project
    .settings(commonSettings: _*)
    .settings(
      name := "server",
      libraryDependencies ++= Seq(
        "org.http4s"      %% "http4s-ember-server" % Http4sVersion,
        "org.http4s"      %% "http4s-ember-client" % Http4sVersion,
        "org.http4s"      %% "http4s-circe"        % Http4sVersion,
        "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
        "io.circe"        %% "circe-generic"       % CirceVersion,
        "org.typelevel"   %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test,
        "ch.qos.logback"   % "logback-classic"     % LogbackVersion,
        "io.indigoengine" %% "tyrian"              % tyrianVersion
      )
    )

lazy val spa =
  project
    .enablePlugins(ScalaJSPlugin)
    .enablePlugins(ScalaJSBundlerPlugin)
    .settings(commonSettings: _*)
    .settings(
      name := "SPA",
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "tyrian" % tyrianVersion
      ),
      scalaJSUseMainModuleInitializer := true,
      scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
    )
    .settings(
      Compile / npmDependencies += "snabbdom" -> "3.0.1",
      // Source maps seem to be broken with bundler
      Compile / fastOptJS / scalaJSLinkerConfig ~= { _.withSourceMap(false) },
      Compile / fullOptJS / scalaJSLinkerConfig ~= { _.withSourceMap(false) }
    )

lazy val serverExamples =
  (project in file("."))
    .settings(commonSettings: _*)
    .aggregate(server, spa)
    .settings(
      code := {
        val command = Seq("code", ".")
        val run = sys.props("os.name").toLowerCase match {
          case x if x contains "windows" => Seq("cmd", "/C") ++ command
          case _                         => command
        }
        run.!
      }
    )

lazy val code =
  taskKey[Unit]("Launch VSCode in the current directory")

addCommandAlias(
  "start",
  List(
    "spa/fastOptJS::webpack",
    "server/run"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "buildAll",
  List(
    "spa/fastOptJS::webpack",
    "server/test"
  ).mkString(";", ";", "")
)
