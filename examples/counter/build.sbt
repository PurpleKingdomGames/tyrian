lazy val root = (project in file("."))
  .dependsOn(tyrian)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaVersion := "3.0.0",
    name := "tyrian-counter-example",
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  )

lazy val tyrian = ProjectRef(file("../.."), "tyrian")
