package tyrian.ui.datatypes

import tyrian.next.Batch

class DataSetTests extends munit.FunSuite {

  final case class Person(name: String, age: Int, likesCheese: Boolean)
  object Person:
    given DataSet.ColumnOps[Person, Int] with
      def read(data: Person): Int = data.age

  test("Can construct a DataSet") {

    val people =
      Batch(
        Person("Alice", 18, true),
        Person("Bob", 47, false)
      )

    val dataset: DataSet[Person] =
      DataSet
        .empty[Person]
        .addColumn[String]("Name", _.name, identity)
        .addColumn[Int]("Age")
        .addColumn[Boolean]("CheeseLover", _.likesCheese)
        .withData(people)

    val actual =
      dataset.rows

    val expected =
      Batch(
        Batch("Alice", "18", "true"),
        Batch("Bob", "47", "false")
      )

    assertEquals(actual, expected)
  }

  test("Can construct a DataSet") {

    val people =
      Batch(
        Person("Alice", 18, true),
        Person("Bob", 47, false)
      )

    val dataset: DataSet[Person] =
      DataSet
        .empty[Person]
        .addColumn[String]("Name", _.name, identity)
        .addColumn[Int]("Age")
        .addColumn[Boolean]("CheeseLover", _.likesCheese)
        .withData(people)

    val actual =
      dataset.headers

    val expected =
      Batch("Name", "Age", "CheeseLover")

    assertEquals(actual, expected)
  }

}
