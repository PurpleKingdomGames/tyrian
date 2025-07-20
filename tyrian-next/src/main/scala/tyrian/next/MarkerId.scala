package tyrian.next

opaque type MarkerId = String
object MarkerId:
  given CanEqual[MarkerId, MarkerId] = CanEqual.derived

  def apply(id: String): MarkerId             = id
  extension (mid: MarkerId) def value: String = mid
