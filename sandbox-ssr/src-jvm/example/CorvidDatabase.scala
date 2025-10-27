package example

import cats.Applicative
import cats.implicits.*

trait CorvidDatabase[F[_]]:
  def search(query: String): F[List[(String, String)]]
  def listAll: F[List[(String, String)]]

object CorvidDatabase:
  private val data: List[(String, String)] = List(
    "Canada Jay"            -> "Perisoreus canadensis",
    "Green Jay"             -> "Cyanocorax yncas",
    "Pinyon Jay"            -> "Gymnorhinus cyanocephalus",
    "Steller's Jay"         -> "Cyanocitta stelleri",
    "Blue Jay"              -> "Cyanocitta cristata",
    "Florida Scrub-Jay"     -> "Aphelocoma coerulescens",
    "California Scrub-Jay"  -> "Aphelocoma californica",
    "Woodhouse's Scrub-Jay" -> "Aphelocoma woodhouseii",
    "Mexican Jay"           -> "Aphelocoma wollweberi",
    "Black-billed Magpie"   -> "Pica hudsonia",
    "Yellow-billed Magpie"  -> "Pica nuttalli",
    "Clark's Nutcracker"    -> "Nucifraga columbiana",
    "American Crow"         -> "Corvus brachyrhynchos",
    "Fish Crow"             -> "Corvus ossifragus",
    "Chihuahuan Raven"      -> "Corvus cryptoleucus",
    "Eurasian Magpie"       -> "Pica pica",
    "Eurasian Jackdaw"      -> "Corvus monedula",
    "Common Raven"          -> "Corvus corax"
  )

  def fakeImpl[F[_]: Applicative]: CorvidDatabase[F] =
    new CorvidDatabase[F]:
      override def search(query: String): F[List[(String, String)]] =
        data
          .filter(row =>
            row._1.toLowerCase.contains(query.toLowerCase) ||
              row._2.toLowerCase.contains(query.toLowerCase)
          )
          .pure[F]

      override def listAll: F[List[(String, String)]] =
        data.pure[F]
