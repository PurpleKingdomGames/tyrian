val Http4sVersion          = "0.23.6"
val CirceVersion           = "0.14.1"
val MunitVersion           = "0.7.29"
val LogbackVersion         = "1.2.6"
val MunitCatsEffectVersion = "1.0.6"
val TyrianVersion          = "0.2.2-SNAPSHOT"

val scala3Version = "3.1.0"

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
        "io.indigoengine" %% "tyrian"              % TyrianVersion
      )
    )

lazy val serverExamples =
  (project in file("."))
    .settings(commonSettings: _*)
    .aggregate(server)
