import Misc._

import scala.language.postfixOps

Global / onChangedBuildSource := ReloadOnSourceChanges

Global / resolvers += "Sonatype S01 OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots"

ThisBuild / versionScheme := Some("early-semver")

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"

ThisBuild / scalaVersion := scala3Version

lazy val tyrianVersion      = TyrianVersion.getVersion
lazy val scala3Version      = "3.1.1"
lazy val tyrianDocsVersion  = "0.3.2"
lazy val scalaJsDocsVersion = "1.9.0"
lazy val scalaDocsVersion   = "3.1.1"

lazy val commonSettings: Seq[sbt.Def.Setting[_]] = Seq(
  version      := tyrianVersion,
  organization := "io.indigoengine",
  libraryDependencies ++= Seq(
    "org.scalameta" %%% "munit"               % "0.7.29"                      % Test,
    "org.typelevel" %%% "munit-cats-effect-3" % Dependancies.munitCatsEffect3 % Test
  ),
  testFrameworks += new TestFramework("munit.Framework"),
  scalacOptions ++= Seq("-language:strictEquality"),
  scalafixOnCompile := true,
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision,
  autoAPIMappings   := true
)

lazy val commonJsSettings: Seq[sbt.Def.Setting[_]] = Seq(
  Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
)

lazy val neverPublish = Seq(
  publish / skip      := true,
  publishLocal / skip := true
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

lazy val tyrianProject =
  project
    .in(file("."))
    .enablePlugins(ScalaUnidocPlugin)
    .settings(
      neverPublish,
      commonSettings,
      name        := "Tyrian",
      code        := codeTaskDefinition,
      copyApiDocs := copyApiDocsTaskDefinition((Compile / target).value),
      usefulTasks := customTasksAliases,
      logoSettings(version),
      ScalaUnidoc / unidoc / unidocProjectFilter := inAnyProject -- inProjects(
        tyrian.jvm,
        indigoSandbox.js,
        sandbox.js,
        docs
      )
    )
    .aggregate(tyrian.js, tyrian.jvm, tyrianIO.js, tyrianIndigoBridge.js, sandbox.js, indigoSandbox.js)

lazy val tyrian =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Full)
    .settings(
      name := "tyrian",
      commonSettings ++ publishSettings,
      Compile / sourceGenerators += codeGen("tyrian", TagGen.gen).taskValue,
      Compile / sourceGenerators += codeGen("tyrian", AttributeGen.gen).taskValue,
      Compile / sourceGenerators += codeGen("CSS", "tyrian", CSSGen.gen).taskValue
    )
    .jsSettings(
      commonJsSettings,
      libraryDependencies ++= Seq(
        "org.scala-js"     %%% "scalajs-dom"        % Dependancies.scalajsDomVersion,
        "org.typelevel"    %%% "cats-effect-kernel" % Dependancies.catsEffect,
        "co.fs2"           %%% "fs2-core"           % Dependancies.fs2,
        "io.github.buntec" %%% "scala-js-snabbdom"  % "0.1.0"
      )
    )

lazy val tyrianIO =
  crossProject(JSPlatform)
    .crossType(CrossType.Pure)
    .withoutSuffixFor(JSPlatform)
    .in(file("tyrian-io"))
    .settings(
      name := "tyrian-io",
      commonSettings ++ publishSettings
    )
    .jsSettings(
      commonJsSettings,
      libraryDependencies ++= Seq(
        "org.scala-js"  %%% "scalajs-dom" % Dependancies.scalajsDomVersion,
        "org.typelevel" %%% "cats-effect" % Dependancies.catsEffect
      )
    )
    .dependsOn(tyrian)

lazy val tyrianIndigoBridge =
  crossProject(JSPlatform)
    .crossType(CrossType.Pure)
    .withoutSuffixFor(JSPlatform)
    .in(file("tyrian-indigo-bridge"))
    .dependsOn(tyrian)
    .settings(
      name := "tyrian-indigo-bridge",
      commonSettings ++ publishSettings,
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo" % Dependancies.indigoVersion
      )
    )
    .jsSettings(commonJsSettings: _*)

lazy val sandbox =
  crossProject(JSPlatform)
    .crossType(CrossType.Pure)
    .withoutSuffixFor(JSPlatform)
    .dependsOn(tyrianIO)
    .settings(
      neverPublish,
      commonSettings,
      name := "sandbox",
      scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
      scalacOptions -= "-language:strictEquality"
    )

lazy val indigoSandbox =
  crossProject(JSPlatform)
    .crossType(CrossType.Pure)
    .withoutSuffixFor(JSPlatform)
    .in(file("indigo-sandbox"))
    .dependsOn(tyrianIndigoBridge)
    .dependsOn(tyrianIO)
    .settings(
      neverPublish,
      commonSettings,
      name := "Indigo Sandbox",
      scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo"            % Dependancies.indigoVersion,
        "io.indigoengine" %%% "indigo-extras"     % Dependancies.indigoVersion,
        "io.indigoengine" %%% "indigo-json-circe" % Dependancies.indigoVersion
      ),
      scalacOptions -= "-language:strictEquality"
    )

lazy val jsdocs =
  project
    .settings(
      neverPublish,
      organization                           := "io.indigoengine",
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % Dependancies.scalajsDomVersion
    )
    .enablePlugins(ScalaJSPlugin)

lazy val docs =
  project
    .in(file("tyrian-docs"))
    .dependsOn(tyrian.js)
    .dependsOn(tyrianIO.js)
    .dependsOn(tyrianIndigoBridge.js)
    .enablePlugins(MdocPlugin)
    .settings(
      neverPublish,
      organization       := "io.indigoengine",
      mdocExtraArguments := List("--no-link-hygiene"),
      mdocJS             := Some(jsdocs),
      mdocVariables := Map(
        "VERSION"         -> tyrianDocsVersion,
        "SCALAJS_VERSION" -> scalaJsDocsVersion,
        "SCALA_VERSION"   -> scalaDocsVersion
      )
    )
    .settings(
      run / fork := true
    )

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
    "docs/clean",
    "unidoc", // Docs in ./target/scala-3.1.1/unidoc/
    "copyApiDocs",
    "docs/mdoc" // Docs in ./indigo/tyrian-docs/target/mdoc
  ).mkString(";", ";", "")
)

addCommandAlias(
  "cleanAll",
  List(
    "tyrianJS/clean",
    "tyrianJVM/clean",
    "tyrianIO/clean",
    "tyrianIndigoBridge/clean",
    "sandbox/clean",
    "indigoSandbox/clean"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "compileAll",
  List(
    "tyrianJS/compile",
    "tyrianJVM/compile",
    "tyrianIO/compile",
    "tyrianIndigoBridge/compile",
    "sandbox/compile",
    "indigoSandbox/compile"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "testAll",
  List(
    "tyrianJS/test",
    "tyrianJVM/test",
    "tyrianIO/test",
    "tyrianIndigoBridge/test",
    "sandbox/test",
    "indigoSandbox/test"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "localPublish",
  "+publishLocal"
)

def codeGen(path: String, makeFiles: (String, File) => Seq[File]) =
  Def.task(makeFiles(path, (Compile / sourceManaged).value))

def codeGen(moduleName: String, path: String, makeFiles: (String, String, File) => Seq[File]) =
  Def.task(makeFiles(moduleName, path, (Compile / sourceManaged).value))
