package example

import cats.effect.IO
import tyrian.Html.*
import tyrian.*

object ChatApp extends TyrianIOApp[ChatAppMsg, ChatAppModel]:

  def router: Location => ChatAppMsg = Routing.none(ChatAppMsg.NoOp)

  def init(flags: Map[String, String]): (ChatAppModel, Cmd[IO, ChatAppMsg]) =
    val initialChat = flags.get("InitialMessage").getOrElse("")
    (ChatAppModel(chatInput = initialChat, messages = Seq()), Cmd.None)

  def update(
      model: ChatAppModel
  ): ChatAppMsg => (ChatAppModel, Cmd[IO, ChatAppMsg]) =
    case ChatAppMsg.ChatInput(input) =>
      (model.copy(chatInput = input), Cmd.None)

    case ChatAppMsg.SendChat =>
      (
        model.copy(
          chatInput = "",
          messages = model.messages :+ model.chatInput
        ),
        Cmd.None
      )

    case ChatAppMsg.NoOp =>
      (model, Cmd.None)

  def view(model: ChatAppModel): Html[ChatAppMsg] =
    div(
      ul()(
        for { message <- model.messages.toList } yield li()(message)
      ),
      input(onInput(ChatAppMsg.ChatInput.apply), value := model.chatInput),
      button(onClick(ChatAppMsg.SendChat))("Send Chat")
    )

  def subscriptions(model: ChatAppModel): Sub[IO, ChatAppMsg] =
    Sub.None

final case class ChatAppModel(chatInput: String, messages: Seq[String])

enum ChatAppMsg:
  case NoOp
  case ChatInput(input: String)
  case SendChat
