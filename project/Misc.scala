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

  def copyApiDocsTaskDefinition(scalaVersion: String, file: File): Unit = {

    println(s"Copy docs from 'target/scala-$scalaVersion/unidoc' to 'target/scala-$scalaVersion/site-docs/api'")

    val src = file / s"scala-$scalaVersion" / "unidoc"
    val dst = file / s"scala-$scalaVersion" / "site-docs" / "api"

    IO.copyDirectory(src, dst)
  }

  lazy val customTasksAliases = Seq(
    UsefulTask("a", "cleanAll", "Clean all (JS + JVM)"),
    UsefulTask("b", "compileAll", "Compile all (JS + JVM)"),
    UsefulTask("c", "testAll", "Test all (JS + JVM)"),
    UsefulTask("d", "localPublish", "Locally publish the core modules (JS + JVM)"),
    UsefulTask("e", "sandboxBuild", "Build the sandbox project"),
    UsefulTask("f", "indigoSandboxBuild", "Build the indigo/tyrian bridge project"),
    UsefulTask("g", "gendocs", "Rebuild the API and markdown docs"),
    UsefulTask("h", "code", "Launch VSCode")
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
