package tyrian

class LocationTests extends munit.FunSuite:

  val internal: Location.Internal =
    Location.Internal(
      hash = None,
      hostName = Some("localhost"),
      path = "/",
      port = Some("8080"),
      protocol = Some("https:"),
      search = None
    )

  test("Internal location renders correctly") {
    assertEquals(internal.href, "https://localhost:8080/")
  }

  test("A more complicated location renders correctly") {

    val actual =
      Location.Internal(
        hash = Some("#fragment"),
        hostName = Some("localhost"),
        path = "/blog/posts/1234",
        port = Some("8080"),
        protocol = Some("https:"),
        search = Some("?id=12&q=fish")
      )

    assertEquals(actual.href, "https://localhost:8080/blog/posts/1234#fragment?id=12&q=fish")
  }

  val examples: Map[String, Location] =
    Map(
      "/" ->
        Location.Internal(
          hash = None,
          hostName = None,
          path = "/",
          port = None,
          protocol = None,
          search = None
        ),
      "/page-1" ->
        Location.Internal(
          hash = None,
          hostName = None,
          path = "/page-1",
          port = None,
          protocol = None,
          search = None
        )
      // "foo/bar#baz"                                                       -> Location(),
      // "/static/images/photo.jpg?width=100&height=50"                      -> Location(),
      // "http://localhost:8080/page2"                                       -> Location(),
      // "https://www.example.com"                                           -> Location(),
      // "http://example.com"                                                -> Location(),
      // "ftp://ftp.example.com"                                             -> Location(),
      // "ssh://example.com:22"                                              -> Location(),
      // "telnet://example.com:23"                                           -> Location(),
      // "mailto:user@example.com"                                           -> Location(),
      // "https://example.com/path/to/page.html"                             -> Location(),
      // "http://example.com/path/to/page.php?param1=value1&param2=value2"   -> Location(),
      // "https://example.com#section1"                                      -> Location(),
      // "http://example.com:8080/path/to/page.html"                         -> Location(),
      // "ftp://example.com:21/path/to/file.txt"                             -> Location(),
      // "http://subdomain.example.com"                                      -> Location(),
      // "https://www.example.co.uk"                                         -> Location(),
      // "http://example.net/path/to/page.html"                              -> Location(),
      // "ftp://ftp.example.net:21/path/to/file.txt"                         -> Location(),
      // "https://example.org/path/to/page.php?param=value#section1"         -> Location(),
      // "http://example.org:8080/path/to/page.html?param=value#section1"    -> Location(),
      // "ftp://example.org:21/path/to/file.txt?param=value#section1"        -> Location(),
      // "http://localhost:8080/path/to/page.php?param=value#section1"       -> Location(),
      // "https://192.168.1.100:8443/path/to/page.html?param=value#section1" -> Location(),
      // "http://www.example.com"                                            -> Location(),
      // "https://www.example.com"                                           -> Location(),
      // "ftp://ftp.example.com"                                             -> Location(),
      // "sftp://ftp.example.com"                                            -> Location(),
      // "ssh://example.com"                                                 -> Location(),
      // "telnet://example.com"                                              -> Location(),
      // "mailto:user@example.com"                                           -> Location(),
      // "news://example.com"                                                -> Location(),
      // "gopher://example.com"                                              -> Location(),
      // "ldap://ldap.example.com"                                           -> Location(),
      // "smb://example.com/share/file.txt"                                  -> Location(),
      // "nfs://example.com/share/file.txt"                                  -> Location(),
      // "file:///path/to/local/file.html"                                   -> Location(),
      // "data:text/plain;base64,SGVsbG8sIFdvcmxkIQ=="                       -> Location(),
      // "irc://irc.example.com/channel"                                     -> Location(),
      // "dns://example.com"                                                 -> Location(),
      // "xmpp:user@example.com"                                             -> Location(),
      // "magnet:?xt=urn:btih:ABCD1234"                                      -> Location(),
      // "steam://run/440"                                                   -> Location(),
      // "magnet:?xt=urn:btih:ABCD1234"                                      -> Location()
    )

  test("check example locations parse correctly") {

    examples.toList.foreach { case (url, loc) =>
      assertEquals(Location.fromUnknownUrl(url, internal), loc)
    }

  }
