package example

final case class Point(x: Int, y: Int)

final case class GameOfLife(width: Int, height: Int, cells: Set[Point]):
  def advance(): GameOfLife = this
    .copy(cells = (for {
      x <- 0 until width
      y <- 0 until height
      point = Point(x, y)
      neighbours = Set(
        Point(x + 1, y - 1),
        Point(x, y - 1),
        Point(x - 1, y - 1),
        Point(x + 1, y),
        Point(x - 1, y),
        Point(x + 1, y + 1),
        Point(x, y + 1),
        Point(x - 1, y + 1)
      )
      nCount = neighbours.count(cells.contains)
      isLive = cells.contains(point)
      if (isLive && (nCount == 2 || nCount == 3)) || (!isLive && nCount == 3)
    } yield Point(x, y)).toSet)

object GameOfLife:
  val chaotic: GameOfLife =
    GameOfLife(50, 50, Set(Point(20, 20), Point(21, 20), Point(21, 21), Point(21, 22), Point(22, 21)))
