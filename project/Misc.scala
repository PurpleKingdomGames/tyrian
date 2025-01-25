import sbt.{Def, *}
import sbtwelcome.UsefulTask
import sbtwelcome.WelcomePlugin.autoImport.*

import scala.sys.process.*

object Misc {

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
