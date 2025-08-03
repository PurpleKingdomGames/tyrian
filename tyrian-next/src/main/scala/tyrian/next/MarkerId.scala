package tyrian.next

/** Unique identifier for HTML markers used in out-of-order DOM construction. */
opaque type MarkerId = String

object MarkerId:
  given CanEqual[MarkerId, MarkerId] = CanEqual.derived

  /** Creates a MarkerId from a string identifier. */
  def apply(id: String): MarkerId = id

  extension (mid: MarkerId)
    /** Extracts the underlying string value of this MarkerId. */
    def value: String = mid
