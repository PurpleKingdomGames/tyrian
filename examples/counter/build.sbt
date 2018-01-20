lazy val root = (project in file("."))
  .dependsOn(scalm)
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(
    scalaVersion := "2.12.4",
    name := "scalm counter example",
    scalaJSUseMainModuleInitializer := true
  )

lazy val scalm = RootProject(uri("git://github.com/julienrf/scalm.git"))
