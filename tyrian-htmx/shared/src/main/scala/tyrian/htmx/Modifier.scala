package tyrian.htmx

enum QueueType(val name: String):
  case First extends QueueType("first")
  case Last  extends QueueType("last")
  case All   extends QueueType("all")

enum Modifier(val name: String):
  case Changed                     extends Modifier("changed")
  case Once                        extends Modifier("once")
  case Consume                     extends Modifier("consume")
  case Delay(interval: String)     extends Modifier("delay:" + interval)
  case From(cssSelector: String)   extends Modifier("from" + cssSelector)
  case Target(cssSelector: String) extends Modifier("target:" + cssSelector)
  case Throttle(interval: String)  extends Modifier("throttle:" + interval)
  case Queue(tpe: QueueType)       extends Modifier("queue:" + tpe.name)
  case Root(cssSelector: String)   extends Modifier("root:" + cssSelector)
  case Threshold(value: Float)     extends Modifier("threshold:" + value.toString)
