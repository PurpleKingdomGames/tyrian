package tyrian

trait LocationPlatform:

  /** Location instances created from JS Location's are assumed to be internal links.
    */
  def fromJsLocation(location: org.scalajs.dom.Location): Location.Internal =
    Location.Internal(
      LocationDetails.fromUrl(location.href)
    )
