package tyrian

import cats.effect.Async

class CE3Tests extends munit.FunSuite {

  test("can I make a callback work...?") {

    // 

  }

}

final case class Cmd2[F[_]](run: Async[F])
