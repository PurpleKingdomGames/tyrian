lazy val root = (project in file("."))
  .dependsOn(scalm)
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(
    scalaVersion := "2.12.4",
    name := "scalm-subcomponents-example",
    scalaJSUseMainModuleInitializer := true
  )

lazy val scalm = ProjectRef(file("../.."), "scalm")
