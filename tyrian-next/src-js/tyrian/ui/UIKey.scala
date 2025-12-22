package tyrian.ui

opaque type UIKey = String

object UIKey:

  given CanEqual[UIKey, UIKey] = CanEqual.derived

  def apply(value: String): UIKey = value

  extension (key: UIKey) def value: String = key
