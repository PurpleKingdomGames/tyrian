package scalm

/** Scalm is a user interface library inspired by Elm.
  */
def pure[Model](model: Model): (Model, Cmd[Nothing]) = (model, Cmd.Empty)
