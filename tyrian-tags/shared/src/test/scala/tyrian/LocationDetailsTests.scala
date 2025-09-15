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
        search = LocationDetails.SearchParams.empty,
        url = url
      )

    assertEquals(actual, expected)
  }

  test("derived values: origin, host, and fullPath") {

    val url = "http://localhost:8080/page2?id=12&id=14&name=Joe%20KD6-3.7&valid#section"

    val actual =
      LocationDetails.fromUrl(url)

    val expected =
      LocationDetails(
        hash = Some("#section"),
        hostName = Some("localhost"),
        pathName = "/page2",
        port = Some("8080"),
        protocol = Some("http:"),
        search = LocationDetails.SearchParams(
          LocationDetails.QueryParam("id", "12"),
          LocationDetails.QueryParam("id", "14"),
          LocationDetails.QueryParam("name", "Joe KD6-3.7"),
          LocationDetails.QueryParam("valid")
        ),
        url = url
      )

    assertEquals(actual, expected)
    assertEquals(actual.host, Some("localhost:8080"))
    assertEquals(actual.origin, Some("http://localhost:8080"))
    assertEquals(actual.fullPath, "/page2?id=12&id=14&name=Joe%20KD6-3.7&valid#section")

    // Query params
    assertEquals(
      actual.search,
      LocationDetails.SearchParams(
        LocationDetails.QueryParam("id", "12"),
        LocationDetails.QueryParam("id", "14"),
        LocationDetails.QueryParam("name", "Joe KD6-3.7"),
        LocationDetails.QueryParam("valid")
      )
    )
    assertEquals(actual.search.find("name").map(_.toTuple), List("name" -> "Joe KD6-3.7"))
    assert(actual.search.hasKey("name"))
    assertEquals(actual.search.keys, List("id", "id", "name", "valid"))
    assertEquals(actual.search.toTuples, List("id" -> "12", "id" -> "14", "name" -> "Joe KD6-3.7", "valid" -> ""))
    assertEquals(actual.search.valueOf("name"), List("Joe KD6-3.7"))
    assertEquals(actual.search.valueOf("id"), List("12", "14"))
    assertEquals(actual.search.valueOf("valid"), Nil)
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
          search = LocationDetails.SearchParams.empty,
          url = "/"
        ),
      "/page-1" ->
        LocationDetails(
          hash = None,
          hostName = None,
          pathName = "/page-1",
          port = None,
          protocol = None,
          search = LocationDetails.SearchParams.empty,
          url = "/page-1"
        ),
      "foo/bar#baz" ->
        LocationDetails(
          hash = Option("#baz"),
          hostName = None,
          pathName = "foo/bar",
          port = None,
          protocol = None,
          search = LocationDetails.SearchParams.empty,
          url = "foo/bar#baz"
        ),
      "/static/images/photo.jpg?width=100&height=50&name=Joe%20KD6-3.7" ->
        LocationDetails(
          hash = None,
          hostName = None,
          pathName = "/static/images/photo.jpg",
          port = None,
          protocol = None,
          search = LocationDetails.SearchParams(
            LocationDetails.QueryParam("width", "100"),
            LocationDetails.QueryParam("height", "50"),
            LocationDetails.QueryParam("name", "Joe KD6-3.7")
          ),
          url = "/static/images/photo.jpg?width=100&height=50&name=Joe%20KD6-3.7"
        ),
      "http://localhost:8080/page2" ->
        LocationDetails(
          hash = None,
          hostName = Option("localhost"),
          pathName = "/page2",
          port = Option("8080"),
          protocol = Option("http:"),
          search = LocationDetails.SearchParams.empty,
          url = "http://localhost:8080/page2"
        ),
      "https://www.example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("www.example.com"),
          pathName = "",
          port = None,
          protocol = Option("https:"),
          search = LocationDetails.SearchParams.empty,
          url = "https://www.example.com"
        ),
      "http://example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "",
          port = None,
          protocol = Option("http:"),
          search = LocationDetails.SearchParams.empty,
          url = "http://example.com"
        ),
      "ftp://ftp.example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("ftp.example.com"),
          pathName = "",
          port = None,
          protocol = Option("ftp:"),
          search = LocationDetails.SearchParams.empty,
          url = "ftp://ftp.example.com"
        ),
      "ssh://example.com:22" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "",
          port = Option("22"),
          protocol = Option("ssh:"),
          search = LocationDetails.SearchParams.empty,
          url = "ssh://example.com:22"
        ),
      "telnet://example.com:23" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "",
          port = Option("23"),
          protocol = Option("telnet:"),
          search = LocationDetails.SearchParams.empty,
          url = "telnet://example.com:23"
        ),
      "mailto:user@example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("user@example.com"),
          pathName = "",
          port = None,
          protocol = Option("mailto:"),
          search = LocationDetails.SearchParams.empty,
          url = "mailto:user@example.com"
        ),
      "https://example.com/path/to/page.html" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "/path/to/page.html",
          port = None,
          protocol = Option("https:"),
          search = LocationDetails.SearchParams.empty,
          url = "https://example.com/path/to/page.html"
        ),
      "http://example.com/path/to/page.php?param1=value1&param2=value2" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "/path/to/page.php",
          port = None,
          protocol = Option("http:"),
          search = LocationDetails.SearchParams(
            LocationDetails.QueryParam("param1", "value1"),
            LocationDetails.QueryParam("param2", "value2")
          ),
          url = "http://example.com/path/to/page.php?param1=value1&param2=value2"
        ),
      "https://example.com#section1" ->
        LocationDetails(
          hash = Option("#section1"),
          hostName = Option("example.com"),
          pathName = "",
          port = None,
          protocol = Option("https:"),
          search = LocationDetails.SearchParams.empty,
          url = "https://example.com#section1"
        ),
      "http://example.com:8080/path/to/page.html" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "/path/to/page.html",
          port = Option("8080"),
          protocol = Option("http:"),
          search = LocationDetails.SearchParams.empty,
          url = "http://example.com:8080/path/to/page.html"
        ),
      "ftp://example.com:21/path/to/file.txt" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "/path/to/file.txt",
          port = Option("21"),
          protocol = Option("ftp:"),
          search = LocationDetails.SearchParams.empty,
          url = "ftp://example.com:21/path/to/file.txt"
        ),
      "http://subdomain.example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("subdomain.example.com"),
          pathName = "",
          port = None,
          protocol = Option("http:"),
          search = LocationDetails.SearchParams.empty,
          url = "http://subdomain.example.com"
        ),
      "https://www.example.co.uk" ->
        LocationDetails(
          hash = None,
          hostName = Option("www.example.co.uk"),
          pathName = "",
          port = None,
          protocol = Option("https:"),
          search = LocationDetails.SearchParams.empty,
          url = "https://www.example.co.uk"
        ),
      "http://example.net/path/to/page.html" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.net"),
          pathName = "/path/to/page.html",
          port = None,
          protocol = Option("http:"),
          search = LocationDetails.SearchParams.empty,
          url = "http://example.net/path/to/page.html"
        ),
      "ftp://ftp.example.net:21/path/to/file.txt" ->
        LocationDetails(
          hash = None,
          hostName = Option("ftp.example.net"),
          pathName = "/path/to/file.txt",
          port = Option("21"),
          protocol = Option("ftp:"),
          search = LocationDetails.SearchParams.empty,
          url = "ftp://ftp.example.net:21/path/to/file.txt"
        ),
      "https://example.org/path/to/page.php?param=value#section1" ->
        LocationDetails(
          hash = Option("#section1"),
          hostName = Option("example.org"),
          pathName = "/path/to/page.php",
          port = None,
          protocol = Option("https:"),
          search = LocationDetails.SearchParams(LocationDetails.QueryParam("param", "value")),
          url = "https://example.org/path/to/page.php?param=value#section1"
        ),
      "http://example.org:8080/path/to/page.html?param=value#section1" ->
        LocationDetails(
          hash = Option("#section1"),
          hostName = Option("example.org"),
          pathName = "/path/to/page.html",
          port = Option("8080"),
          protocol = Option("http:"),
          search = LocationDetails.SearchParams(LocationDetails.QueryParam("param", "value")),
          url = "http://example.org:8080/path/to/page.html?param=value#section1"
        ),
      "ftp://example.org:21/path/to/file.txt?param=value#section1" ->
        LocationDetails(
          hash = Option("#section1"),
          hostName = Option("example.org"),
          pathName = "/path/to/file.txt",
          port = Option("21"),
          protocol = Option("ftp:"),
          search = LocationDetails.SearchParams(LocationDetails.QueryParam("param", "value")),
          url = "ftp://example.org:21/path/to/file.txt?param=value#section1"
        ),
      "http://localhost:8080/path/to/page.php?param=value#section1" ->
        LocationDetails(
          hash = Option("#section1"),
          hostName = Option("localhost"),
          pathName = "/path/to/page.php",
          port = Option("8080"),
          protocol = Option("http:"),
          search = LocationDetails.SearchParams(LocationDetails.QueryParam("param", "value")),
          url = "http://localhost:8080/path/to/page.php?param=value#section1"
        ),
      "https://192.168.1.100:8443/path/to/page.html?param=value#section1" ->
        LocationDetails(
          hash = Option("#section1"),
          hostName = Option("192.168.1.100"),
          pathName = "/path/to/page.html",
          port = Option("8443"),
          protocol = Option("https:"),
          search = LocationDetails.SearchParams(LocationDetails.QueryParam("param", "value")),
          url = "https://192.168.1.100:8443/path/to/page.html?param=value#section1"
        ),
      "http://www.example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("www.example.com"),
          pathName = "",
          port = None,
          protocol = Option("http:"),
          search = LocationDetails.SearchParams.empty,
          url = "http://www.example.com"
        ),
      "https://www.example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("www.example.com"),
          pathName = "",
          port = None,
          protocol = Option("https:"),
          search = LocationDetails.SearchParams.empty,
          url = "https://www.example.com"
        ),
      "ftp://ftp.example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("ftp.example.com"),
          pathName = "",
          port = None,
          protocol = Option("ftp:"),
          search = LocationDetails.SearchParams.empty,
          url = "ftp://ftp.example.com"
        ),
      "sftp://ftp.example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("ftp.example.com"),
          pathName = "",
          port = None,
          protocol = Option("sftp:"),
          search = LocationDetails.SearchParams.empty,
          url = "sftp://ftp.example.com"
        ),
      "ssh://example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "",
          port = None,
          protocol = Option("ssh:"),
          search = LocationDetails.SearchParams.empty,
          url = "ssh://example.com"
        ),
      "telnet://example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "",
          port = None,
          protocol = Option("telnet:"),
          search = LocationDetails.SearchParams.empty,
          url = "telnet://example.com"
        ),
      "mailto:user@example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("user@example.com"),
          pathName = "",
          port = None,
          protocol = Option("mailto:"),
          search = LocationDetails.SearchParams.empty,
          url = "mailto:user@example.com"
        ),
      "news://example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "",
          port = None,
          protocol = Option("news:"),
          search = LocationDetails.SearchParams.empty,
          url = "news://example.com"
        ),
      "gopher://example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "",
          port = None,
          protocol = Option("gopher:"),
          search = LocationDetails.SearchParams.empty,
          url = "gopher://example.com"
        ),
      "ldap://ldap.example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("ldap.example.com"),
          pathName = "",
          port = None,
          protocol = Option("ldap:"),
          search = LocationDetails.SearchParams.empty,
          url = "ldap://ldap.example.com"
        ),
      "smb://example.com/share/file.txt" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "/share/file.txt",
          port = None,
          protocol = Option("smb:"),
          search = LocationDetails.SearchParams.empty,
          url = "smb://example.com/share/file.txt"
        ),
      "nfs://example.com/share/file.txt" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "/share/file.txt",
          port = None,
          protocol = Option("nfs:"),
          search = LocationDetails.SearchParams.empty,
          url = "nfs://example.com/share/file.txt"
        ),
      "file:///path/to/local/file.html" ->
        LocationDetails(
          hash = None,
          hostName = None,
          pathName = "/path/to/local/file.html",
          port = None,
          protocol = Option("file:"),
          search = LocationDetails.SearchParams.empty,
          url = "file:///path/to/local/file.html"
        ),
      "data:text/plain;base64,SGVsbG8sIFdvcmxkIQ==" ->
        LocationDetails(
          hash = None,
          hostName = None,
          pathName = "text/plain;base64,SGVsbG8sIFdvcmxkIQ==",
          port = None,
          protocol = Option("data:"),
          search = LocationDetails.SearchParams.empty,
          url = "data:text/plain;base64,SGVsbG8sIFdvcmxkIQ=="
        ),
      "dns://example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("example.com"),
          pathName = "",
          port = None,
          protocol = Option("dns:"),
          search = LocationDetails.SearchParams.empty,
          url = "dns://example.com"
        ),
      "xmpp:user@example.com" ->
        LocationDetails(
          hash = None,
          hostName = Option("user@example.com"),
          pathName = "",
          port = None,
          protocol = Option("xmpp:"),
          search = LocationDetails.SearchParams.empty,
          url = "xmpp:user@example.com"
        )
    )
