package tyrian

extension (l: Location.type)
  /** Location instances created from JS Location's are assumed to be internal links.
    */
  def fromJsLocation(location: org.scalajs.dom.Location): Location.Internal =
    Location.Internal(
      LocationDetails.fromUrl(location.href)
    )
