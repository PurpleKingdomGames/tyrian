/** Scalm is a user interface library inspired by Elm.
  */
package object scalm {

  def pure[Model](model: Model): (Model, Cmd[Nothing]) = (model, Cmd.Empty)

}
