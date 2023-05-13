package tyrian

import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import org.scalajs.dom.Element
import org.scalajs.dom.PopStateEvent
import org.scalajs.dom.document
import org.scalajs.dom.window

import scala.scalajs.js
import scala.scalajs.js.annotation._

sealed trait Location:
  def locationDetails: LocationDetails
  def isInternal: Boolean
  def isExternal: Boolean

object Location:

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

  /** Location instances created from JS Location's are assumed to be internal links.
    */
  def fromJsLocation(location: org.scalajs.dom.Location): Location.Internal =
    Location.Internal(
      LocationDetails.fromUrl(location.href)
    )
