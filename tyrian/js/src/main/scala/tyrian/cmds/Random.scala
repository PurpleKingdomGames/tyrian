package tyrian.cmds

import cats.effect.kernel.Async
import tyrian.Cmd

/** A Cmd to generate random values.
  */
object Random:

  private val r: scala.util.Random =
    new scala.util.Random()

  def int[F[_]: Async]: Cmd[F, RandomValue.NextInt] =
    RandomValue.NextInt(r.nextInt).toCmd
  def int[F[_]: Async](upperLimit: Int): Cmd[F, RandomValue.NextInt] =
    RandomValue.NextInt(r.nextInt(upperLimit)).toCmd

  def long[F[_]: Async]: Cmd[F, RandomValue.NextLong] =
    RandomValue.NextLong(r.nextLong).toCmd
  def long[F[_]: Async](upperLimit: Long): Cmd[F, RandomValue.NextLong] =
    RandomValue.NextLong(r.nextLong(upperLimit)).toCmd

  def float[F[_]: Async]: Cmd[F, RandomValue.NextFloat] =
    RandomValue.NextFloat(r.nextFloat).toCmd

  def double[F[_]: Async]: Cmd[F, RandomValue.NextDouble] =
    RandomValue.NextDouble(r.nextDouble).toCmd

  def alphaNumeric[F[_]: Async](length: Int): Cmd[F, RandomValue.NextAlphaNumeric] =
    RandomValue.NextAlphaNumeric(r.alphanumeric.take(length).mkString).toCmd

  def shuffle[F[_]: Async, A](l: List[A]): Cmd[F, RandomValue.NextShuffle[A]] =
    RandomValue.NextShuffle(r.shuffle(l)).toCmd

  final case class Seeded(seed: Long):

    private val r: scala.util.Random =
      new scala.util.Random(seed)

    def int[F[_]: Async]: Cmd[F, RandomValue.NextInt] =
      RandomValue.NextInt(r.nextInt).toCmd
    def int[F[_]: Async](upperLimit: Int): Cmd[F, RandomValue.NextInt] =
      RandomValue.NextInt(r.nextInt(upperLimit)).toCmd

    def long[F[_]: Async]: Cmd[F, RandomValue.NextLong] =
      RandomValue.NextLong(r.nextLong).toCmd
    def long[F[_]: Async](upperLimit: Long): Cmd[F, RandomValue.NextLong] =
      RandomValue.NextLong(r.nextLong(upperLimit)).toCmd

    def float[F[_]: Async]: Cmd[F, RandomValue.NextFloat] =
      RandomValue.NextFloat(r.nextFloat).toCmd

    def double[F[_]: Async]: Cmd[F, RandomValue.NextDouble] =
      RandomValue.NextDouble(r.nextDouble).toCmd

    def alphaNumeric[F[_]: Async](length: Int): Cmd[F, RandomValue.NextAlphaNumeric] =
      RandomValue.NextAlphaNumeric(r.alphanumeric.take(length).mkString).toCmd

    def shuffle[F[_]: Async, A](l: List[A]): Cmd[F, RandomValue.NextShuffle[A]] =
      RandomValue.NextShuffle(r.shuffle(l)).toCmd

  end Seeded

  extension [F[_]: Async](i: RandomValue.NextInt)
    def toCmd: Cmd[F, RandomValue.NextInt] =
      Cmd.Run(Async[F].delay(i.value), RandomValue.NextInt(_))

  extension [F[_]: Async](i: RandomValue.NextLong)
    def toCmd: Cmd[F, RandomValue.NextLong] =
      Cmd.Run(Async[F].delay(i.value), RandomValue.NextLong(_))

  extension [F[_]: Async](i: RandomValue.NextFloat)
    def toCmd: Cmd[F, RandomValue.NextFloat] =
      Cmd.Run(Async[F].delay(i.value), RandomValue.NextFloat(_))

  extension [F[_]: Async](i: RandomValue.NextDouble)
    def toCmd: Cmd[F, RandomValue.NextDouble] =
      Cmd.Run(Async[F].delay(i.value), RandomValue.NextDouble(_))

  extension [F[_]: Async](i: RandomValue.NextAlphaNumeric)
    def toCmd: Cmd[F, RandomValue.NextAlphaNumeric] =
      Cmd.Run(Async[F].delay(i.value), RandomValue.NextAlphaNumeric(_))

  extension [F[_]: Async, A](i: RandomValue.NextShuffle[A])
    def toCmd: Cmd[F, RandomValue.NextShuffle[A]] =
      Cmd.Run(Async[F].delay(i.value), RandomValue.NextShuffle(_))

enum RandomValue derives CanEqual:
  case NextInt(value: Int)             extends RandomValue
  case NextLong(value: Long)           extends RandomValue
  case NextFloat(value: Float)         extends RandomValue
  case NextDouble(value: Double)       extends RandomValue
  case NextAlphaNumeric(value: String) extends RandomValue
  case NextShuffle[A](value: List[A])  extends RandomValue
