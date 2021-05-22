lazy val root = (project in file("."))
  .dependsOn(scalm)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaVersion := "3.0.0",
    name := "scalm-http-example",
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core",
      "io.circe" %%% "circe-parser"
    ).map(_ % "0.14.0-M7")
  )

lazy val scalm = ProjectRef(file("../.."), "scalm")
