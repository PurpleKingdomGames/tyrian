package domtests

import org.scalajs.dom.document

class ExampleJsDomTests extends munit.FunSuite {

  test("Example jsdom test") {
    val id      = "my-fancy-element"
    val content = "Hi there and greetings!"

    // Create a new div element
    val newDiv = document.createElement("div")

    // Create an id attribue and assign it to the div
    val a = document.createAttribute("id")
    a.value = id;
    newDiv.setAttributeNode(a)

    // Create some text content
    val newContent = document.createTextNode(content)

    // Add the text node to the newly created div
    newDiv.appendChild(newContent)

    // Add the newly created element and its content into the DOM
    document.body.appendChild(newDiv)

    // Find the element by id on the page, and compare the contents
    assertEquals(document.getElementById(id).innerHTML, content)
  }

}
