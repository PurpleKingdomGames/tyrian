// package tyrian

// import cats.effect.IO

// import Outcome.*

// @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
// class OutcomeTests extends munit.FunSuite {

//   test("Adding events.adding events after the fact") {
//     assertEquals(Outcome(10).unsafeGlobalEvents, List.empty)
//     assertEquals(Outcome(10).addGlobalEvents(TestMsg("a")).unsafeGlobalEvents, List(TestMsg("a")))
//   }

//   test("Adding events.creating events based on new state") {
//     val actual = Outcome(10)
//       .addGlobalEvents(TestMsg("a"))
//       .createGlobalEvents(i => List(TestMsg(s"count: $i")))
//       .unsafeGlobalEvents

//     val expected = List(TestMsg("a"), TestMsg("count: 10"))

//     assertEquals(actual, expected)
//   }

//   test("Extractor should allow pattern match") {
//     val a = Outcome(1).addGlobalEvents(TestMsg("a"))

//     a match {
//       case Outcome(n, List(TestMsg(s))) =>
//         assertEquals(n, 1)
//         assertEquals(s, "a")

//       case _ =>
//         fail("shouldn't have got here.")
//     }
//   }

//   test("Transforming outcomes.sequencing (list)") {
//     val l: List[Outcome[IO, Int, Msg]] =
//       List(
//         Outcome(1).addGlobalEvents(TestMsg("a")),
//         Outcome(2).addGlobalEvents(TestMsg("b")),
//         Outcome(3).addGlobalEvents(TestMsg("c"))
//       )

//     val actual: Outcome[IO, List[Int], Msg] =
//       l.sequence

//     val expected: Outcome[IO, List[Int], Msg] =
//       Outcome(List(1, 2, 3))
//         .addGlobalEvents(TestMsg("a"), TestMsg("b"), TestMsg("c"))

//     assertEquals(actual.unsafeGet, expected.unsafeGet)
//     assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
//   }

//   test("Transforming outcomes.sequencing (batch)") {
//     val l: List[Outcome[IO, Int, Msg]] =
//       List(
//         Outcome(1).addGlobalEvents(TestMsg("a")),
//         Outcome(2).addGlobalEvents(TestMsg("b")),
//         Outcome(3).addGlobalEvents(TestMsg("c"))
//       )

//     val actual: Outcome[IO, List[Int], Msg] =
//       l.sequence

//     val expected: Outcome[IO, List[Int], Msg] =
//       Outcome(List(1, 2, 3))
//         .addGlobalEvents(TestMsg("a"), TestMsg("b"), TestMsg("c"))

//     assertEquals(actual.unsafeGet, expected.unsafeGet)
//     assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
//   }

//   test("Mapping over Outcomes.map state") {
//     assertEquals(Outcome(10).map(_ + 10).unsafeGet, Outcome(20).unsafeGet)

//     assertEquals(
//       Outcome(10).addGlobalEvents(TestMsg("a")).map(_ + 10).unsafeGet,
//       Outcome(20).addGlobalEvents(TestMsg("a")).unsafeGet
//     )
//     assertEquals(
//       Outcome(10).addGlobalEvents(TestMsg("a")).map(_ + 10).unsafeGlobalEvents,
//       Outcome(20).addGlobalEvents(TestMsg("a")).unsafeGlobalEvents
//     )
//   }

//   test("Replace global event list") {
//     val actual =
//       Outcome(10)
//         .addGlobalEvents(TestMsg("a"), TestMsg("b"), TestMsg("c"))
//         .replaceGlobalEvents(_.filter {
//           case TestMsg(msg) =>
//             msg == "b"
//         })

//     val expected =
//       Outcome(10)
//         .addGlobalEvents(TestMsg("b"))

//     assertEquals(actual.unsafeGet, expected.unsafeGet)
//     assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
//   }

//   test("clear global event list") {
//     val actual =
//       Outcome(10)
//         .addGlobalEvents(TestMsg("a"), TestMsg("b"), TestMsg("c"))
//         .clearGlobalEvents

