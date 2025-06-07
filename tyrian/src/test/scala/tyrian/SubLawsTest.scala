package tyrian

import cats.effect.IO
import cats.kernel.laws.discipline.EqTests
import cats.kernel.laws.discipline.MonoidTests
import cats.laws.discipline.FunctorTests
import org.scalacheck.Arbitrary
import org.scalacheck.Cogen
import tyrian.runtime.SubHelper

class SubLawsTests extends munit.DisciplineSuite {

  given [A: Arbitrary]: Arbitrary[Sub[IO, A]] =
    Arbitrary(Arbitrary.arbitrary[A].map(Sub.emit))

  given [A]: Cogen[Sub[IO, A]] =
    Cogen[List[String]].contramap(SubHelper.flatten(_).map(_.id))

  checkAll("Eq[Sub]", EqTests[Sub[IO, Int]].eqv)
  checkAll("Functor[Sub]", FunctorTests[Sub[IO, *]].functor[Int, Double, String])
  checkAll("Monoid[Sub]", MonoidTests[Sub[IO, String]].monoid)

}
