package tyrian

import cats.effect.IO
import cats.kernel.laws.discipline.EqTests
import cats.kernel.laws.discipline.MonoidTests
import cats.laws.discipline.FunctorTests
import org.scalacheck.Arbitrary
import org.scalacheck.Cogen
import org.scalacheck.Gen
import org.scalacheck.rng.Seed

class SubLawsTests extends munit.DisciplineSuite {

  given [A: Arbitrary]: Arbitrary[Sub[IO, A]] =
    Arbitrary(Arbitrary.arbitrary[A].map(Sub.emit))

  val cogenValue: Sub[IO, Int] => Long =
    case Sub.None             => 0L
    case Sub.Observe(_, _, _) => 1L
    case Sub.Combine(x, y)    => 2L + cogenValue(x) + cogenValue(y)
    case Sub.Batch(xs)        => xs.foldLeft(3L)((acc, sub) => acc + cogenValue(sub))

  given Cogen[Sub[IO, Int]] = Cogen(cogenValue)

  checkAll("Eq[Sub]", EqTests[Sub[IO, Int]].eqv)
  checkAll("Functor[Sub]", FunctorTests[Sub[IO, *]].functor[Int, Double, String])
  checkAll("Monoid[Sub]", MonoidTests[Sub[IO, String]].monoid)

}
