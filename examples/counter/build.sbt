lazy val root = (project in file("."))
  .dependsOn(scalm)
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(
    inThisBuild(
      List(
        scalaVersion := "2.12.4",
        version := "0.1.0-SNAPSHOT"
      )),
    name := "scalm counter example",
    scalaJSUseMainModuleInitializer := true
  )

lazy val scalm = RootProject(uri("git://github.com/julienrf/scalm.git"))
