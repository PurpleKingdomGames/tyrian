package tyrian

/** A component is a typical structure that emerges from your code when you start modularizing it. You don’t *have to*
  * break down your code into components, but sometimes you do and this class is useful to abstract over them.
  *
  * A component defines how to maintain and render a state.
  *
  * In the case of a counter, for instance, the state would be just an `Int`, and the component would to react to an
  * `Increment` message to increment the current state.
  */
trait Component { parent =>

  /** Type of state of the component
    */
  type Model

  /** Type of messages that this component reacts to
    */
  type Msg

  /** @return
    *   An HTML view of the given model
    * @param model
    *   the model to render
    */
  def view(model: Model): Html[Msg]

  /** Performs a state transition: updates the current state and optionally apply a side-effect (such as performing an
    * XHR).
    *
    * @return
    *   The new value of the model and a command to execute for this state transition
    * @param msg
    *   the message to react to
    * @param model
    *   the current state
    */
  def update(msg: Msg, model: Model): (Model, Cmd[Msg])

  /** @return
    *   The subscriptions of this component for the given state
    * @param model
    *   current state
    */
  def subscriptions(model: Model): Sub[Msg]

  trait Child:

    val child: Component
    def msgCtor: child.Msg => parent.Msg
    def extractor: PartialFunction[(parent.Model, parent.Msg), (child.Model, child.Msg)]

    def html(h: Html[child.Msg]): Html[parent.Msg] = h.map(msgCtor)
    def cmd(c: Cmd[child.Msg]): Cmd[parent.Msg]    = c.map(msgCtor)
    def sub(s: Sub[child.Msg]): Sub[parent.Msg]    = s.map(msgCtor)

    def view(childModel: child.Model): Html[parent.Msg] =
      child.view(childModel).map(msgCtor)

    def update(
        model: parent.Model,
        msg: parent.Msg,
        modelCtor: child.Model => parent.Model
    ): (parent.Model, Cmd[parent.Msg]) =
      given CanEqual[Option[(child.Model, child.Msg)], Option[(child.Model, child.Msg)]] = CanEqual.derived
      extractor.lift((model, msg)) match {
        case Some((childModel, childMsg)) => modelAndCmd(child.update(childMsg, childModel), modelCtor)
        case None                         => pure(model)
      }

    def subscriptions(childModel: child.Model): Sub[parent.Msg] =
      child.subscriptions(childModel).map(msgCtor)

    def modelAndCmd(
        s: (child.Model, Cmd[child.Msg]),
        modelCtor: child.Model => parent.Model
    ): (parent.Model, Cmd[parent.Msg]) = {
      val (model, cmd) = s
      (modelCtor(model), cmd.map(msgCtor))
    }

  end Child

  object Child:
    def apply(
        _child: Component
    )(
        _extractor: PartialFunction[(parent.Model, parent.Msg), (_child.Model, _child.Msg)],
        _msgCtor: _child.Msg => parent.Msg
    ): Child { val child: _child.type } =
      new Child {
        val child: _child.type = _child
        def extractor          = _extractor
        def msgCtor            = _msgCtor
      }

}
