package tyrian

class LocationDetailsTests extends munit.FunSuite:

  test("Can be constructed from a URL") {

    val url = "http://localhost:8080/page2"

    val actual =
      LocationDetails.fromUrl(url)

    val expected =
      LocationDetails(
        hash = None,
        hostName = Some("localhost"),
        pathName = "/page2",
        port = Some("8080"),
        protocol = Some("http:"),
        search = None,
        url = url
      )

    assertEquals(actual, expected)
  }

  test("derived values: origin, host, and fullPath") {

    val url = "http://localhost:8080/page2?id=12#section"

    val actual =
      LocationDetails.fromUrl(url)

    val expected =
      LocationDetails(
        hash = Some("#section"),
        hostName = Some("localhost"),
        pathName = "/page2",
        port = Some("8080"),
        protocol = Some("http:"),
        search = Some("?id=12"),
        url = url
      )

    assertEquals(actual, expected)
    assertEquals(actual.host, Some("localhost:8080"))
    assertEquals(actual.origin, Some("http://localhost:8080"))
    assertEquals(actual.fullPath, "/page2?id=12#section")
  }

  test("check example locations parse correctly") {

    examples.toList.foreach { case (url, loc) =>
      assertEquals(LocationDetails.fromUrl(url), loc)
    }

  }

  val examples: Map[String, LocationDetails] =
    Map(
      "/" ->
        LocationDetails(
          hash = None,
          hostName = None,
          pathName = "/",
          port = None,
          protocol = None,
          search = None,
          url = "/"
        ),
      "/page-1" ->
        LocationDetails(
          hash = None,
          hostName = None,
          pathName = "/page-1",
          port = None,
          protocol = None,
          search = None,
          url = "/page-1"
        ),
      "foo/bar#baz" ->
        LocationDetails(
          hash = Option("#baz"),
          hostName = None,
          pathName = "foo/bar",
          port = None,
          protocol = None,
          search = None,
          url = "foo/bar#baz"
        ),
      "/static/images/photo.jpg?width=100&height=50" ->
        LocationDetails(
          hash = None,
          hostName = None,
          pathName = "/static/images/photo.jpg",
          port = None,
          protocol = None,
          search = Option("?width=100&height=50"),
          url = "/static/images/photo.jpg?width=100&height=50"
        ),
      "http://localhost:8080/page2" ->
        LocationDetails(
          hash = None,
          hostName = Option("localhost"),
          pathName = "/page2",
          port = Option("8080"),
          protocol = Option("http:"),
          search = None,
          url = "http://localhost:8080/page2"
        ),
      "https://www.example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("www.example.com"),
          pathName = "",
          port = None,
          protocol = Option("https:"),
          search = None,
          url = "https://www.example.com"
        ),
      "http://example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "",
          port = None,
          protocol = Option("http:"),
          search = None,
          url = "http://example.com"
        ),
      "ftp://ftp.example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("ftp.example.com"),
          pathName = "",
          port = None,
          protocol = Option("ftp:"),
          search = None,
          url = "ftp://ftp.example.com"
        ),
      "ssh://example.com:22" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "",
          port = Option("22"),
          protocol = Option("ssh:"),
          search = None,
          url = "ssh://example.com:22"
        ),
      "telnet://example.com:23" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "",
          port = Option("23"),
          protocol = Option("telnet:"),
          search = None,
          url = "telnet://example.com:23"
        ),
      "mailto:user@example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("user@example.com"),
          pathName = "",
          port = None,
          protocol = Option("mailto:"),
          search = None,
          url = "mailto:user@example.com"
        ),
      "https://example.com/path/to/page.html" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "/path/to/page.html",
          port = None,
          protocol = Option("https:"),
          search = None,
          url = "https://example.com/path/to/page.html"
        ),
      "http://example.com/path/to/page.php?param1=value1&param2=value2" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "/path/to/page.php",
          port = None,
          protocol = Option("http:"),
          search = Option("?param1=value1&param2=value2"),
          url = "http://example.com/path/to/page.php?param1=value1&param2=value2"
        ),
      "https://example.com#section1" ->
        LocationDetails(
          hash = Option("#section1"),
          hostName = Option("example.com"),
          pathName = "",
          port = None,
          protocol = Option("https:"),
          search = None,
          url = "https://example.com#section1"
        ),
      "http://example.com:8080/path/to/page.html" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "/path/to/page.html",
          port = Option("8080"),
          protocol = Option("http:"),
          search = None,
          url = "http://example.com:8080/path/to/page.html"
        ),
      "ftp://example.com:21/path/to/file.txt" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "/path/to/file.txt",
          port = Option("21"),
          protocol = Option("ftp:"),
          search = None,
          url = "ftp://example.com:21/path/to/file.txt"
        ),
      "http://subdomain.example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("subdomain.example.com"),
          pathName = "",
          port = None,
          protocol = Option("http:"),
          search = None,
          url = "http://subdomain.example.com"
        ),
      "https://www.example.co.uk" ->
        LocationDetails(
          hash = None,
          hostName = Option("www.example.co.uk"),
          pathName = "",
          port = None,
          protocol = Option("https:"),
          search = None,
          url = "https://www.example.co.uk"
        ),
      "http://example.net/path/to/page.html" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.net"),
          pathName = "/path/to/page.html",
          port = None,
          protocol = Option("http:"),
          search = None,
          url = "http://example.net/path/to/page.html"
        ),
      "ftp://ftp.example.net:21/path/to/file.txt" ->
        LocationDetails(
          hash = None,
          hostName = Option("ftp.example.net"),
          pathName = "/path/to/file.txt",
          port = Option("21"),
          protocol = Option("ftp:"),
          search = None,
          url = "ftp://ftp.example.net:21/path/to/file.txt"
        ),
      "https://example.org/path/to/page.php?param=value#section1" ->
        LocationDetails(
          hash = Option("#section1"),
          hostName = Option("example.org"),
          pathName = "/path/to/page.php",
          port = None,
          protocol = Option("https:"),
          search = Option("?param=value"),
          url = "https://example.org/path/to/page.php?param=value#section1"
        ),
      "http://example.org:8080/path/to/page.html?param=value#section1" ->
        LocationDetails(
          hash = Option("#section1"),
          hostName = Option("example.org"),
          pathName = "/path/to/page.html",
          port = Option("8080"),
          protocol = Option("http:"),
          search = Option("?param=value"),
          url = "http://example.org:8080/path/to/page.html?param=value#section1"
        ),
      "ftp://example.org:21/path/to/file.txt?param=value#section1" ->
        LocationDetails(
          hash = Option("#section1"),
          hostName = Option("example.org"),
          pathName = "/path/to/file.txt",
          port = Option("21"),
          protocol = Option("ftp:"),
          search = Option("?param=value"),
          url = "ftp://example.org:21/path/to/file.txt?param=value#section1"
        ),
      "http://localhost:8080/path/to/page.php?param=value#section1" ->
        LocationDetails(
          hash = Option("#section1"),
          hostName = Option("localhost"),
          pathName = "/path/to/page.php",
          port = Option("8080"),
          protocol = Option("http:"),
          search = Option("?param=value"),
          url = "http://localhost:8080/path/to/page.php?param=value#section1"
        ),
      "https://192.168.1.100:8443/path/to/page.html?param=value#section1" ->
        LocationDetails(
          hash = Option("#section1"),
          hostName = Option("192.168.1.100"),
          pathName = "/path/to/page.html",
          port = Option("8443"),
          protocol = Option("https:"),
          search = Option("?param=value"),
          url = "https://192.168.1.100:8443/path/to/page.html?param=value#section1"
        ),
      "http://www.example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("www.example.com"),
          pathName = "",
          port = None,
          protocol = Option("http:"),
          search = None,
          url = "http://www.example.com"
        ),
      "https://www.example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("www.example.com"),
          pathName = "",
          port = None,
          protocol = Option("https:"),
          search = None,
          url = "https://www.example.com"
        ),
      "ftp://ftp.example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("ftp.example.com"),
          pathName = "",
          port = None,
          protocol = Option("ftp:"),
          search = None,
          url = "ftp://ftp.example.com"
        ),
      "sftp://ftp.example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("ftp.example.com"),
          pathName = "",
          port = None,
          protocol = Option("sftp:"),
          search = None,
          url = "sftp://ftp.example.com"
        ),
      "ssh://example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "",
          port = None,
          protocol = Option("ssh:"),
          search = None,
          url = "ssh://example.com"
        ),
      "telnet://example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "",
          port = None,
          protocol = Option("telnet:"),
          search = None,
          url = "telnet://example.com"
        ),
      "mailto:user@example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("user@example.com"),
          pathName = "",
          port = None,
          protocol = Option("mailto:"),
          search = None,
          url = "mailto:user@example.com"
        ),
      "news://example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "",
          port = None,
          protocol = Option("news:"),
          search = None,
          url = "news://example.com"
        ),
      "gopher://example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "",
          port = None,
          protocol = Option("gopher:"),
          search = None,
          url = "gopher://example.com"
        ),
      "ldap://ldap.example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("ldap.example.com"),
          pathName = "",
          port = None,
          protocol = Option("ldap:"),
          search = None,
          url = "ldap://ldap.example.com"
        ),
      "smb://example.com/share/file.txt" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "/share/file.txt",
          port = None,
          protocol = Option("smb:"),
          search = None,
          url = "smb://example.com/share/file.txt"
        ),
      "nfs://example.com/share/file.txt" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "/share/file.txt",
          port = None,
          protocol = Option("nfs:"),
          search = None,
          url = "nfs://example.com/share/file.txt"
        ),
      "file:///path/to/local/file.html" ->
        LocationDetails(
          hash = None,
          hostName = None,
          pathName = "/path/to/local/file.html",
          port = None,
          protocol = Option("file:"),
          search = None,
          url = "file:///path/to/local/file.html"
        ),
      "data:text/plain;base64,SGVsbG8sIFdvcmxkIQ==" ->
        LocationDetails(
          hash = None,
          hostName = None,
          pathName = "text/plain;base64,SGVsbG8sIFdvcmxkIQ==",
          port = None,
          protocol = Option("data:"),
          search = None,
          url = "data:text/plain;base64,SGVsbG8sIFdvcmxkIQ=="
        ),
      "dns://example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "",
          port = None,
          protocol = Option("dns:"),
          search = None,
          url = "dns://example.com"
        ),
      "xmpp:user@example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("user@example.com"),
          pathName = "",
          port = None,
          protocol = Option("xmpp:"),
          search = None,
          url = "xmpp:user@example.com"
        )
    )
