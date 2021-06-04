import scala.sys.process._
import scala.language.postfixOps
import sbt.Credentials

lazy val scalm =
  (project in file("scalm"))
    .enablePlugins(ScalaJSPlugin)
    .settings(
      scalaVersion := "3.0.0",
      version := "0.0.1-SNAPSHOT",
      name := "scalm",
      organization := "davesmith00000",
      libraryDependencies ++= Seq(
        ("org.scala-js" %%% "scalajs-dom" % "1.1.0").cross(CrossVersion.for3Use2_13),
        "org.typelevel" %%% "cats-core"   % "2.6.1",
        "org.scalameta" %%% "munit"       % "0.7.26" % Test
      ),
      testFrameworks += new TestFramework("munit.Framework"),
      scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
      publishTo := sonatypePublishTo.value
    )

lazy val scalmProject =
  (project in file("."))
    .settings(
      code := { "code ." ! }
    )
    .enablePlugins(ScalaJSPlugin)
    .aggregate(scalm)

lazy val code =
  taskKey[Unit]("Launch VSCode in the current directory")
