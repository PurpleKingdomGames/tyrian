package example

import cats.effect.IO
import tyrian.Html.*
import tyrian.*

import scala.scalajs.js
import scala.scalajs.js.annotation.*

object ChatApp extends TyrianApp[ChatAppMsg, ChatAppModel]:

  def init(flags: Map[String, String]): (ChatAppModel, Cmd[IO, ChatAppMsg]) =
    val initialChat = flags.get("InitialMessage").getOrElse("")
    (ChatAppModel(chatInput = initialChat, messages = Seq()), Cmd.None)

  def update(model: ChatAppModel): ChatAppMsg => (ChatAppModel, Cmd[IO, ChatAppMsg]) =
    case ChatInput(input) => (model.copy(chatInput = input), Cmd.None)
    case SendChat() => (model.copy(chatInput = "", messages = model.messages :+ model.chatInput), Cmd.None)
    case NoOp() => (model, Cmd.None)

  def view(model: ChatAppModel): Html[ChatAppMsg] =
    div(
      ul()(
        for { message <- model.messages.toList }
          yield li()(message)
      ),
      input(onInput(ChatInput.apply), value := model.chatInput),
      button(onClick(SendChat()))("Send Chat")
    )

  def router: Location => ChatAppMsg =
    _ => NoOp()

  def subscriptions(model: ChatAppModel): Sub[IO, ChatAppMsg] =
    Sub.None

case class ChatAppModel(chatInput: String, messages: Seq[String])

sealed abstract class ChatAppMsg
case class NoOp() extends ChatAppMsg
case class ChatInput(input: String) extends ChatAppMsg
case class SendChat() extends ChatAppMsg

