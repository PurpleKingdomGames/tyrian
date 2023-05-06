package tyrian

import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import org.scalajs.dom.Element
import org.scalajs.dom.document
import org.scalajs.dom.window
import tyrian.runtime.TyrianRuntime

import scala.scalajs.js
import scala.scalajs.js.annotation._

/** The TyrianApp trait can be extended to conveniently prompt you for all the methods needed for a Tyrian app, as well
  * as providing a number of standard app launching methods.
  */
trait TyrianRoutedAppF[F[_]: Async, Msg, Model] extends TyrianAppF[F, Msg, Model]:

  def router: Location => Msg

  private def _init(flags: Map[String, String]): (Model, Cmd[F, Msg]) =
    val (m, cmd) = init(flags)
    (m, Routing.getLocation[F, Msg](router) |+| cmd)

  private def _update(model: Model): Msg => (Model, Cmd[F, Msg]) =
    msg => update(model)(msg)

  private def _view(model: Model): Html[Msg] =
    view(model)

  private def _subscriptions(model: Model): Sub[F, Msg] =
    Routing.onLocationChange[F, Msg](router) |+| subscriptions(model)

  override def ready(node: Element, flags: Map[String, String]): Unit =
    run(
      Tyrian.start(
        node,
        _init(flags),
        _update,
        _view,
        _subscriptions,
        MaxConcurrentTasks
      )
    )

object Routing:

  private def jsLocationToLocation(loc: org.scalajs.dom.Location): Location =
    Location(
      _url = loc.toString,
      _hash = loc.hash,
      _protocol = loc.protocol,
      _search = loc.search,
      _href = loc.href,
      _hostname = loc.hostname,
      _port = loc.port,
      _pathname = loc.pathname,
      _host = loc.host,
      _origin = loc.origin.toOption
    )

  def onLocationChange[F[_]: Async, Msg](router: Location => Msg): Sub[F, Msg] =
    def makeMsg = Option(router(jsLocationToLocation(window.location)))

    Sub.Batch(
      Sub.fromEvent("DOMContentLoaded", window)(_ => makeMsg),
      Sub.fromEvent("popstate", window)(_ => makeMsg)
    )

  def getLocation[F[_]: Async, Msg](router: Location => Msg): Cmd[F, Msg] =
    val task =
      Async[F].delay {
        jsLocationToLocation(window.location)
      }
    Cmd.Run(task, router)

  private val emptyObj: js.Object = new js.Object

  def setLocation[F[_]: Async](newLocation: String): Cmd[F, Nothing] =
    Cmd.SideEffect {
      window.history.pushState(emptyObj, "", newLocation)
    }

/** The Location interface represents the location of the object it is linked to. Changes done on it are reflected on
  * the object it relates to. Both the Document and Window interface have such a linked Location, accessible via
  * Document.location and Window.location respectively.
  *
  * This `Location` type is mostly a paired back version of Scala.js's: org.scalajs.dom.Location, right down to the
  * scaladocs! However, field names have been changed to camel case to be more Scala consitent, and additional fields
  * `url` and `fullPath` have been added for convenience.
  */
final case class Location(
    private val _url: String,
    private val _hash: String,
    private val _protocol: String,
    private val _search: String,
    private val _href: String,
    private val _hostname: String,
    private val _port: String,
    private val _pathname: String,
    private val _host: String,
    private val _origin: Option[String]
):
  /** Is a String of the full rendered url address. */
  val url: String = _url

  /** Is a String of the full rendered url address, minus the origin. e.g. /my-page?id=12#anchor */
  val fullPath: String =
    _origin match
      case None =>
        _url

      case Some(o) =>
        _url.replaceFirst(o, "")

  /** Is a String containing a '#' followed by the fragment identifier of the URL. */
  val hash: String = _hash

  /** Is a String containing the protocol scheme of the URL, including the final ':'. */
  val protocol: String = _protocol

  /** Is a String containing a '?' followed by the parameters of the URL. */
  val search: String = _search

  /** Is a String containing the whole URL. */
  val href: String = _href

  /** Is a String containing the domain of the URL. */
  val hostName: String = _hostname

  /** Is a String containing the port number of the URL. */
  val port: String = _port

  /** Is a String containing an initial '/' followed by the path of the URL. */
  val pathName: String = _pathname

  /** Is a String containing the host, that is the hostname, a ':', and the port of the URL. */
  val host: String = _host

  /** The origin read-only property is a String containing the Unicode serialization of the origin of the represented
    * URL, that is, for http and https, the scheme followed by '://', followed by the domain, followed by ':', followed
    * by the port (the default port, 80 and 443 respectively, if explicitly specified). For URL using file: scheme, the
    * value is browser dependant.
    *
    * This property also does not exist consistently on IE, even as new as IE11, hence it must be optional.
    */
  def origin: Option[String] = _origin
