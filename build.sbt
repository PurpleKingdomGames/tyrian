import Misc._

import scala.language.postfixOps
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxOptions
import org.scalajs.jsenv.selenium.SeleniumJSEnv
import org.typelevel.scalacoptions.ScalacOptions

Global / onChangedBuildSource := ReloadOnSourceChanges

Global / resolvers += "Sonatype S01 OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots"

ThisBuild / versionScheme := Some("early-semver")

ThisBuild / scalaVersion := scala3Version

lazy val tyrianVersion      = TyrianVersion.getVersion
lazy val scala3Version      = "3.4.1"
lazy val tyrianDocsVersion  = "0.10.0"
lazy val scalaJsDocsVersion = "1.16.0"
lazy val scalaDocsVersion   = "3.4.1"
lazy val indigoDocsVersion  = "0.16.0"

lazy val commonSettings: Seq[sbt.Def.Setting[_]] = Seq(
  version      := tyrianVersion,
  organization := "io.indigoengine",
  libraryDependencies ++= Seq(
    "org.typelevel" %%% "munit-cats-effect" % Dependencies.munitCatsEffect3 % Test,
    "org.typelevel" %%% "discipline-munit"  % Dependencies.disciplineMUnit  % Test,
    "org.typelevel" %%% "cats-laws"         % Dependencies.catsLaws         % Test
  ),
  testFrameworks += new TestFramework("munit.Framework"),
  scalacOptions ++= Seq("-language:strictEquality"),
  scalafixOnCompile := true,
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision,
  autoAPIMappings   := true
)

lazy val commonScalacOptions = Def.setting {
  // Map the sourcemaps to github paths instead of local directories
  val localSourcesPath = (LocalRootProject / baseDirectory).value.toURI
  val headCommit       = git.gitHeadCommit.value.get
  scmInfo.value.map { info =>
    val remoteSourcesPath =
      s"${info.browseUrl.toString
          .replace("github.com", "raw.githubusercontent.com")}/$headCommit"
    s"-scalajs-mapSourceURI:$localSourcesPath->$remoteSourcesPath"
  }
}

lazy val commonJsSettings: Seq[sbt.Def.Setting[_]] = Seq(
  Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  scalacOptions ++= commonScalacOptions.value
)

lazy val commonBrowserTestJsSettings: Seq[sbt.Def.Setting[_]] = Seq(
  scalacOptions ++= commonScalacOptions.value,
  libraryDependencies ++= Seq(
    "org.scala-js"  %%% "scalajs-dom"  % Dependencies.scalajsDomVersion,
    "org.typelevel" %%% "cats-effect"  % Dependencies.catsEffect,
    "io.circe"      %%% "circe-core"   % Dependencies.circe,
    "io.circe"      %%% "circe-parser" % Dependencies.circe
  )
)

lazy val firefoxJsSettings: Seq[sbt.Def.Setting[_]] = Seq(
  jsEnv := {
    val options = new FirefoxOptions()
    options.setHeadless(true)
    new SeleniumJSEnv(options)
  }
)

lazy val chromeJsSettings: Seq[sbt.Def.Setting[_]] = Seq(
  jsEnv := {
    val options = new ChromeOptions()
    options.setHeadless(true)
    new SeleniumJSEnv(options)
  }
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
    .settings(
      neverPublish,
      commonSettings,
      name := "Tyrian",
      code := codeTaskDefinition,
      copyApiDocs := copyApiDocsTaskDefinition(
        scala3Version,
        (unidocs / Compile / target).value,
        (Compile / target).value
      ),
      usefulTasks := customTasksAliases,
      logoSettings(version)
    )
    .aggregate(
      tyrian.js,
      tyrian.jvm,
      tyrianIO.js,
      tyrianZIO.js,
      sandbox.js,
      sandboxZIO.js,
      firefoxTests.js,
      chromeTests.js,
      docs,
      tyrianHtmx.js,
      tyrianHtmx.jvm,
      sandboxSSR.jvm
    )

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
        "org.typelevel"    %%% "cats-effect-kernel" % Dependencies.catsEffect,
        "co.fs2"           %%% "fs2-core"           % Dependencies.fs2,
        "io.github.buntec" %%% "scala-js-snabbdom"  % Dependencies.scalajsSnabbdom
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
        "org.scala-js"  %%% "scalajs-dom" % Dependencies.scalajsDomVersion,
        "org.typelevel" %%% "cats-effect" % Dependencies.catsEffect
      )
    )
    .dependsOn(tyrian)

