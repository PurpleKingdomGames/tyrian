lazy val root = (project in file("."))
  .dependsOn(scalm)
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(
    scalaVersion := "2.13.6",
    name := "mario",
    scalaJSUseMainModuleInitializer := true
  )
lazy val scalm = ProjectRef(file("../.."), "scalm")
