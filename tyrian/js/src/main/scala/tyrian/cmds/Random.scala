package tyrian.cmds

import tyrian.Cmd
import tyrian.Task

/** A Cmd to generate random values.
  */
object Random:

  private val r: scala.util.Random =
    new scala.util.Random()

  def int: Cmd[RandomValue.NextInt] =
    RandomValue.NextInt(r.nextInt).toCmd
  def int(upperLimit: Int): Cmd[RandomValue.NextInt] =
    RandomValue.NextInt(r.nextInt(upperLimit)).toCmd

  def long: Cmd[RandomValue.NextLong] =
    RandomValue.NextLong(r.nextLong).toCmd
  def long(upperLimit: Long): Cmd[RandomValue.NextLong] =
    RandomValue.NextLong(r.nextLong(upperLimit)).toCmd

  def float: Cmd[RandomValue.NextFloat] =
    RandomValue.NextFloat(r.nextFloat).toCmd

  def double: Cmd[RandomValue.NextDouble] =
    RandomValue.NextDouble(r.nextDouble).toCmd

  def alphaNumeric(length: Int): Cmd[RandomValue.NextAlphaNumeric] =
    RandomValue.NextAlphaNumeric(r.alphanumeric.take(length).mkString).toCmd

  def shuffle[A](l: List[A]): Cmd[RandomValue.NextShuffle[A]] =
    RandomValue.NextShuffle(r.shuffle(l)).toCmd

  final case class Seeded(seed: Long):

    private val r: scala.util.Random =
      new scala.util.Random(seed)

    def int: Cmd[RandomValue.NextInt] =
      RandomValue.NextInt(r.nextInt).toCmd
    def int(upperLimit: Int): Cmd[RandomValue.NextInt] =
      RandomValue.NextInt(r.nextInt(upperLimit)).toCmd

    def long: Cmd[RandomValue.NextLong] =
      RandomValue.NextLong(r.nextLong).toCmd
    def long(upperLimit: Long): Cmd[RandomValue.NextLong] =
      RandomValue.NextLong(r.nextLong(upperLimit)).toCmd

    def float: Cmd[RandomValue.NextFloat] =
      RandomValue.NextFloat(r.nextFloat).toCmd

    def double: Cmd[RandomValue.NextDouble] =
      RandomValue.NextDouble(r.nextDouble).toCmd

    def alphaNumeric(length: Int): Cmd[RandomValue.NextAlphaNumeric] =
      RandomValue.NextAlphaNumeric(r.alphanumeric.take(length).mkString).toCmd

    def shuffle[A](l: List[A]): Cmd[RandomValue.NextShuffle[A]] =
      RandomValue.NextShuffle(r.shuffle(l)).toCmd

  end Seeded

  extension (i: RandomValue.NextInt)
    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def toCmd: Cmd[RandomValue.NextInt] =
      val f: Either[_, Int] => RandomValue.NextInt = {
        case Right(v: Int) => RandomValue.NextInt(v)
        case _             => throw new Exception("Unfailable random has failed!")
      }
      Cmd.RunTask(Task.Succeeded(i.value), f)

  extension (i: RandomValue.NextLong)
    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def toCmd: Cmd[RandomValue.NextLong] =
      val f: Either[_, Long] => RandomValue.NextLong = {
        case Right(v: Long) => RandomValue.NextLong(v)
        case _              => throw new Exception("Unfailable random has failed!")
      }
      Cmd.RunTask(Task.Succeeded(i.value), f)

  extension (i: RandomValue.NextFloat)
    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def toCmd: Cmd[RandomValue.NextFloat] =
      val f: Either[_, Float] => RandomValue.NextFloat = {
        case Right(v: Float) => RandomValue.NextFloat(v)
        case _               => throw new Exception("Unfailable random has failed!")
      }
      Cmd.RunTask(Task.Succeeded(i.value), f)

  extension (i: RandomValue.NextDouble)
    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def toCmd: Cmd[RandomValue.NextDouble] =
      val f: Either[_, Double] => RandomValue.NextDouble = {
        case Right(v: Double) => RandomValue.NextDouble(v)
        case _                => throw new Exception("Unfailable random has failed!")
      }
      Cmd.RunTask(Task.Succeeded(i.value), f)

  extension (i: RandomValue.NextAlphaNumeric)
    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def toCmd: Cmd[RandomValue.NextAlphaNumeric] =
      val f: Either[_, String] => RandomValue.NextAlphaNumeric = {
        case Right(v: String) => RandomValue.NextAlphaNumeric(v)
        case _                => throw new Exception("Unfailable random has failed!")
      }
      Cmd.RunTask(Task.Succeeded(i.value), f)
      
  extension [A](i: RandomValue.NextShuffle[A])
    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def toCmd: Cmd[RandomValue.NextShuffle[A]] =
      val f: Either[_, List[A]] => RandomValue.NextShuffle[A] = {
        case Right(v: List[A]) => RandomValue.NextShuffle(v)
        case _                 => throw new Exception("Unfailable random has failed!")
      }
      Cmd.RunTask(Task.Succeeded(i.value), f)

enum RandomValue derives CanEqual:
  case NextInt(value: Int)             extends RandomValue
  case NextLong(value: Long)           extends RandomValue
  case NextFloat(value: Float)         extends RandomValue
  case NextDouble(value: Double)       extends RandomValue
  case NextAlphaNumeric(value: String) extends RandomValue
  case NextShuffle[A](value: List[A])  extends RandomValue
