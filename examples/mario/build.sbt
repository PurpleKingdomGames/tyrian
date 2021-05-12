lazy val root = (project in file("."))
  .dependsOn(scalm)
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(
    scalaVersion := "2.13.5",
    name := "mario",
    scalaJSUseMainModuleInitializer := true
  )
lazy val scalm = ProjectRef(file("../.."), "scalm")
