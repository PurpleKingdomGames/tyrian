package tyrian.cmds

import cats.effect.IO
import tyrian.Cmd

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
    def toCmd: Cmd[RandomValue.NextInt] =
      Cmd.Run(IO(i.value), RandomValue.NextInt(_))

  extension (i: RandomValue.NextLong)
    def toCmd: Cmd[RandomValue.NextLong] =
      Cmd.Run(IO(i.value), RandomValue.NextLong(_))

  extension (i: RandomValue.NextFloat)
    def toCmd: Cmd[RandomValue.NextFloat] =
      Cmd.Run(IO(i.value), RandomValue.NextFloat(_))

  extension (i: RandomValue.NextDouble)
    def toCmd: Cmd[RandomValue.NextDouble] =
      Cmd.Run(IO(i.value), RandomValue.NextDouble(_))

  extension (i: RandomValue.NextAlphaNumeric)
    def toCmd: Cmd[RandomValue.NextAlphaNumeric] =
      Cmd.Run(IO(i.value), RandomValue.NextAlphaNumeric(_))

  extension [A](i: RandomValue.NextShuffle[A])
    def toCmd: Cmd[RandomValue.NextShuffle[A]] =
      Cmd.Run(IO(i.value), RandomValue.NextShuffle(_))

enum RandomValue derives CanEqual:
  case NextInt(value: Int)             extends RandomValue
  case NextLong(value: Long)           extends RandomValue
  case NextFloat(value: Float)         extends RandomValue
  case NextDouble(value: Double)       extends RandomValue
  case NextAlphaNumeric(value: String) extends RandomValue
  case NextShuffle[A](value: List[A])  extends RandomValue
