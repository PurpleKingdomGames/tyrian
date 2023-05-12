package tyrian

import tyrian.Html.*

import scala.annotation.targetName

class HtmlTests extends munit.FunSuite {

  test("'draggable' attribute can have different types") {

    val attr1 = draggable := "fish"
    // val attr2 = draggable := 10 // Invalid, does not compile
    // val attr3 = draggable := 101.5d  // Invalid, does not compile
    val attr4 = draggable := true // Invalid, does not compile

    assert(clue(attr1.name) == "draggable")
    assert(clue(attr1.value) == "fish")
    assert(clue(attr4.name) == "draggable")
    assert(clue(attr4.value) == "true")

  }

  test("'high' attribute can have different types") {

    val attr1 = high := "fish"
    val attr2 = high := 10
    val attr3 = high := 101.5d
    // val attr4 = high := true // Invalid, does not compile

    assert(clue(attr1.name) == "high")
    assert(clue(attr1.value) == "fish")
    assert(clue(attr2.name) == "high")
    assert(clue(attr2.value) == "10")
    assert(clue(attr3.name) == "high")
    assert(clue(attr3.value) == "101.5")

  }

  test("'low' attribute can have different types") {

    val attr1 = low := "fish"
    val attr2 = low := 10
    val attr3 = low := 101.5d
    // val attr4 = low := true // Invalid, does not compile

    assert(clue(attr1.name) == "low")
    assert(clue(attr1.value) == "fish")
    assert(clue(attr2.name) == "low")
    assert(clue(attr2.value) == "10")
    assert(clue(attr3.name) == "low")
    assert(clue(attr3.value) == "101.5")

  }

  test("'optimum' attribute can have different types") {

    val attr1 = optimum := "fish"
    val attr2 = optimum := 10
    val attr3 = optimum := 101.5d
    // val attr4 = optimum := true // Invalid, does not compile

    assert(clue(attr1.name) == "optimum")
    assert(clue(attr1.value) == "fish")
    assert(clue(attr2.name) == "optimum")
    assert(clue(attr2.value) == "10")
    assert(clue(attr3.name) == "optimum")
    assert(clue(attr3.value) == "101.5")

  }

  test("'value' property can have different types") {

    val attr1 = value := "fish"
    val attr2 = value := 10.toString
    val attr3 = value := 101.5d.toString
    val attr4 = value := true.toString

    assert(clue(attr1.name) == "value")
    assert(clue(attr1.value) == "fish")
    assert(clue(attr2.name) == "value")
    assert(clue(attr2.value) == "10")
    assert(clue(attr3.name) == "value")
    assert(clue(attr3.value) == "101.5")
    assert(clue(attr4.name) == "value")
    assert(clue(attr4.value) == "true")

  }

}