//     val expected =
//       Outcome(10, List.empty)

//     assertEquals(actual.unsafeGet, expected.unsafeGet)
//     assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
//   }

//   test("Mapping over Outcomes.map global events") {
//     val actual =
//       Outcome(10)
//         .addGlobalEvents(TestMsg("a"), TestMsg("b"), TestMsg("c"))
//         .mapGlobalEvents {
//           case TestMsg(msg) =>
//             TestMsg(msg + msg)
//         }

//     val expected =
//       Outcome(10)
//         .addGlobalEvents(TestMsg("aa"), TestMsg("bb"), TestMsg("cc"))

//     assertEquals(actual.unsafeGet, expected.unsafeGet)
//     assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
//   }

//   test("Mapping over Outcomes.map all") {
//     val actual =
//       Outcome(10)
//         .addGlobalEvents(TestMsg("a"), TestMsg("b"), TestMsg("c"))
//         .mapAll(
//           _ + 20,
//           _.filter {
//             case TestMsg(msg) =>
//               msg == "b"
//           }
//         )

//     val expected =
//       Outcome(30)
//         .addGlobalEvents(TestMsg("b"))

//     assertEquals(actual.unsafeGet, expected.unsafeGet)
//     assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
//   }

//   test("flat map & join.join preserves event order") {
//     val oa =
//       Outcome(
//         Outcome(
//           Outcome(10).addGlobalEvents(TestMsg("z"))
//         ).addGlobalEvents(TestMsg("x"), TestMsg("y"))
//       ).addGlobalEvents(TestMsg("w"))

//     val expected =
//       Outcome(10)
//         .addGlobalEvents(TestMsg("w"), TestMsg("x"), TestMsg("y"), TestMsg("z"))

//     val actual = Outcome.join(Outcome.join(oa))

//     assertEquals(actual.unsafeGet, expected.unsafeGet)
//     assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
//   }

//   test("flat map & join.flatMap") {
//     assertEquals(Outcome(10).flatMap(i => Outcome(i * 10)).unsafeGet, Outcome(100).unsafeGet)
//     assertEquals(Outcome(10).flatMap(i => Outcome(i * 10)).unsafeGlobalEvents, Outcome(100).unsafeGlobalEvents)

//     assertEquals(Outcome.join(Outcome(10).map(i => Outcome(i * 10))).unsafeGet, Outcome(100).unsafeGet)
//     assertEquals(
//       Outcome.join(Outcome(10).map(i => Outcome(i * 10))).unsafeGlobalEvents,
//       Outcome(100).unsafeGlobalEvents
//     )
//   }

//   test("Applicative.ap") {

//     val actual: Outcome[IO, Int, Msg] =
//       Outcome(10).ap(Outcome((i: Int) => i + 10))

//     val expected: Outcome[IO, Int, Msg] =
//       Outcome(20)

//     assertEquals(actual.unsafeGet, expected.unsafeGet)
//     assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
//   }

//   test("Applicative.ap with event") {

//     val actual: Outcome[IO, Int, Msg] =
//       Outcome(10).addGlobalEvents(TestMsg("x")).ap(Outcome((i: Int) => i + 10))

//     val expected: Outcome[IO, Int, Msg] =
//       Outcome(20).addGlobalEvents(TestMsg("x"))

//     assertEquals(actual.unsafeGet, expected.unsafeGet)
//     assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
//   }

//   test("Combine - 2 Outcomes can be combined") {

//     val oa = Outcome("count").addGlobalEvents(TestMsg("x"))
//     val ob = Outcome(1).addGlobalEvents(TestMsg("y"), TestMsg("z"))

//     val actual1 = oa.combine(ob)
//     val actual2 = Outcome.combine(oa, ob)
//     val actual3 = (oa, ob).combine

//     val expected =
//       Outcome(("count", 1)).addGlobalEvents(TestMsg("x"), TestMsg("y"), TestMsg("z"))

