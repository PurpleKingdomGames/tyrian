package tyrian.htmx

case class Trigger(
    event: Event,
    filter: Option[String],
    modifiers: List[Modifier]
) {
  def render: String =
    val eventString  = event.name
    val filterString = filter.map(f => s"[$f]").getOrElse("")
    val modifiersString = modifiers
      .map(_.name)
      .mkString(" ", " ", "")
    s"$eventString$filterString$modifiersString"

  def withFilter(filter: String): Trigger = this.copy(filter = Some(filter))

  def withModifiers(newModifiers: Modifier*): Trigger =
    this.copy(modifiers = modifiers ++ newModifiers)
}

object Trigger:
  def apply(event: Event): Trigger = Trigger(event, None, List())
