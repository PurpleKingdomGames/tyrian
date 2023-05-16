package tyrian

class LocationTests extends munit.FunSuite:

  test("fromUrl: a path only location is Internal") {

    val actual =
      Location.fromUrl(
        "/page4",
        Location.Internal(
          LocationDetails(
            hash = None,
            hostName = None,
            pathName = "/",
            port = None,
            protocol = None,
            search = None,
            url = "/"
          )
        )
      )

    val expected =
      Location.Internal(
        LocationDetails(
          hash = None,
          hostName = None,
          pathName = "/page4",
          port = None,
          protocol = None,
          search = None,
          url = "/page4"
        )
      )

    assertEquals(actual, expected)

  }

  test("fromUrl: a path with an internal origin is Internal") {

    val actual =
      Location.fromUrl(
        "https://localhost:8080/page4",
        Location.Internal(LocationDetails.fromUrl("https://localhost:8080/"))
      )

    val expected =
      Location.Internal(
        LocationDetails(
          hash = None,
          hostName = Some("localhost"),
          pathName = "/page4",
          port = Some("8080"),
          protocol = Some("https:"),
          search = None,
          url = "https://localhost:8080/page4"
        )
      )

    assertEquals(actual, expected)

  }

  test("fromUrl: a path with an external origin is External") {

    val actual =
      Location.fromUrl(
        "https://indigoengine.io/docs",
        Location.Internal(LocationDetails.fromUrl("https://localhost:8080/"))
      )

    val expected =
      Location.External(
        LocationDetails(
          hash = None,
          hostName = Some("indigoengine.io"),
          pathName = "/docs",
          port = None,
          protocol = Some("https:"),
          search = None,
          url = "https://indigoengine.io/docs"
        )
      )

    assertEquals(actual, expected)

  }