//     assertEquals(actual1.unsafeGet, expected.unsafeGet)
//     assertEquals(actual1.unsafeGlobalEvents, expected.unsafeGlobalEvents)
//     assertEquals(actual2.unsafeGet, expected.unsafeGet)
//     assertEquals(actual2.unsafeGlobalEvents, expected.unsafeGlobalEvents)
//     assertEquals(actual3.unsafeGet, expected.unsafeGet)
//     assertEquals(actual3.unsafeGlobalEvents, expected.unsafeGlobalEvents)
//   }

//   test("Combine - 3 Outcomes can be combined") {

//     val oa = Outcome("count").addGlobalEvents(TestMsg("x"))
//     val ob = Outcome(1).addGlobalEvents(TestMsg("y"), TestMsg("z"))
//     val oc = Outcome(true).addGlobalEvents(TestMsg("w"))

//     val actual1 = Outcome.combine3(oa, ob, oc)
//     val actual2 = (oa, ob, oc).combine

//     val expected =
//       Outcome(("count", 1, true)).addGlobalEvents(TestMsg("x"), TestMsg("y"), TestMsg("z"), TestMsg("w"))

//     assertEquals(actual1.unsafeGet, expected.unsafeGet)
//     assertEquals(actual1.unsafeGlobalEvents, expected.unsafeGlobalEvents)
//     assertEquals(actual2.unsafeGet, expected.unsafeGet)
//     assertEquals(actual2.unsafeGlobalEvents, expected.unsafeGlobalEvents)
//   }

//   test("Applicative.map2 / merge") {
//     val oa = Outcome[IO, String, Msg]("count").addGlobalEvents(TestMsg("x"))
//     val ob = Outcome[IO, Int, Msg](1).addGlobalEvents(TestMsg("y"), TestMsg("z"))

//     val actual1: Outcome[IO, String, Msg] =
//       Outcome.merge(oa, ob)((a: String, b: Int) => a + ": " + b)
//     val actual2: Outcome[IO, String, Msg] =
//       oa.merge(ob)((a: String, b: Int) => a + ": " + b)
//     val actual3: Outcome[IO, String, Msg] =
//       (oa, ob).merge((a: String, b: Int) => a + ": " + b)

//     val expected: Outcome[IO, String, Msg] =
//       Outcome("count: 1").addGlobalEvents(TestMsg("x"), TestMsg("y"), TestMsg("z"))

//     assertEquals(actual1.unsafeGet, expected.unsafeGet)
//     assertEquals(actual1.unsafeGlobalEvents, expected.unsafeGlobalEvents)
//     assertEquals(actual2.unsafeGet, expected.unsafeGet)
//     assertEquals(actual2.unsafeGlobalEvents, expected.unsafeGlobalEvents)
//     assertEquals(actual3.unsafeGet, expected.unsafeGet)
//     assertEquals(actual3.unsafeGlobalEvents, expected.unsafeGlobalEvents)
//   }

//   test("Applicative.map3 / merge") {
//     val oa = Outcome[IO, String, Msg]("count").addGlobalEvents(TestMsg("x"))
//     val ob = Outcome[IO, Int, Msg](1).addGlobalEvents(TestMsg("y"), TestMsg("z"))
//     val oc = Outcome[IO, Boolean, Msg](true).addGlobalEvents(TestMsg("w"))

//     val actual1: Outcome[IO, String, Msg] =
//       Outcome.merge3(oa, ob, oc)((a: String, b: Int, c: Boolean) => a + ": " + b + ": " + c)
//     val actual2: Outcome[IO, String, Msg] =
//       (oa, ob, oc).merge((a: String, b: Int, c: Boolean) => a + ": " + b + ": " + c)

//     val expected: Outcome[IO, String, Msg] =
//       Outcome("count: 1: true").addGlobalEvents(TestMsg("x"), TestMsg("y"), TestMsg("z"), TestMsg("w"))

//     assertEquals(actual1.unsafeGet, expected.unsafeGet)
//     assertEquals(actual1.unsafeGlobalEvents, expected.unsafeGlobalEvents)
//     assertEquals(actual2.unsafeGet, expected.unsafeGet)
//     assertEquals(actual2.unsafeGlobalEvents, expected.unsafeGlobalEvents)
//   }

