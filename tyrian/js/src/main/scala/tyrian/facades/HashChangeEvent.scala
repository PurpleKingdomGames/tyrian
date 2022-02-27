package tyrian.facades

import org.scalajs.dom.window
import tyrian.Sub

import scala.scalajs.js

@js.native
trait HashChangeEvent extends js.Object:
  def newURL: String = js.native
  def oldURL: String = js.native
