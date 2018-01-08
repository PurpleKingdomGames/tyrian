enablePlugins(ScalaJSBundlerPlugin)
addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3")

name := "scalm"
version := "1.0.0-SNAPSHOT"
organization := "org.julienrf"

npmDependencies in Compile += "snabbdom" -> "0.6.7"
libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  "org.typelevel" %%% "cats" % "0.9.0"
)

scalaVersion := "2.12.4"
scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard"
)
