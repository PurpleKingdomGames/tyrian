package tyrian

import cats.Id
import cats.kernel.laws.discipline.EqTests
import cats.kernel.laws.discipline.MonoidTests
import cats.laws.discipline.FunctorTests
import cats.syntax.foldable.*
import org.scalacheck.Arbitrary
import org.scalacheck.Cogen
import org.scalacheck.Gen
import org.scalacheck.rng.Seed
import tyrian.runtime.CmdHelper

class CmdLawsTests extends munit.DisciplineSuite {

  given [A: Arbitrary]: Arbitrary[Cmd[Id, A]] =
    Arbitrary(Arbitrary.arbitrary[A].map(Cmd.emit))

  given [A: Cogen]: Cogen[Cmd[Id, A]] =
    Cogen[List[Id[Option[A]]]].contramap(CmdHelper.cmdToTaskList)

  checkAll("Eq[Cmd]", EqTests[Cmd[Id, Int]].eqv)
  checkAll("Functor[Cmd]", FunctorTests[Cmd[Id, *]].functor[Int, Double, String])
  checkAll("Monoid[Cmd]", MonoidTests[Cmd[Id, String]].monoid)

}
