package tyrian

final case class LocationDetails(
    hash: Option[String],
    hostName: Option[String],
    pathName: String,
    port: Option[String],
    protocol: Option[String],
    search: Option[String],
    url: String
)

object LocationDetails:

  private val urlFileMatch = """^(file\:)\/\/(\/.*)?""".r
  private val urlDataMatch = """^(data\:)(.*)?""".r
  private val urlMatch     = """^([a-z]+\:)(\/+)?([a-zA-Z0-9-\.\@]+)(:)?([0-9]+)?(\/.*)?""".r

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

  def fromUrl(url: String): LocationDetails =
    url match
      case urlFileMatch(protocol, path) =>
        val p = parsePath(Option(path).getOrElse(""))

        LocationDetails(
          hash = p.hash,
          hostName = None,
          pathName = p.path,
          port = None,
          protocol = Option(protocol),
          search = p.search,
          url = url
        )

      case urlDataMatch(protocol, path) =>
        val p = parsePath(Option(path).getOrElse(""))

        LocationDetails(
          hash = p.hash,
          hostName = None,
          pathName = p.path,
          port = None,
          protocol = Option(protocol),
          search = p.search,
          url = url
        )

      case urlMatch(protocol, _, hostname, _, port, path) =>
        val p = parsePath(Option(path).getOrElse(""))

        LocationDetails(
          hash = p.hash,
          hostName = Option(hostname),
          pathName = p.path,
          port = Option(port),
          protocol = Option(protocol),
          search = p.search,
          url = url
        )

      case pathOnly =>
        val p = parsePath(Option(pathOnly).getOrElse(""))

        LocationDetails(
          hash = p.hash,
          hostName = None,
          pathName = p.path,
          port = None,
          protocol = None,
          search = p.search,
          url = url
        )

  final case class LocationPathDetails(path: String, search: Option[String], hash: Option[String])

  /** Is a String of the full rendered url address, minus the origin. e.g. /my-page?id=12#anchor */
  // val fullPath: String =
  //   origin match
  //     case None =>
  //       href

  //     case Some(o) =>
  //       href.replaceFirst(o, "")

  // /** The anchor in the url starting with '#' followed by the fragment of the URL. */
  // val hash: Option[String] =
  //   ???

  // /** The host, e.g. localhost:8080. */
  // val host: Option[String] =
  //   for {
  //     h <- hostName
  //     p <- port
  //   } yield s"$h:$p"

  // /** The name of host, e.g. localhost. */
  // val hostName: Option[String]

  // /** The whole URL. */
  // val href: String

  // /** The origin, e.g. http://localhost:8080. */
  // val origin: Option[String] =
  //   for {
  //     pr <- protocol
  //     ht <- host
  //   } yield pr + ht

  // /** Is the path minus hash anchors and query params, e.g. "/page1". */
  // val path: String

  // /** Is the port number of the URL, e.g. 80. */
  // val port: Option[String]

  // /** The protocol e.g. https:// */
  // val protocol: Option[String]

  // /** Is a String containing a '?' followed by the parameters of the URL. */
  // val search: Option[String]

  // /** The whole URL. */
  // val url: String = href
