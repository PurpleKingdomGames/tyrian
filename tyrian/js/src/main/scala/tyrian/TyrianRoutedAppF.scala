package tyrian

import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import org.scalajs.dom.Element
import org.scalajs.dom.PopStateEvent
import org.scalajs.dom.document
import org.scalajs.dom.window

import scala.scalajs.js
import scala.scalajs.js.annotation._

/** The TyrianApp trait can be extended to conveniently prompt you for all the methods needed for a Tyrian app, as well
  * as providing a number of standard app launching methods.
  */
trait TyrianRoutedAppF[F[_]: Async, Msg, Model] extends TyrianAppF[F, Msg, Model]:

  def router: Location => Msg

  private def routeCurrentLocation[F[_]: Async, Msg](router: Location => Msg): Cmd[F, Msg] =
    val task =
      Async[F].delay {
        Location.fromJsLocation(window.location)
      }
    Cmd.Run(task, router)

  private def _init(flags: Map[String, String]): (Model, Cmd[F, Msg]) =
    val (m, cmd) = init(flags)
    (m, cmd |+| routeCurrentLocation[F, Msg](router))

  private def _update(model: Model): Msg => (Model, Cmd[F, Msg]) =
    msg => update(model)(msg)

  private def _view(model: Model): Html[Msg] =
    view(model)

  private def _subscriptions(model: Model): Sub[F, Msg] =
    Routing.onUrlChange[F, Msg](router) |+| subscriptions(model)

  override def ready(node: Element, flags: Map[String, String]): Unit =
    run(
      Tyrian.start(
        node,
        router,
        _init(flags),
        _update,
        _view,
        _subscriptions,
        MaxConcurrentTasks
      )
    )

object Routing:

  def onUrlChange[F[_]: Async, Msg](router: Location => Msg): Sub[F, Msg] =
    def makeMsg = Option(router(Location.fromJsLocation(window.location)))
    Sub.Batch(
      Sub.fromEvent("DOMContentLoaded", window)(_ => makeMsg),
      Sub.fromEvent("popstate", window)(_ => makeMsg)
    )

object Nav:

  def pushUrl[F[_]: Async](url: String): Cmd[F, Nothing] =
    Cmd.SideEffect {
      window.history.pushState("", "", url)
    }

  def loadUrl[F[_]: Async](href: String): Cmd[F, Nothing] =
    Cmd.SideEffect {
      window.location.href = href
    }

trait LocationDetails:

  /** Is a String of the full rendered url address, minus the origin. e.g. /my-page?id=12#anchor */
  def fullPath: String =
    origin match
      case None =>
        href

      case Some(o) =>
        href.replaceFirst(o, "")

  /** The anchor in the url starting with '#' followed by the fragment of the URL. */
  def hash: Option[String]

  /** The protocol e.g. https:// */
  def protocol: Option[String]

  /** Is a String containing a '?' followed by the parameters of the URL. */
  def search: Option[String]

  /** The whole URL. */
  def href: String =
    origin.getOrElse("") + path + hash.getOrElse("") + search.getOrElse("")

  /** The whole URL. */
  def url: String = href

  /** The name of host, e.g. localhost. */
  def hostName: Option[String]

  /** Is the port number of the URL, e.g. 80. */
  def port: Option[String]

  /** Is the path minus hash anchors and query params, e.g. "/page1". */
  def path: String

  /** The host, e.g. localhost:8080. */
  def host: Option[String] =
    for {
      h <- hostName
      p <- port
    } yield s"$h:$p"

  /** The origin, e.g. http://localhost:8080. */
  def origin: Option[String] = 
    for {
      pr <- protocol
      ht <- host
    } yield pr + ht

enum Location:

  case Internal(
      hash: Option[String],
      hostName: Option[String],
      path: String,
      port: Option[String],
      protocol: Option[String],
      search: Option[String]
  ) extends Location with LocationDetails

  case External(
      hash: Option[String],
      hostName: Option[String],
      path: String,
      port: Option[String],
      protocol: Option[String],
      search: Option[String]
  ) extends Location with LocationDetails

object Location:

  extension (s: String) def optional: Option[String] = if s.isEmpty then None else Option(s)

  // TODO!
  def fromUrl(path: String): Location =
    fromUnknownUrl(path, fromJsLocation(window.location))

  def fromUnknownUrl(path: String, currentLocation: Location.Internal): Location =
    ???
    // Location.Internal(
    //   hash = location.hash.optional,
    //   host = location.host.optional,
    //   hostName = location.hostname.optional,
    //   href = location.href,
    //   origin = location.origin.toOption,
    //   path = location.pathname,
    //   port = location.port.optional,
    //   protocol = location.protocol.optional,
    //   search = location.search.optional
    // )

  /** Location instances created from JS Location's are assumed to be internal links.
    */
  def fromJsLocation(location: org.scalajs.dom.Location): Location.Internal =
    Location.Internal(
      hash = location.hash.optional,
      hostName = location.hostname.optional,
      path = location.pathname,
      port = location.port.optional,
      protocol = location.protocol.optional,
      search = location.search.optional
    )
