package tyrian.next

import tyrian.next.syntax.*

import Outcome.*

final case class TestMsg(message: String) extends GlobalMsg

@SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
class OutcomeTests extends munit.FunSuite {

  test("Adding events.adding events after the fact") {
    assertEquals(Outcome(10).unsafeActions, Batch.empty)
    assertEquals(Outcome(10).addActions(Action.Emit(TestMsg("a"))).unsafeActions.head, Action.Emit(TestMsg("a")))
  }

  test("Adding events.creating events based on new state") {
    val actual = Outcome(10)
      .addActions(Action.Emit(TestMsg("a")))
      .createActions(i => Batch(Action.Emit(TestMsg(s"count: $i"))))
      .unsafeActions

    val expected = Batch(Action.Emit(TestMsg("a")), Action.Emit(TestMsg("count: 10")))

    assertEquals(actual, expected)
  }

  test("Extractor should allow pattern match") {
    val a = Outcome(1).addActions(Action.Emit(TestMsg("a")))

    a match {
      case Outcome(n, Batch(Action.Emit(TestMsg(s)))) =>
        assertEquals(n, 1)
        assertEquals(s, "a")

      case x =>
        fail("shouldn't have got here.")
    }
  }

  test("Transforming outcomes.sequencing (list)") {
    val l: List[Outcome[Int]] =
      List(
        Outcome(1).addActions(Action.Emit(TestMsg("a"))),
        Outcome(2).addActions(Action.Emit(TestMsg("b"))),
        Outcome(3).addActions(Action.Emit(TestMsg("c")))
      )

    val actual: Outcome[List[Int]] =
      l.sequence

    val expected: Outcome[List[Int]] =
      Outcome(List(1, 2, 3))
        .addActions(Action.Emit(TestMsg("a")), Action.Emit(TestMsg("b")), Action.Emit(TestMsg("c")))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeActions, expected.unsafeActions)
  }

  test("Transforming outcomes.sequencing (batch)") {
    val l: List[Outcome[Int]] =
      List(
        Outcome(1).addActions(Action.Emit(TestMsg("a"))),
        Outcome(2).addActions(Action.Emit(TestMsg("b"))),
        Outcome(3).addActions(Action.Emit(TestMsg("c")))
      )

    val actual: Outcome[List[Int]] =
      l.sequence

    val expected: Outcome[List[Int]] =
      Outcome(List(1, 2, 3))
        .addActions(Action.Emit(TestMsg("a")), Action.Emit(TestMsg("b")), Action.Emit(TestMsg("c")))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeActions, expected.unsafeActions)
  }

  test("Mapping over Outcomes.map state") {
    assertEquals(Outcome(10).map(_ + 10).unsafeGet, Outcome(20).unsafeGet)

    assertEquals(
      Outcome(10).addActions(Action.Emit(TestMsg("a"))).map(_ + 10).unsafeGet,
      Outcome(20).addActions(Action.Emit(TestMsg("a"))).unsafeGet
    )
    assertEquals(
      Outcome(10).addActions(Action.Emit(TestMsg("a"))).map(_ + 10).unsafeActions,
      Outcome(20).addActions(Action.Emit(TestMsg("a"))).unsafeActions
    )
  }

  test("Replace global event list") {
    val actual =
      Outcome(10)
        .addActions(Action.Emit(TestMsg("a")), Action.Emit(TestMsg("b")), Action.Emit(TestMsg("c")))
        .replaceActions(_.filter {
          case Action.Emit(TestMsg(msg)) =>
            msg == "b"

          case _ =>
            fail("Expected Action.Emit, but got something else.")
        })

    val expected =
      Outcome(10)
        .addActions(Action.Emit(TestMsg("b")))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeActions, expected.unsafeActions)
  }

  test("clear global event list") {
    val actual =
      Outcome(10)
        .addActions(Action.Emit(TestMsg("a")), Action.Emit(TestMsg("b")), Action.Emit(TestMsg("c")))
        .clearActions

    val expected =
      Outcome(10, Batch.empty)

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeActions, expected.unsafeActions)
  }

  test("Mapping over Outcomes.map global events") {
    val actual =
      Outcome(10)
        .addActions(Action.Emit(TestMsg("a")), Action.Emit(TestMsg("b")), Action.Emit(TestMsg("c")))
        .mapActions {
          case Action.Emit(TestMsg(msg)) =>
            Action.Emit(TestMsg(msg + msg))

          case _ =>
            fail("Expected Action.Emit, but got something else.")
        }

    val expected =
      Outcome(10)
        .addActions(Action.Emit(TestMsg("aa")), Action.Emit(TestMsg("bb")), Action.Emit(TestMsg("cc")))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeActions, expected.unsafeActions)
  }

  test("Mapping over Outcomes.map all") {
    val actual =
      Outcome(10)
        .addActions(Action.Emit(TestMsg("a")), Action.Emit(TestMsg("b")), Action.Emit(TestMsg("c")))
        .mapAll(
          _ + 20,
          _.filter {
            case Action.Emit(TestMsg(msg)) =>
              msg == "b"

            case _ =>
              fail("Expected Action.Emit, but got something else.")
          }
        )

    val expected =
      Outcome(30)
        .addActions(Action.Emit(TestMsg("b")))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeActions, expected.unsafeActions)
  }

  test("flat map & join.join preserves event order") {
    val oa =
      Outcome(
        Outcome(
          Outcome(10).addActions(Action.Emit(TestMsg("z")))
        ).addActions(Action.Emit(TestMsg("x")), Action.Emit(TestMsg("y")))
      ).addActions(Action.Emit(TestMsg("w")))

    val expected =
      Outcome(10)
        .addActions(
          Action.Emit(TestMsg("w")),
          Action.Emit(TestMsg("x")),
          Action.Emit(TestMsg("y")),
          Action.Emit(TestMsg("z"))
        )

    val actual = Outcome.join(Outcome.join(oa))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeActions, expected.unsafeActions)
  }

  test("flat map & join.flatMap") {
    assertEquals(Outcome(10).flatMap(i => Outcome(i * 10)).unsafeGet, Outcome(100).unsafeGet)
    assertEquals(Outcome(10).flatMap(i => Outcome(i * 10)).unsafeActions, Outcome(100).unsafeActions)

    assertEquals(Outcome.join(Outcome(10).map(i => Outcome(i * 10))).unsafeGet, Outcome(100).unsafeGet)
    assertEquals(
      Outcome.join(Outcome(10).map(i => Outcome(i * 10))).unsafeActions,
      Outcome(100).unsafeActions
    )
  }

  test("Applicative.ap") {

    val actual: Outcome[Int] =
      Outcome(10).ap(Outcome((i: Int) => i + 10))

    val expected: Outcome[Int] =
      Outcome(20)

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeActions, expected.unsafeActions)
  }

  test("Applicative.ap with event") {

    val actual: Outcome[Int] =
      Outcome(10).addActions(Action.Emit(TestMsg("x"))).ap(Outcome((i: Int) => i + 10))

    val expected: Outcome[Int] =
      Outcome(20).addActions(Action.Emit(TestMsg("x")))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeActions, expected.unsafeActions)
  }

  test("Combine - 2 Outcomes can be combined") {

    val oa = Outcome("count").addActions(Action.Emit(TestMsg("x")))
    val ob = Outcome(1).addActions(Action.Emit(TestMsg("y")), Action.Emit(TestMsg("z")))

    val actual1 = oa.combine(ob)
    val actual2 = Outcome.combine(oa, ob)
    val actual3 = (oa, ob).combine

    val expected =
      Outcome(("count", 1)).addActions(Action.Emit(TestMsg("x")), Action.Emit(TestMsg("y")), Action.Emit(TestMsg("z")))

    assertEquals(actual1.unsafeGet, expected.unsafeGet)
    assertEquals(actual1.unsafeActions, expected.unsafeActions)
    assertEquals(actual2.unsafeGet, expected.unsafeGet)
    assertEquals(actual2.unsafeActions, expected.unsafeActions)
    assertEquals(actual3.unsafeGet, expected.unsafeGet)
    assertEquals(actual3.unsafeActions, expected.unsafeActions)
  }

  test("Combine - 3 Outcomes can be combined") {

    val oa = Outcome("count").addActions(Action.Emit(TestMsg("x")))
    val ob = Outcome(1).addActions(Action.Emit(TestMsg("y")), Action.Emit(TestMsg("z")))
    val oc = Outcome(true).addActions(Action.Emit(TestMsg("w")))

    val actual1 = Outcome.combine3(oa, ob, oc)
    val actual2 = (oa, ob, oc).combine

    val expected =
      Outcome(("count", 1, true)).addActions(
        Action.Emit(TestMsg("x")),
        Action.Emit(TestMsg("y")),
        Action.Emit(TestMsg("z")),
        Action.Emit(TestMsg("w"))
      )

    assertEquals(actual1.unsafeGet, expected.unsafeGet)
    assertEquals(actual1.unsafeActions, expected.unsafeActions)
    assertEquals(actual2.unsafeGet, expected.unsafeGet)
    assertEquals(actual2.unsafeActions, expected.unsafeActions)
  }

  test("Applicative.map2 / merge") {
    val oa = Outcome[String]("count").addActions(Action.Emit(TestMsg("x")))
    val ob = Outcome[Int](1).addActions(Action.Emit(TestMsg("y")), Action.Emit(TestMsg("z")))

    val actual1: Outcome[String] =
      Outcome.merge(oa, ob)((a: String, b: Int) => a + ": " + b)
    val actual2: Outcome[String] =
      oa.merge(ob)((a: String, b: Int) => a + ": " + b)
    val actual3: Outcome[String] =
      (oa, ob).merge((a: String, b: Int) => a + ": " + b)

    val expected: Outcome[String] =
      Outcome("count: 1").addActions(Action.Emit(TestMsg("x")), Action.Emit(TestMsg("y")), Action.Emit(TestMsg("z")))

    assertEquals(actual1.unsafeGet, expected.unsafeGet)
    assertEquals(actual1.unsafeActions, expected.unsafeActions)
    assertEquals(actual2.unsafeGet, expected.unsafeGet)
    assertEquals(actual2.unsafeActions, expected.unsafeActions)
    assertEquals(actual3.unsafeGet, expected.unsafeGet)
    assertEquals(actual3.unsafeActions, expected.unsafeActions)
  }

  test("Applicative.map3 / merge") {
    val oa = Outcome[String]("count").addActions(Action.Emit(TestMsg("x")))
    val ob = Outcome[Int](1).addActions(Action.Emit(TestMsg("y")), Action.Emit(TestMsg("z")))
    val oc = Outcome[Boolean](true).addActions(Action.Emit(TestMsg("w")))

    val actual1: Outcome[String] =
      Outcome.merge3(oa, ob, oc)((a: String, b: Int, c: Boolean) => a + ": " + b + ": " + c)
    val actual2: Outcome[String] =
      (oa, ob, oc).merge((a: String, b: Int, c: Boolean) => a + ": " + b + ": " + c)

    val expected: Outcome[String] =
      Outcome("count: 1: true").addActions(
        Action.Emit(TestMsg("x")),
        Action.Emit(TestMsg("y")),
        Action.Emit(TestMsg("z")),
        Action.Emit(TestMsg("w"))
      )

    assertEquals(actual1.unsafeGet, expected.unsafeGet)
    assertEquals(actual1.unsafeActions, expected.unsafeActions)
    assertEquals(actual2.unsafeGet, expected.unsafeGet)
    assertEquals(actual2.unsafeActions, expected.unsafeActions)
  }

  // Error handline

  def errorsMatch[A](actual: Outcome[A], expected: Outcome[A]): Boolean =
    (actual, expected) match {
      case (Outcome.Error(e1, _), Outcome.Error(e2, _)) =>
        e1.getMessage == e2.getMessage

      case _ =>
        false
    }

  test("Exceptions thrown during creation are handled") {
    val e = new Exception("Boom!")

    val actual =
      Outcome[Int](throw e)

    val expected =
      Outcome.Error(e)

    assert(errorsMatch(actual, expected))
  }

  test("mapping an error") {
    val e = new Exception("Boom!")
    val actual =
      Outcome[Int](10).map[Int](_ => throw e).map(i => i * i)

    val expected =
      Outcome.Error(e)

    assert(errorsMatch(actual, expected))
  }

  test("flatMapping an error") {
    def foo(): Int =
      throw new Exception("amount: 10")

    val actual =
      for {
        a <- Outcome[Int](10)
        b <- Outcome[Int](foo())
        c <- Outcome[Int](30)
      } yield a + b + c

    val expected =
      Outcome.Error(new Exception("amount: 10"))

    assertEquals(actual.isError, expected.isError)
    assert(errorsMatch(actual, expected))
  }

  test("raising an error") {
    val e = new Exception("Boom!")

    def foo(o: Outcome[Int]): Outcome[Int] =
      o.flatMap { i =>
        if i % 2 == 0 then Outcome(i * 10)
        else Outcome.raiseError(e)
      }

    assertEquals(foo(Outcome(4)), Outcome(40))
    assert(errorsMatch(foo(Outcome(5)), Outcome(throw e)))
  }

  test("recovering from an error") {
    val e = new Exception("Boom!")
    val actual =
      Outcome(10)
        .map[Int](_ => throw e)
        .map(i => i * i)
        .handleError { case e =>
          Outcome(e.getMessage.length)
        }

    val expected =
      Outcome(5)

    assertEquals(actual, expected)
  }

  test("recovering from an error with orElse") {
    val e = new Exception("Boom!")
    val actual =
      Outcome(10)
        .map[Int](_ => throw e)
        .map(i => i * i)
        .orElse(Outcome(e.getMessage.length))

    val expected =
      Outcome(5)

    assertEquals(actual, expected)
  }

  test("logging a crash") {
    val e = new Exception("Boom!")

    val actual =
      try
        Outcome(10)
          .map[Int](_ => throw e)
          .map(i => i * i)
          .logCrash { case e => e.getMessage }
      catch {
        case _: Throwable =>
          ()
      }

    val expected =
      "Boom!"

    actual match {
      case Error(e, r) =>
        assertEquals(r(e), expected)

      case _ =>
        fail("Failed...")
    }

  }

  test("Convert Option[A] to Outcome[A]") {

    val e = new Exception("Boom!")

    val actual =
      Option(123).toOutcome(e)

    val expected =
      Outcome[Int](123)

    assertEquals(actual, expected)
  }

  test("Convert Option[A] to Outcome[A] (error case)") {

    val e = new Exception("Boom!")

    val actual =
      Option.empty[Int].toOutcome(e)

    val expected =
      Outcome.Error(e)

    assert(errorsMatch(actual, expected))
  }

}
