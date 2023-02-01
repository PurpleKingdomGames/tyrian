package tyrian.runtime

final case class ModelHolder[Model](model: Model, updated: Boolean)
