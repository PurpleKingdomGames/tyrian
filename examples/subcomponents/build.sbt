lazy val root = (project in file("."))
  .dependsOn(tyrian)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaVersion                    := "3.1.0",
    name                            := "tyrian-subcomponents-example",
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  )

lazy val tyrian = ProjectRef(file("../.."), "tyrian")
