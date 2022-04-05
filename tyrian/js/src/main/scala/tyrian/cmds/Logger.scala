package tyrian.cmds

import cats.effect.kernel.Async
import tyrian.Cmd

import scala.collection.mutable.ArrayBuffer

/** A very, very simple logger that logs to the Browsers console with a few standard headers and the log message.
  */
object Logger:

  private val INFO: String  = "INFO"
  private val ERROR: String = "ERROR"
  private val DEBUG: String = "DEBUG"

  private val errorLogs: ArrayBuffer[String] = new ArrayBuffer[String]()
  private val debugLogs: ArrayBuffer[String] = new ArrayBuffer[String]()

  private def formatMessage(level: String, message: String): String =
    s"""[$level] [Tyrian] $message"""

  private val consoleLogString: String => Unit = message => println(message)

  private val infoString: String => Unit = message => println(formatMessage(INFO, message))

  private val errorString: String => Unit = message => println(formatMessage(ERROR, message))

  private val errorOnceString: String => Unit = message =>
    if (!errorLogs.contains(message)) {
      errorLogs += message
      println(formatMessage(ERROR, message))
    }

  private val debugString: String => Unit = message => println(formatMessage(DEBUG, message))

  private val debugOnceString: String => Unit = message =>
    if (!debugLogs.contains(message)) {
      debugLogs += message
      println(formatMessage(DEBUG, message))
    }

  def consoleLog[F[_]: Async](messages: String*): Cmd.SideEffect[F] =
    Cmd.SideEffect {
      consoleLogString(messages.toList.mkString(", "))
    }

  def info[F[_]: Async](messages: String*): Cmd.SideEffect[F] =
    Cmd.SideEffect {
      infoString(messages.toList.mkString(", "))
    }

  def error[F[_]: Async](messages: String*): Cmd.SideEffect[F] =
    Cmd.SideEffect {
      errorString(messages.toList.mkString(", "))
    }

  def errorOnce[F[_]: Async](messages: String*): Cmd.SideEffect[F] =
    Cmd.SideEffect {
      errorOnceString(messages.toList.mkString(", "))
    }

  def debug[F[_]: Async](messages: String*): Cmd.SideEffect[F] =
    Cmd.SideEffect {
      debugString(messages.toList.mkString(", "))
    }

  def debugOnce[F[_]: Async](messages: String*): Cmd.SideEffect[F] =
    Cmd.SideEffect {
      debugOnceString(messages.toList.mkString(", "))
    }
