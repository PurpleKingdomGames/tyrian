import scala.sys.process._
import scala.language.postfixOps

import sbtwelcome._

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / versionScheme := Some("early-semver")

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"

lazy val tyrianVersion = "0.2.2-SNAPSHOT"

lazy val scala3Version = "3.1.0"

lazy val tyrianDocsVersion  = "0.2.2-SNAPSHOT"
lazy val scalaJsDocsVersion = "1.8.0"
lazy val scalaDocsVersion   = "3.1.0"

lazy val commonSettings: Seq[sbt.Def.Setting[_]] = Seq(
  version            := tyrianVersion,
  scalaVersion       := scala3Version,
  crossScalaVersions := Seq(scala3Version),
  organization       := "io.indigoengine",
  libraryDependencies ++= Seq(
    "org.scalameta" %%% "munit" % "0.7.29" % Test
  ),
  testFrameworks += new TestFramework("munit.Framework"),
  Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  scalacOptions ++= Seq("-language:strictEquality"),
  crossScalaVersions := Seq(scala3Version),
  scalafixOnCompile  := true,
  semanticdbEnabled  := true,
  semanticdbVersion  := scalafixSemanticdb.revision,
  autoAPIMappings    := true
)

lazy val publishSettings = {
  import xerial.sbt.Sonatype._
  Seq(
    publishTo              := sonatypePublishToBundle.value,
    publishMavenStyle      := true,
    sonatypeProfileName    := "io.indigoengine",
    licenses               := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
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
        "org.scala-js"  %%% "scalajs-dom" % Dependancies.scalajsDomVersion,
        "org.typelevel" %%% "cats-core"   % Dependancies.catsCore
      )
    )
    .settings(
      Compile / sourceGenerators += Def.task {
        TagGen
          .gen("HtmlTags", "tyrian", (Compile / sourceManaged).value)
      }.taskValue,
      Compile / sourceGenerators += Def.task {
        AttributeGen
          .gen("HtmlAttributes", "tyrian", (Compile / sourceManaged).value)
      }.taskValue
    )

lazy val tyrianIndigoBridge =
  project
    .in(file("tyrian-indigo-bridge"))
    .dependsOn(tyrian)
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(publishSettings: _*)
    .settings(
      name := "tyrian-indigo-bridge"
    )
    .settings(
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo" % Dependancies.indigoVersion
      )
    )

lazy val sandbox =
  project
    .dependsOn(tyrian)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      scalaVersion                    := scala3Version,
      name                            := "sandbox",
      scalaJSUseMainModuleInitializer := true,
      scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
    )
    .settings(
      publish      := {},
      publishLocal := {}
    )

lazy val indigoSandbox =
  project
    .in(file("indigo-sandbox"))
    .dependsOn(tyrian)
    .dependsOn(tyrianIndigoBridge)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      scalaVersion                    := scala3Version,
      name                            := "Indigo Sandbox",
      scalaJSUseMainModuleInitializer := true,
      scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
    )
    .settings(
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo"            % Dependancies.indigoVersion,
        "io.indigoengine" %%% "indigo-extras"     % Dependancies.indigoVersion,
        "io.indigoengine" %%% "indigo-json-circe" % Dependancies.indigoVersion
      )
    )
    .settings(
      publish      := {},
      publishLocal := {}
    )

lazy val jsdocs = project
  .settings(
    scalaVersion := scala3Version,
    organization := "io.indigoengine"
  )
  .settings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % Dependancies.scalajsDomVersion
  )
  .settings(
    publish      := {},
    publishLocal := {}
  )
  .enablePlugins(ScalaJSPlugin)

lazy val docs = project
  .in(file("tyrian-docs"))
  .dependsOn(tyrian)
  .dependsOn(tyrianIndigoBridge)
  .enablePlugins(MdocPlugin)
  .settings(
    scalaVersion := scala3Version,
    organization := "io.indigoengine"
  )
  .settings(
    mdocVariables := Map(
      "VERSION"         -> tyrianDocsVersion,
      "SCALAJS_VERSION" -> scalaJsDocsVersion,
      "SCALA_VERSION"   -> scalaDocsVersion
    ),
    mdocExtraArguments := List("--no-link-hygiene")
  )
  .settings(
    mdocJS := Some(jsdocs)
  )
  .settings(
    publish      := {},
    publishLocal := {}
  )

lazy val rawLogo: String =
  """
    |  _____         _           
    | |_   _|  _ _ _(_)__ _ _ _  
    |   | || || | '_| / _` | ' \ 
    |   |_| \_, |_| |_\__,_|_||_|
    |       |__/                 
    |""".stripMargin

lazy val tyrianProject =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin)
    .enablePlugins(ScalaUnidocPlugin)
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
      name                                       := "Tyrian",
      ScalaUnidoc / unidoc / unidocProjectFilter := inAnyProject -- inProjects(indigoSandbox, sandbox, docs),
      copyApiDocs := {
        println("Copy docs from 'target/scala-3.1.0/unidoc' to 'target/scala-3.1.0/site-docs/api'")

        val src = (Compile / target).value / "scala-3.1.0" / "unidoc"
        val dst = (Compile / target).value / "scala-3.1.0" / "site-docs" / "api"

        IO.copyDirectory(src, dst)
      }
    )
    .settings(
      publish      := {},
      publishLocal := {}
    )
    .settings(
      logo := rawLogo + s"version ${version.value}",
      usefulTasks := Seq(
        UsefulTask("", "publishLocal", "Locally publish the core modules"),
        UsefulTask("", "sandboxBuild", "Build the sandbox project"),
        UsefulTask("", "indigoSandboxBuild", "Build the indigo/tyrian bridge project"),
        UsefulTask("", "gendocs", "Rebuild the API and markdown docs"),
        UsefulTask("", "code", "Launch VSCode")
      ),
      logoColor        := scala.Console.MAGENTA,
      aliasColor       := scala.Console.BLUE,
      commandColor     := scala.Console.CYAN,
      descriptionColor := scala.Console.WHITE
    )
    .aggregate(tyrian, tyrianIndigoBridge, sandbox, indigoSandbox)

lazy val code =
  taskKey[Unit]("Launch VSCode in the current directory")

// Define task to  copy html files
val copyApiDocs =
  taskKey[Unit]("Copy html files from src/main/html to cross-version target directory")

addCommandAlias(
  "sandboxBuild",
  List(
    "sandbox/fastOptJS"
  ).mkString("", ";", ";")
)
addCommandAlias(
  "indigoSandboxBuild",
  List(
    "indigoSandbox/fastOptJS"
  ).mkString("", ";", ";")
)

addCommandAlias(
  "gendocs",
  List(
    "clean",
    "unidoc", // Docs in ./target/scala-3.1.0/unidoc/
    "copyApiDocs",
    "docs/mdoc" // Docs in ./indigo/tyrian-docs/target/mdoc
  ).mkString(";", ";", "")
)
