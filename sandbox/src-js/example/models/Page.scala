package example.models

enum Page:
  case Page1, Page2, Page3, Page4, Page5, Page6, Page7

  def toNavLabel: String =
    this match
      case Page1 => "Input fields"
      case Page2 => "Counters"
      case Page3 => "WebSockets"
      case Page4 => "Clock"
      case Page5 => "Http"
      case Page6 => "Form"
      case Page7 => "File Select"

  def toUrlPath: String =
    this match
      case Page1 => "/page1"
      case Page2 => "/page2"
      case Page3 => "/page3"
      case Page4 => "/page4"
      case Page5 => "/page5"
      case Page6 => "/page6"
      case Page7 => "/page7"