lazy val tyrianZIO =
  crossProject(JSPlatform)
    .crossType(CrossType.Pure)
    .withoutSuffixFor(JSPlatform)
    .in(file("tyrian-zio"))
    .settings(
      name := "tyrian-zio",
      commonSettings ++ publishSettings
    )
    .jsSettings(
      commonJsSettings,
      libraryDependencies ++= Seq(
        "org.scala-js"      %%% "scalajs-dom"     % Dependencies.scalajsDomVersion,
        "io.github.cquiroz" %%% "scala-java-time" % Dependencies.scalaJavaTime,
        "dev.zio"           %%% "zio"             % Dependencies.zio
      )
    )
    .dependsOn(tyrian)

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

lazy val sandboxZIO =
  crossProject(JSPlatform)
    .crossType(CrossType.Pure)
    .withoutSuffixFor(JSPlatform)
    .in(file("sandbox-zio"))
    .dependsOn(tyrianZIO)
    .settings(
      neverPublish,
      commonSettings,
      name := "Sandbox ZIO",
      scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
      libraryDependencies ++= Seq(
        "dev.zio" %%% "zio-interop-cats" % Dependencies.zioInteropCats
      ),
      scalacOptions -= "-language:strictEquality"
    )

lazy val sandboxSSR =
  crossProject(JVMPlatform)
    .crossType(CrossType.Pure)
    .dependsOn(tyrian, tyrianHtmx)
    .in(file("sandbox-ssr"))
    .settings(
      neverPublish,
      commonSettings,
      name := "sandbox-ssr",
      scalacOptions -= "-language:strictEquality",
      libraryDependencies ++= Seq(
        "org.http4s" %% "http4s-ember-server" % Dependencies.http4sServer,
        "org.http4s" %% "http4s-dsl"          % Dependencies.http4sServer
      ),
      run / fork := true
    )

lazy val unidocs =
  project
    .enablePlugins(ScalaJSPlugin, ScalaUnidocPlugin)
    .settings(
      name := "Tyrian",
      neverPublish,
      ScalaUnidoc / unidoc / unidocProjectFilter := inAnyProject -- inProjects(
        tyrian.jvm,
        sandboxZIO.js,
        sandbox.js,
        docs,
        firefoxTests.js,
        chromeTests.js
      )
    )

lazy val jsdocs =
  project
    .settings(
      neverPublish,
      organization := "io.indigoengine",
      libraryDependencies ++= Seq(
        "org.scala-js"    %%% "scalajs-dom"  % Dependencies.scalajsDomVersion,
        "io.circe"        %%% "circe-core"   % Dependencies.circe,
        "io.circe"        %%% "circe-parser" % Dependencies.circe,
        "io.indigoengine" %%% "indigo"       % indigoDocsVersion,
        "io.indigoengine" %%% "tyrian-io"    % tyrianDocsVersion,
        "org.http4s"      %%% "http4s-dom"   % Dependencies.http4sDom,
        "org.http4s"      %%% "http4s-circe" % Dependencies.http4sCirce,
        "org.typelevel"   %%% "cats-effect"  % Dependencies.catsEffect
      ),
      Compile / tpolecatExcludeOptions ++= Set(
        ScalacOptions.warnValueDiscard,
        ScalacOptions.warnUnusedImports,
        ScalacOptions.warnUnusedLocals
      )
    )
    .enablePlugins(ScalaJSPlugin)

lazy val docs =
  project
    .in(file("tyrian-docs"))
    .enablePlugins(MdocPlugin)
    .settings(
      neverPublish,
      organization := "io.indigoengine",
      mdocJS       := Some(jsdocs),
      mdocVariables := Map(
        "VERSION"         -> tyrianDocsVersion,
        "SCALAJS_VERSION" -> scalaJsDocsVersion,
        "SCALA_VERSION"   -> scalaDocsVersion
      ),
      Compile / tpolecatExcludeOptions ++= Set(
        ScalacOptions.warnValueDiscard,
        ScalacOptions.warnUnusedImports,
        ScalacOptions.warnUnusedLocals
      )
    )
    .settings(
      run / fork := true
    )

