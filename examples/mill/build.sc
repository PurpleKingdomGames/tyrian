import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`
import mill._
import mill.scalalib._
import mill.scalajslib._
import mill.scalajslib.api._

object counter extends ScalaJSModule {
  def scalaVersion   = "3.1.1"
  def scalaJSVersion = "1.8.0"

  def buildSite() =
    T.command {
      T {
        compile()
        fastOpt()
      }
    }

  def ivyDeps =
    Agg(
      ivy"io.indigoengine::tyrian::${TyrianVersion.getVersion}"
    )

  def scalacOptions = super.scalacOptions() ++ ScalacOptions.compile

  override def moduleKind = T(mill.scalajslib.api.ModuleKind.CommonJSModule)

  object test extends Tests {
    def ivyDeps = Agg(
      ivy"org.scalameta::munit::0.7.29"
    )

    def testFramework = "munit.Framework"

    override def moduleKind = T(mill.scalajslib.api.ModuleKind.CommonJSModule)
    override def jsEnvConfig = T(
      JsEnvConfig.NodeJs(args = List("--dns-result-order=ipv4first"))
    )

    def scalacOptions = super.scalacOptions() ++ ScalacOptions.test
  }

}

object ScalacOptions {

  lazy val compile: Seq[String] =
    Seq(
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-encoding",
      "utf-8",                  // Specify character encoding used by source files.
      "-feature",               // Emit warning and location for usages of features that should be imported explicitly.
      "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
      "-language:experimental.macros", // Allow macro definition (besides implementation and application)
      "-language:higherKinds",         // Allow higher-kinded types
      "-language:implicitConversions", // Allow definition of implicit functions called views
      "-unchecked",                    // Enable additional warnings where generated code depends on assumptions.
      "-Xfatal-warnings"               // Fail the compilation if there are any warnings.
      // "-language:strictEquality"       // Scala 3 - Multiversal Equality
    )

  lazy val test: Seq[String] =
    Seq(
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-encoding",
      "utf-8",                  // Specify character encoding used by source files.
      "-feature",               // Emit warning and location for usages of features that should be imported explicitly.
      "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
      "-language:experimental.macros", // Allow macro definition (besides implementation and application)
      "-language:higherKinds",         // Allow higher-kinded types
      "-language:implicitConversions", // Allow definition of implicit functions called views
      "-unchecked"                     // Enable additional warnings where generated code depends on assumptions.
    )

}

object TyrianVersion {
  def getVersion: String = {
    def rec(path: String, levels: Int, version: Option[String]): String = {
      val msg = "ERROR: Couldn't find Tyrian version."
      version match {
        case Some(v) =>
          println(s"""Tyrian version set to '$v'""")
          v

        case None if levels < 3 =>
          try {
            val v = scala.io.Source.fromFile(path).getLines.toList.head
            rec(path, levels, Some(v))
          } catch {
            case _: Throwable =>
              rec("../" + path, levels + 1, None)
          }

        case None =>
          println(msg)
          throw new Exception(msg)
      }
    }

    rec(".tyrian-version", 0, None)
  }
}
