package tyrian

sealed trait Location:
  def locationDetails: LocationDetails
  def isInternal: Boolean
  def isExternal: Boolean
  def href: String
  def url: String

object Location extends LocationPlatform:

  final case class Internal(locationDetails: LocationDetails) extends Location:
    export locationDetails.*
    val isInternal: Boolean = true
    val isExternal: Boolean = false

  final case class External(locationDetails: LocationDetails) extends Location:
    export locationDetails.*
    val isInternal: Boolean = false
    val isExternal: Boolean = true

  /** Construct a Location from a given url, decides internal / external based on comparison with `currentLocation`
    */
  def fromUrl(url: String, currentLocation: Location.Internal): Location =
    val ld = LocationDetails.fromUrl(url)

    if ld.protocol.isEmpty then Location.Internal(ld)
    else if ld.origin == currentLocation.origin then Location.Internal(ld)
    else Location.External(ld)
