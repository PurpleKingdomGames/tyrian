import scala.sys.process._
import scala.language.postfixOps

import sbtwelcome._

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"

lazy val tyrianVersion = TyrianVersion.getVersion
lazy val scala3Version = "3.1.1"

lazy val commonSettings: Seq[sbt.Def.Setting[_]] = Seq(
  version      := tyrianVersion,
  scalaVersion := scala3Version,
  organization := "io.indigoengine",
  libraryDependencies ++= Seq(
    "org.scalameta" %%% "munit" % "0.7.29" % Test
  ),
  libraryDependencies ++= Seq(
    "io.indigoengine" %%% "tyrian" % tyrianVersion
  ),
  testFrameworks += new TestFramework("munit.Framework"),
  scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  crossScalaVersions := Seq(scala3Version),
  scalafixOnCompile  := true,
  semanticdbEnabled  := true,
  semanticdbVersion  := scalafixSemanticdb.revision,
  autoAPIMappings    := true
)

lazy val bootstrap =
  (project in file("bootstrap"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(name := "bootstrap")

lazy val bundler =
  (project in file("bundler"))
    .enablePlugins(ScalaJSPlugin)
    .enablePlugins(ScalaJSBundlerPlugin)
    .settings(commonSettings: _*)
    .settings(name := "bundler")
    .settings(
      Compile / npmDependencies += "snabbdom" -> "3.0.1",
      // Source maps seem to be broken with bundler
      Compile / fastOptJS / scalaJSLinkerConfig ~= { _.withSourceMap(false) },
      Compile / fullOptJS / scalaJSLinkerConfig ~= { _.withSourceMap(false) },
      scalaJSUseMainModuleInitializer := true
    )

lazy val clock =
  (project in file("clock"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(name := "clock")

lazy val counter =
  (project in file("counter"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(name := "counter")

lazy val field =
  (project in file("field"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(name := "field")

lazy val http =
  (project in file("http"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      name := "http",
      libraryDependencies ++= Seq(
        "io.circe" %%% "circe-core",
        "io.circe" %%% "circe-parser"
      ).map(_ % "0.14.0-M7")
    )

lazy val indigo =
  (project in file("indigo"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      name := "indigo-bridge",
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "tyrian-indigo-bridge" % tyrianVersion,
        "io.indigoengine" %%% "indigo"            % Dependancies.indigoVersion,
        "io.indigoengine" %%% "indigo-extras"     % Dependancies.indigoVersion,
        "io.indigoengine" %%% "indigo-json-circe" % Dependancies.indigoVersion
      )
    )

lazy val mario =
  (project in file("mario"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(name := "mario")

lazy val subcomponents =
  (project in file("subcomponents"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(name := "subcomponents")

lazy val websocket =
  (project in file("websocket"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(name := "websocket")

lazy val exampleProjects: List[String] =
  List(
    "bootstrap",
    "bundler",
    "clock",
    "counter",
    "field",
    "http",
    "indigo",
    "mario",
    "subcomponents",
    "websocket"
  )

lazy val tyrianExamplesProject =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      code := {
        val command = Seq("code", ".")
        val run = sys.props("os.name").toLowerCase match {
          case x if x contains "windows" => Seq("cmd", "/C") ++ command
          case _                         => command
        }
        run.!
      },
      name := "TyrianExamples"
    )
    .settings(
      logo := s"Tyrian Examples (v${version.value})",
      usefulTasks := Seq(
        UsefulTask("a", "buildExamples", "Cleans and builds all examples"),
        UsefulTask("b", "cleanAll", "Cleans all examples"),
        UsefulTask("c", "compileAll", "Compiles all examples"),
        UsefulTask("d", "fastOptAll", "Compiles all examples to JS"),
        UsefulTask("e", "code", "Launch VSCode")
      ) ++ makeCmds(exampleProjects),
      logoColor        := scala.Console.MAGENTA,
      aliasColor       := scala.Console.BLUE,
      commandColor     := scala.Console.CYAN,
      descriptionColor := scala.Console.WHITE
    )

lazy val code =
  taskKey[Unit]("Launch VSCode in the current directory")

def makeCmds(names: List[String]): Seq[UsefulTask] =
  names.zipWithIndex.map { case (n, i) =>
    val cmd =
      n match {
        case "bundler" =>
          List(
            s"$n/clean",
            s"$n/fastOptJS::webpack"
          ).mkString(";", ";", "")

        case _ =>
          List(
            s"$n/clean",
            s"$n/fastOptJS"
          ).mkString(";", ";", "")
      }

    UsefulTask("build" + (i + 1), cmd, n)
  }.toSeq

// Top level commands
def applyCommand(projects: List[String], command: String): String =
  projects.map(p => p + "/" + command).mkString(";", ";", "")

def applyToAll(command: String): String =
  List(
    applyCommand(exampleProjects, command)
  ).mkString

addCommandAlias(
  "cleanAll",
  applyToAll("clean")
)
addCommandAlias(
  "compileAll",
  applyToAll("compile")
)
addCommandAlias(
  "fastOptAll",
  applyToAll("fastOptJS")
)
addCommandAlias(
  "buildExamples",
  List(
    "cleanAll",
    "compileAll",
    "fastOptAll"
  ).mkString(";", ";", "")
)
