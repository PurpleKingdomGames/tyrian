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

lazy val tyrianVersion = TyrianVersion.getVersion
lazy val scala3Version = "3.7.0"

lazy val commonSettings: Seq[sbt.Def.Setting[?]] = Seq(
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

lazy val commonJsSettings: Seq[sbt.Def.Setting[?]] = Seq(
  Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  scalacOptions ++= commonScalacOptions.value
)

lazy val commonBrowserTestJsSettings: Seq[sbt.Def.Setting[?]] = Seq(
  scalacOptions ++= commonScalacOptions.value,
  libraryDependencies ++= Seq(
    "org.scala-js"  %%% "scalajs-dom"  % Dependencies.scalajsDomVersion,
    "org.typelevel" %%% "cats-effect"  % Dependencies.catsEffect,
    "io.circe"      %%% "circe-core"   % Dependencies.circe,
    "io.circe"      %%% "circe-parser" % Dependencies.circe
  )
)

lazy val firefoxJsSettings: Seq[sbt.Def.Setting[?]] = Seq(
  jsEnv := {
    val options = new FirefoxOptions()
    options.setHeadless(true)
    new SeleniumJSEnv(options)
  }
)

lazy val chromeJsSettings: Seq[sbt.Def.Setting[?]] = Seq(
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
    ),
    sonatypeCredentialHost := "oss.sonatype.org",
    sonatypeRepository     := "https://oss.sonatype.org/service/local"
  )
}

lazy val tyrianProject =
  project
    .in(file("."))
    .settings(
      neverPublish,
      commonSettings,
      name        := "Tyrian",
      usefulTasks := customTasksAliases,
      logoSettings(version)
    )
    .aggregate(
      tyrianTags.js,
      tyrianTags.jvm,
      tyrian.js,
      tyrianIO.js,
      tyrianZIO.js,
      sandbox.js,
      sandboxZIO.js,
      firefoxTests.js,
      chromeTests.js,
      tyrianHtmx.js,
      tyrianHtmx.jvm,
      sandboxSSR.jvm
    )

lazy val tyrianTags =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Full)
    .in(file("tyrian-tags"))
    .settings(
      name := "tyrian-tags",
      commonSettings ++ publishSettings,
      Compile / sourceGenerators += codeGen("tyrian", TagGen.gen).taskValue,
      Compile / sourceGenerators += codeGen("tyrian", AttributeGen.gen).taskValue,
      Compile / sourceGenerators += codeGen("CSS", "tyrian", CSSGen.gen).taskValue
    )
    .jsSettings(
      commonJsSettings,
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % Dependencies.scalajsDomVersion
      )
    )

lazy val tyrian =
  crossProject(JSPlatform)
    .crossType(CrossType.Pure)
    .withoutSuffixFor(JSPlatform)
    .settings(
      name := "tyrian",
      commonSettings ++ publishSettings
    )
    .jsSettings(
      commonJsSettings,
      libraryDependencies ++= Seq(
        "org.typelevel"    %%% "cats-effect-kernel" % Dependencies.catsEffect,
        "co.fs2"           %%% "fs2-core"           % Dependencies.fs2,
        "io.github.buntec" %%% "scala-js-snabbdom"  % Dependencies.scalajsSnabbdom
      )
    )
    .dependsOn(tyrianTags)

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
    .dependsOn(tyrianTags, tyrianHtmx)
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
    .dependsOn(tyrianTags)

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
  "cleanAll",
  List(
    "clean",
    "tyrianTagsJS/clean",
    "tyrianTagsJVM/clean",
    "tyrian/clean",
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
    "tyrianTagsJS/compile",
    "tyrianTagsJVM/compile",
    "tyrian/compile",
    "tyrianIO/compile",
    "tyrianZIO/compile",
    "sandbox/compile",
    "sandboxZIO/compile"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "testAll",
  List(
    "tyrianTagsJS/test",
    "tyrianTagsJVM/test",
    "tyrian/test",
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
    "tyrianTagsJS/test",
    "tyrianTagsJVM/test",
    "tyrian/test",
    "tyrianIO/test",
    "tyrianZIO/test",
    "sandbox/test",
    "sandboxZIO/test"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "testRelease",
  List(
    "tyrianTagsJS/test",
    "tyrianTagsJVM/test",
    "tyrian/test",
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
