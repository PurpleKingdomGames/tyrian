package tyrian.next

import cats.effect.IO
import cats.syntax.all.*

import scala.concurrent.duration.*

@SuppressWarnings(Array("scalafix:DisableSyntax.var"))
class ActionTests extends munit.CatsEffectSuite {

  final case class IntMsg(i: Int)       extends GlobalMsg
  final case class StringMsg(s: String) extends GlobalMsg

  import ActionWatchUtils.*

  test("map - Empty") {
    assertEquals(Action.None.map(_ => IntMsg(10)), Action.None)
  }

  test("map - Run") {
    val mapped: Action =
      Action
        .Run(IO(10), (res: Int) => StringMsg(res.toString))
        .map {
          case StringMsg(s) =>
            StringMsg(s"count: ${s}")

          case _ =>
            fail("Unexpected type")
        }

    val actual: IO[String] =
      mapped.run.map {
        case StringMsg(s) => s
        case _            => fail("Unexpected type")
      }

    val expected: String =
      "count: 10"

    actual.assertEquals(expected)
  }

  test("map - Many") {
    val many =
      Action.Many(
        Action.Run(IO(10), i => IntMsg(i * 2)),
        Action.Run(IO(10), i => IntMsg(i * 100)),
        Action.Run(IO(10), i => IntMsg(i * 10))
      )

    val actual: IO[List[Int]] =
      many.actions.toList.traverse(
        _.run.map {
          case IntMsg(i) => i
          case _         => fail("unexpected type")
        }
      )

    val expected: List[Int] =
      List(20, 1000, 100)

    actual.assertEquals(expected)
  }

  test("Many cons") {
    val actual =
      Action.Emit(IntMsg(10)) :: Action.Many(Action.Emit(IntMsg(20)))

    val expected =
      Action.Many(Action.Emit(IntMsg(10)), Action.Emit(IntMsg(20)))

    assertEquals(actual, expected)
  }

  test("Many prepend") {
    val actual =
      Action.Emit(IntMsg(10)) +: Action.Many(Action.Emit(IntMsg(20)))

    val expected =
      Action.Many(Action.Emit(IntMsg(10)), Action.Emit(IntMsg(20)))

    assertEquals(actual, expected)
  }

  test("Many append") {
    val actual =
      Action.Many(Action.Emit(IntMsg(10))) :+ Action.Emit(IntMsg(20))

    val expected =
      Action.Many(Action.Emit(IntMsg(10)), Action.Emit(IntMsg(20)))

    assertEquals(actual, expected)
  }

  test("Many concat") {
    val actual =
      Action.Many(Action.Emit(IntMsg(10)), Action.Emit(IntMsg(20))) ++ Action.Many(
        Action.Emit(IntMsg(30)),
        Action.Emit(IntMsg(40))
      )

    val expected =
      Action.Many(Action.Emit(IntMsg(10)), Action.Emit(IntMsg(20)), Action.Emit(IntMsg(30)), Action.Emit(IntMsg(40)))

    assertEquals(actual, expected)
  }

  test("emitAfterDelay") {
    val action: Action =
      Action.emitAfterDelay(IntMsg(10), 1.seconds)

    val actual: IO[IntMsg] =
      action.run.map {
        case e: IntMsg => e
        case _         => fail("wrong type")
      }

    actual.assertEquals(IntMsg(10))
  }

  test("Action.Emit toTask") {
    val actual: IO[IntMsg] =
      Action.Emit(IntMsg(10)).toTask.map {
        case e: IntMsg => e
        case _         => fail("wrong type")
      }

    actual.assertEquals(IntMsg(10))
  }

  test("Action.SideEffect toTask") {
    var actual = 0

    val t = IO {
      actual = actual + 1
    }

    Action.SideEffect(t).toTask.map(_ => actual == 1).assert
  }

  test("Action.SideEffect toTask with non-unit return type") {
    var actual = 0

    val t: IO[Boolean] = IO {
      actual = actual + 1
      true
    }

    Action.SideEffect(t).toTask.map(_ => actual == 1).assert
  }

  test("Action.Run toTask") {
    Action.Run(IO(10), i => StringMsg(i.toString)).toTask.assertEquals(StringMsg("10"))
  }

  test("Action -> toTask -> Action") {
    var acc = 0

    val t: IO[String] =
      for {
        i <- Action.Emit(IntMsg(10)).toTask.map {
          case e: IntMsg => e
          case _         => fail("wrong type")
        }
        _ <- IO { acc = acc + 1 }
        s <- IO((i.i + acc).toString)
      } yield s

    val actual =
      Action.Run(t, s => StringMsg("count: " + s))

    actual.run.assertEquals(StringMsg("count: 11"))
  }

  test("Action.Run produces message directly") {
    Action.Run(IO(StringMsg("testing"))).toTask.assertEquals(StringMsg("testing"))
  }

}
