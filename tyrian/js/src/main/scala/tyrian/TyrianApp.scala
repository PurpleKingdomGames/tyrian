package tyrian

import org.scalajs.dom.Element

trait TyrianApp[Msg, Model]:

  def container: Element

  def init: (Model, Cmd[Msg])

  def update(msg: Msg, model: Model): (Model, Cmd[Msg])

  def view(model: Model): Html[Msg]

  def subscriptions(model: Model): Sub[Msg]

  def main(args: Array[String]): Unit =
    Tyrian.start(container, init, update, view, subscriptions)
