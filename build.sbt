import sbt.Credentials

lazy val scalm = (project in file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaVersion := "3.0.0",
    version := "0.0.1-SNAPSHOT",
    name := "scalm",
    organization := "davesmith00000",
    libraryDependencies ++= Seq(
      ("org.scala-js" %%% "scalajs-dom" % "1.1.0").cross(CrossVersion.for3Use2_13),
      "org.typelevel" %%% "cats-core"   % "2.6.1"
    ),
    publishTo := sonatypePublishTo.value
  )
