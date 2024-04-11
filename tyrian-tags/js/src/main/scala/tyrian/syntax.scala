package tyrian

object syntax:

  extension [M](oa: Option[Elem[M]]) def orEmpty: Elem[M] = oa.getOrElse(tyrian.Empty)
