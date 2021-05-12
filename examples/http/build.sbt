lazy val root = (project in file("."))
  .dependsOn(scalm)
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(
    scalaVersion := "2.13.5",
    name := "scalm-http-example",
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core",
      "io.circe" %%% "circe-parser"
    ).map(_ % "0.13.0")
  )

lazy val scalm = ProjectRef(file("../.."), "scalm")
