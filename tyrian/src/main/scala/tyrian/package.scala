package tyrian

/** Tyrian is a user interface library inspired by Elm.
  */
def pure[Model](model: Model): (Model, Cmd[Nothing]) = (model, Cmd.Empty)
