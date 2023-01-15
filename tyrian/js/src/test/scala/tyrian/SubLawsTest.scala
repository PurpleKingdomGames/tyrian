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

  given Cogen[Sub[IO, Int]] = Cogen {
    case Sub.None             => 0L
    case Sub.Observe(_, _, _) => 1L
    case Sub.Combine(_, _)    => 2L
    case Sub.Batch(_)         => 3L
  }

  checkAll("Eq[Sub]", EqTests[Sub[IO, Int]].eqv)
  checkAll("Functor[Sub]", FunctorTests[Sub[IO, *]].functor[Int, Double, String])

  // FIXME: not passing associativity laws
  checkAll("Monoid[Sub]".ignore, MonoidTests[Sub[IO, String]].monoid)

}
