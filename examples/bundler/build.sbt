ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"

lazy val root = (project in file("."))
  .dependsOn(tyrian)
  .enablePlugins(ScalaJSBundlerPlugin)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaVersion                    := "3.1.0",
    name                            := "tyrian-counter-example",
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    Compile / npmDependencies += "snabbdom" -> "3.0.1",
    scalafixOnCompile                       := true,
    semanticdbEnabled                       := true,
    semanticdbVersion                       := scalafixSemanticdb.revision,
    // Source maps seem to be broken with bundler
    Compile / fastOptJS / scalaJSLinkerConfig ~= { _.withSourceMap(false) }, 
    Compile / fullOptJS / scalaJSLinkerConfig ~= { _.withSourceMap(false) }
  )

lazy val tyrian = ProjectRef(file("../.."), "tyrian")