//   // Error handline

//   def errorsMatch[A](actual: Outcome[IO, A, Msg], expected: Outcome[IO, A, Msg]): Boolean =
//     (actual, expected) match {
//       case (Outcome.Error(e1, _), Outcome.Error(e2, _)) =>
//         e1.getMessage == e2.getMessage

//       case _ =>
//         false
//     }

//   test("Exceptions thrown during creation are handled") {
//     val e = new Exception("Boom!")

//     val actual =
//       Outcome[IO, Int, Msg](throw e)

//     val expected =
//       Outcome.Error[IO, Msg](e)

//     assert(errorsMatch(actual, expected))
//   }

//   test("mapping an error") {
//     val e = new Exception("Boom!")
//     val actual =
//       Outcome[IO, Int, Msg](10).map[Int](_ => throw e).map(i => i * i)

//     val expected =
//       Outcome.Error[IO, Msg](e)

//     assert(errorsMatch(actual, expected))
//   }

//   test("flatMapping an error") {
//     def foo(): Int =
//       throw new Exception("amount: 10")

//     val actual =
//       for {
//         a <- Outcome[IO, Int, Msg](10)
//         b <- Outcome[IO, Int, Msg](foo())
//         c <- Outcome[IO, Int, Msg](30)
//       } yield a + b + c

//     val expected =
//       Outcome.Error[IO, Msg](new Exception("amount: 10"))

//     assertEquals(actual.isError, expected.isError)
//     assert(errorsMatch(actual, expected))
//   }

//   test("raising an error") {
//     val e = new Exception("Boom!")

//     def foo(o: Outcome[IO, Int, Msg]): Outcome[IO, Int, Msg] =
//       o.flatMap { i =>
//         if (i % 2 == 0) Outcome(i * 10)
//         else Outcome.raiseError(e)
//       }

//     assertEquals(foo(Outcome(4)), Outcome(40))
//     assert(errorsMatch(foo(Outcome(5)), Outcome(throw e)))
//   }

//   test("recovering from an error") {
//     val e = new Exception("Boom!")
//     val actual =
//       Outcome(10)
//         .map[Int](_ => throw e)
//         .map(i => i * i)
//         .handleError { case e =>
//           Outcome(e.getMessage.length)
//         }

//     val expected =
//       Outcome(5)

//     assertEquals(actual, expected)
//   }

//   test("recovering from an error with orElse") {
//     val e = new Exception("Boom!")
//     val actual =
//       Outcome(10)
//         .map[Int](_ => throw e)
//         .map(i => i * i)
//         .orElse(Outcome(e.getMessage.length))

//     val expected =
//       Outcome(5)

//     assertEquals(actual, expected)
//   }

//   test("logging a crash") {
//     val e = new Exception("Boom!")

//     val actual =
//       try
//         Outcome(10)
//           .map[Int](_ => throw e)
//           .map(i => i * i)
//           .logCrash { case e => e.getMessage }
//       catch {
//         case _: Throwable =>
//           ()
//       }

//     val expected =
//       "Boom!"

//     actual match {
//       case Error(e, r) =>
//         assertEquals(r(e), expected)

//       case _ =>
//         fail("Failed...")
//     }

//   }

//   test("Convert Option[A] to Outcome[IO, A, Msg]") {
//     import tyrian.syntax.*

//     val e = new Exception("Boom!")

//     val actual =
//       Option(123).toOutcome[IO, Msg](e)

//     val expected =
//       Outcome[IO, Int, Msg](123)

//     assertEquals(actual, expected)
//   }

//   test("Convert Option[A] to Outcome[IO, A, Msg] (error case)") {
//     import tyrian.syntax.*

//     val e = new Exception("Boom!")

//     val actual =
//       Option.empty[Int].toOutcome[IO, Msg](e)

//     val expected =
//       Outcome.Error[IO, Msg](e)

//     assert(errorsMatch(actual, expected))
//   }

// }

// trait Msg
// final case class TestMsg(message: String) extends Msg
