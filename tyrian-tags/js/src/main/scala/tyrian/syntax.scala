package tyrian

import tyrian.Html.text

object syntax:

  extension [M](oa: Option[Elem[M]]) def orEmpty: Elem[M] = oa.getOrElse(tyrian.Empty)

  given Conversion[String, Text] = text(_)
  given Conversion[Int, Text]    = n => text(n.toString)
  given Conversion[Double, Text] = d => text(d.toString)
