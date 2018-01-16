package example

import scalm._
import scalm.Html._
import org.scalajs.dom.document

object Main extends App {

  def main(args: Array[String]): Unit = Scalm.start(this, document.body)


  // MODEL


  type Model = Int

  def init: (Model, Cmd[Msg]) = (0, Cmd.Empty)

  sealed trait Msg


  // UPDATE


  def update(msg: Msg, model: Model): (Model, Cmd[Msg]) =
    (model, Cmd.Empty)


  // VIEW


  def view(model: Model): Html[Msg] =
    div()(

    )


  // SUBSCRIPTION


  def subscriptions(model: Int): Sub[Msg] = Sub.Empty
}