import sbt.{Def, _}
import sbtwelcome.UsefulTask
import sbtwelcome.WelcomePlugin.autoImport._

import scala.sys.process._

object Misc {

  lazy val code =
    taskKey[Unit]("Launch VSCode in the current directory")

  // Define task to copy html files
  lazy val copyApiDocs =
    taskKey[Unit]("Copy html files from src/main/html to cross-version target directory")

  def codeTaskDefinition: Int = {
    val command = Seq("code", ".")
    val run = sys.props("os.name").toLowerCase match {
      case x if x contains "windows" => Seq("cmd", "/C") ++ command
      case _                         => command
    }
    run.!
  }

  def copyApiDocsTaskDefinition(scalaVersion: String, fromFileTarget: File, toFileTarget: File): Unit = {
    val fromTargetPath = fromFileTarget.getCanonicalFile().toString()
    val toTargetPath   = toFileTarget.getCanonicalFile().toString()

    println(
      s"Copy docs from '$fromTargetPath/scala-$scalaVersion/unidoc' to '$toTargetPath/unidocs/site-docs/api'"
    )

    val src = fromFileTarget / s"scala-$scalaVersion" / "unidoc"
    val dst = toFileTarget / s"unidocs" / "site-docs" / "api"

    IO.copyDirectory(src, dst)
  }

  lazy val customTasksAliases = Seq(
    UsefulTask("cleanAll", "Clean all (JS + JVM)"),
    UsefulTask("compileAll", "Compile all (JS + JVM)"),
    UsefulTask("testAllUnit", "Test all unit (JS + JVM)"),
    UsefulTask("testAll", "Test all (JS + JVM)"),
    UsefulTask("localPublish", "Locally publish the core modules (JS + JVM)"),
    UsefulTask("sandboxBuild", "Build the sandbox project"),
    UsefulTask("sandboxZIOBuild", "Build the zio sandbox project"),
    UsefulTask("sandboxSSRBuild", "Build the sandbox SSR project"),
    UsefulTask("sandboxSSRServer", "Run the sandbox SSR server"),
    UsefulTask("gendocs", "Rebuild the API and markdown docs"),
    UsefulTask("code", "Launch VSCode"),
    UsefulTask("scalafmtCheckAll", "")
  )

  def logoSettings(version: SettingKey[String]): Seq[Def.Setting[String]] = {
    val rawLogo: String =
      """
        |  _____         _           
        | |_   _|  _ _ _(_)__ _ _ _  
        |   | || || | '_| / _` | ' \ 
        |   |_| \_, |_| |_\__,_|_||_|
        |       |__/                 
        |""".stripMargin

    Seq(
      logo             := rawLogo + s"version ${version.value}",
      logoColor        := scala.Console.MAGENTA,
      aliasColor       := scala.Console.BLUE,
      commandColor     := scala.Console.CYAN,
      descriptionColor := scala.Console.WHITE
    )
  }
}