lazy val firefoxTests =
  crossProject(JSPlatform)
    .crossType(CrossType.Pure)
    .withoutSuffixFor(JSPlatform)
    .in(file("tyrian-firefox-tests"))
    .settings(
      name := "tyrian-firefox-tests",
      neverPublish,
      Test / unmanagedSourceDirectories +=
        (LocalRootProject / baseDirectory).value / "tyrian-browser-tests" / "src" / "test" / "scala",
      commonSettings ++ publishSettings ++ firefoxJsSettings
    )
    .jsSettings(commonBrowserTestJsSettings)
    .dependsOn(tyrian)

lazy val chromeTests =
  crossProject(JSPlatform)
    .crossType(CrossType.Pure)
    .withoutSuffixFor(JSPlatform)
    .in(file("tyrian-chrome-tests"))
    .settings(
      name := "tyrian-chrome-tests",
      neverPublish,
      Test / unmanagedSourceDirectories +=
        (LocalRootProject / baseDirectory).value / "tyrian-browser-tests" / "src" / "test" / "scala",
      commonSettings ++ publishSettings ++ chromeJsSettings
    )
    .jsSettings(commonBrowserTestJsSettings)
    .dependsOn(tyrian)

lazy val tyrianHtmx =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Full)
    .in(file("tyrian-htmx"))
    .settings(
      name := "tyrian-htmx",
      commonSettings ++ publishSettings,
      Compile / sourceGenerators += codeGen(
        "tyrian.htmx",
        (fullyQualifiedPath, sourceManagedDir) =>
          List(HtmxAttributes.gen(fullyQualifiedPath, sourceManagedDir)(HtmxAttributes.htmxAttrsList))
      ).taskValue
    )
    .jsSettings(
      commonJsSettings
    )
    .dependsOn(tyrian)

addCommandAlias(
  "sandboxBuild",
  List(
    "sandbox/fastLinkJS"
  ).mkString("", ";", ";")
)
addCommandAlias(
  "sandboxZIOBuild",
  List(
    "sandboxZIO/fastLinkJS"
  ).mkString("", ";", ";")
)
addCommandAlias(
  "sandboxSSRBuild",
  List(
    "sandboxSSRJVM/compile"
  ).mkString("", ";", ";")
)
addCommandAlias(
  "sandboxSSRServer",
  List(
    "sandboxSSRJVM/run"
  ).mkString("", ";", ";")
)

addCommandAlias(
  "gendocs",
  List(
    "clean",
    "compile",        // Make sure we generate sources
    "unidocs/unidoc", // Docs in ./target/scala-3.x.x/unidoc/
    "copyApiDocs",    // Copied to ./target/unidocs/site-docs
    "docs/mdoc"       // Content docs in ./tyrian-docs/target/mdoc
  ).mkString(";", ";", "")
)

addCommandAlias(
  "cleanAll",
  List(
    "clean",
    "tyrianJS/clean",
    "tyrianJVM/clean",
    "tyrianIO/clean",
    "tyrianZIO/clean",
    "sandbox/clean",
    "sandboxZIO/clean",
    "firefoxTests/clean",
    "chromeTests/clean"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "compileAll",
  List(
    "tyrianJS/compile",
    "tyrianJVM/compile",
    "tyrianIO/compile",
    "tyrianZIO/compile",
    "sandbox/compile",
    "sandboxZIO/compile"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "testAll",
  List(
    "tyrianJS/test",
    "tyrianJVM/test",
    "tyrianIO/test",
    "tyrianZIO/test",
    "sandbox/test",
    "sandboxZIO/test",
    "firefoxTests/test",
    "chromeTests/test"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "testAllUnit",
  List(
    "tyrianJS/test",
    "tyrianJVM/test",
    "tyrianIO/test",
    "tyrianZIO/test",
    "sandbox/test",
    "sandboxZIO/test"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "testRelease",
  List(
    "tyrianJS/test",
    "tyrianJVM/test",
    "tyrianIO/test",
    "tyrianZIO/test"
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
