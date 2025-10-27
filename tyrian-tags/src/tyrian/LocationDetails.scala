package tyrian

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/** Represents the deconstructed parts of a url.
  *
  * @param hash
  *   The anchor in the url starting with '#' followed by the fragment of the URL.
  * @param hostName
  *   The name of host, e.g. localhost.
  * @param pathName
  *   Is the path minus hash anchors and query params, e.g. "/page1".
  * @param port
  *   Is the port number of the URL, e.g. 80.
  * @param protocol
  *   The protocol e.g. https:
  * @param search
  *   Is a String containing a '?' followed by the parameters of the URL.
  * @param url
  *   The whole URL.
  */
final case class LocationDetails(
    hash: Option[String],
    hostName: Option[String],
    pathName: String,
    port: Option[String],
    protocol: Option[String],
    search: LocationDetails.SearchParams,
    url: String
):
  private val noSep = List("xmpp:", "data:", "mailto:")

  private def whichSeparator(protocol: String): String =
    if noSep.contains(protocol) then "" else "//"

  /** The host, e.g. localhost:8080. */
  val host: Option[String] =
    port match
      case None =>
        hostName

      case Some(p) =>
        hostName.map(h => s"$h:$p")

  /** The whole URL. */
  val href: String = url

  /** The origin, e.g. http://localhost:8080. */
  val origin: Option[String] =
    for {
      pr <- protocol
      ht <- host
    } yield pr + whichSeparator(pr) + ht

  /** Is a String of the full rendered url address, minus the origin. e.g. /my-page?id=12#anchor */
  val fullPath: String =
    origin match
      case None =>
        href

      case Some(o) =>
        href.replaceFirst(o, "")

object LocationDetails:

  private val urlFileMatch = """^(file\:)\/\/(\/.*)?""".r
  private val urlDataMatch = """^(data\:)(.*)?""".r
  private val urlMatch     = """^([a-z]+\:)(\/+)?([a-zA-Z0-9-\.\@]+)(:)?([0-9]+)?(.*)?""".r

  private val pathMatchAll    = """(.*)(\?.*)(#.*)""".r
  private val pathMatchHash   = """(.*)(#.*)""".r
  private val pathMatchSearch = """(.*)(\?.*)""".r

  private def parsePath(path: String): LocationPathDetails =
    path match
      case "" =>
        LocationPathDetails(path, None, None)

      case pathMatchAll(path, search, hash) =>
        LocationPathDetails(path, Option(search), Option(hash))

      case pathMatchHash(path, hash) =>
        LocationPathDetails(path, None, Option(hash))

      case pathMatchSearch(path, search) =>
        LocationPathDetails(path, Option(search), None)

      case _ =>
        LocationPathDetails(path, None, None)

  def parseQueryParams(rawQuery: String): SearchParams =
    val decoded = URLDecoder.decode(rawQuery, StandardCharsets.UTF_8.name())
    if decoded.startsWith("?") then
      val values = decoded.drop(1)
      SearchParams(
        values.split("&").toList.flatMap { param =>
          param.split("=", 2).toList match
            case key :: value :: Nil => List(QueryParam.Pair(key, value))
            case key :: Nil          => List(QueryParam.KeyOnly(key))
            case _                   => Nil
        }
      )
    else SearchParams.empty

  def fromUrl(url: String): LocationDetails =
    url match
      case urlFileMatch(protocol, path) =>
        val p = parsePath(Option(path).getOrElse(""))
        val q = p.search.map(parseQueryParams).getOrElse(LocationDetails.SearchParams.empty)

        LocationDetails(
          hash = p.hash,
          hostName = None,
          pathName = p.path,
          port = None,
          protocol = Option(protocol),
          search = q,
          url = url
        )

      case urlDataMatch(protocol, path) =>
        val p = parsePath(Option(path).getOrElse(""))
        val q = p.search.map(parseQueryParams).getOrElse(LocationDetails.SearchParams.empty)

        LocationDetails(
          hash = p.hash,
          hostName = None,
          pathName = p.path,
          port = None,
          protocol = Option(protocol),
          search = q,
          url = url
        )

      case urlMatch(protocol, _, hostname, _, port, path) =>
        val p = parsePath(Option(path).getOrElse(""))
        val q = p.search.map(parseQueryParams).getOrElse(LocationDetails.SearchParams.empty)

        LocationDetails(
          hash = p.hash,
          hostName = Option(hostname),
          pathName = p.path,
          port = Option(port),
          protocol = Option(protocol),
          search = q,
          url = url
        )

      case pathOnly =>
        val p = parsePath(Option(pathOnly).getOrElse(""))
        val q = p.search.map(parseQueryParams).getOrElse(LocationDetails.SearchParams.empty)

        LocationDetails(
          hash = p.hash,
          hostName = None,
          pathName = p.path,
          port = None,
          protocol = None,
          search = q,
          url = url
        )

  final case class LocationPathDetails(path: String, search: Option[String], hash: Option[String])

  final case class SearchParams(params: List[QueryParam]):
    def find(key: String): List[QueryParam] =
      params.filter(_.key == key)

    def hasKey(key: String): Boolean =
      params.exists(_.key == key)

    def keys: List[String] =
      params.map(_.key)

    def toTuples: List[(String, String)] =
      params.map {
        case QueryParam.KeyOnly(_key)     => (_key, "")
        case QueryParam.Pair(_key, value) => (_key, value)
      }

    def valueOf(key: String): List[String] =
      find(key).flatMap(_.valueOpt.toList)

  object SearchParams:
    val empty: SearchParams =
      SearchParams(Nil)

    def apply(params: QueryParam*): SearchParams =
      SearchParams(params.toList)

  enum QueryParam(val key: String) derives CanEqual:
    case KeyOnly(_key: String)             extends QueryParam(_key)
    case Pair(_key: String, value: String) extends QueryParam(_key)

    def valueOpt: Option[String] =
      this match
        case KeyOnly(_key)     => None
        case Pair(_key, value) => Some(value)

    def toTuple: (String, String) =
      this match
        case KeyOnly(_key)     => (_key, "")
        case Pair(_key, value) => (_key, value)

  object QueryParam:

    def apply(key: String, value: String): QueryParam =
      QueryParam.Pair(key, value)

    def apply(key: String): QueryParam =
      QueryParam.KeyOnly(key)
