package tyrian.cmds

import cats.effect.kernel.Sync
import tyrian.Cmd

/** Generate random values. */
object Random:

  private val r: scala.util.Random =
    new scala.util.Random()

  /** Random `Int` */
  def int[F[_]: Sync]: Cmd[F, RandomValue.NextInt] =
    RandomValue.NextInt(r.nextInt).toCmd

  /** Random `Int` within an upper limit */
  def int[F[_]: Sync](upperLimit: Int): Cmd[F, RandomValue.NextInt] =
    RandomValue.NextInt(r.nextInt(upperLimit)).toCmd

  /** Random `Long` */
  def long[F[_]: Sync]: Cmd[F, RandomValue.NextLong] =
    RandomValue.NextLong(r.nextLong).toCmd

  /** Random `Long` within an upper limit */
  def long[F[_]: Sync](upperLimit: Long): Cmd[F, RandomValue.NextLong] =
    RandomValue.NextLong(r.nextLong(upperLimit)).toCmd

  /** Random `Float` */
  def float[F[_]: Sync]: Cmd[F, RandomValue.NextFloat] =
    RandomValue.NextFloat(r.nextFloat).toCmd

  /** Random `Double` */
  def double[F[_]: Sync]: Cmd[F, RandomValue.NextDouble] =
    RandomValue.NextDouble(r.nextDouble).toCmd

  /** Random series of alphanumeric characters */
  def alphaNumeric[F[_]: Sync](length: Int): Cmd[F, RandomValue.NextAlphaNumeric] =
    RandomValue.NextAlphaNumeric(r.alphanumeric.take(length).mkString).toCmd

  /** Randomly shuffle a list of elements */
  def shuffle[F[_]: Sync, A](l: List[A]): Cmd[F, RandomValue.NextShuffle[A]] =
    RandomValue.NextShuffle(r.shuffle(l)).toCmd

  /** Random values produced based on a specific seed value */
  final case class Seeded(seed: Long):

    private val r: scala.util.Random =
      new scala.util.Random(seed)

    /** Random `Int` */
    def int[F[_]: Sync]: Cmd[F, RandomValue.NextInt] =
      RandomValue.NextInt(r.nextInt).toCmd

    /** Random `Int` within an upper limit */
    def int[F[_]: Sync](upperLimit: Int): Cmd[F, RandomValue.NextInt] =
      RandomValue.NextInt(r.nextInt(upperLimit)).toCmd

    /** Random `Long` */
    def long[F[_]: Sync]: Cmd[F, RandomValue.NextLong] =
      RandomValue.NextLong(r.nextLong).toCmd

    /** Random `Long` within an upper limit */
    def long[F[_]: Sync](upperLimit: Long): Cmd[F, RandomValue.NextLong] =
      RandomValue.NextLong(r.nextLong(upperLimit)).toCmd

    /** Random `Float` */
    def float[F[_]: Sync]: Cmd[F, RandomValue.NextFloat] =
      RandomValue.NextFloat(r.nextFloat).toCmd

    /** Random `Double` */
    def double[F[_]: Sync]: Cmd[F, RandomValue.NextDouble] =
      RandomValue.NextDouble(r.nextDouble).toCmd

    /** Random series of alphanumeric characters */
    def alphaNumeric[F[_]: Sync](length: Int): Cmd[F, RandomValue.NextAlphaNumeric] =
      RandomValue.NextAlphaNumeric(r.alphanumeric.take(length).mkString).toCmd

    /** Randomly shuffle a list of elements */
    def shuffle[F[_]: Sync, A](l: List[A]): Cmd[F, RandomValue.NextShuffle[A]] =
      RandomValue.NextShuffle(r.shuffle(l)).toCmd

  end Seeded

  extension [F[_]: Sync](i: RandomValue.NextInt)
    def toCmd: Cmd[F, RandomValue.NextInt] =
      Cmd.Run(Sync[F].delay(i.value), RandomValue.NextInt(_))

  extension [F[_]: Sync](i: RandomValue.NextLong)
    def toCmd: Cmd[F, RandomValue.NextLong] =
      Cmd.Run(Sync[F].delay(i.value), RandomValue.NextLong(_))

  extension [F[_]: Sync](i: RandomValue.NextFloat)
    def toCmd: Cmd[F, RandomValue.NextFloat] =
      Cmd.Run(Sync[F].delay(i.value), RandomValue.NextFloat(_))

  extension [F[_]: Sync](i: RandomValue.NextDouble)
    def toCmd: Cmd[F, RandomValue.NextDouble] =
      Cmd.Run(Sync[F].delay(i.value), RandomValue.NextDouble(_))

  extension [F[_]: Sync](i: RandomValue.NextAlphaNumeric)
    def toCmd: Cmd[F, RandomValue.NextAlphaNumeric] =
      Cmd.Run(Sync[F].delay(i.value), RandomValue.NextAlphaNumeric(_))

  extension [F[_]: Sync, A](i: RandomValue.NextShuffle[A])
    def toCmd: Cmd[F, RandomValue.NextShuffle[A]] =
      Cmd.Run(Sync[F].delay(i.value), RandomValue.NextShuffle(_))

enum RandomValue derives CanEqual:
  case NextInt(value: Int)             extends RandomValue
  case NextLong(value: Long)           extends RandomValue
  case NextFloat(value: Float)         extends RandomValue
  case NextDouble(value: Double)       extends RandomValue
  case NextAlphaNumeric(value: String) extends RandomValue
  case NextShuffle[A](value: List[A])  extends RandomValue
