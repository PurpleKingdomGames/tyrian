package tyrian.cmds

import tyrian.Cmd

/** A Cmd to generate random values.
 */
object Random:

  private val r: scala.util.Random =
    new scala.util.Random()

  def int: Cmd[RandomValue] =
    makeCmd(RandomValue.NextInt(r.nextInt))
  def int(upperLimit: Int): Cmd[RandomValue] =
    makeCmd(RandomValue.NextInt(r.nextInt(upperLimit)))

  def long: Cmd[RandomValue] =
    makeCmd(RandomValue.NextLong(r.nextLong))
  def long(upperLimit: Long): Cmd[RandomValue] =
    makeCmd(RandomValue.NextLong(r.nextLong(upperLimit)))

  def float: Cmd[RandomValue] =
    makeCmd(RandomValue.NextFloat(r.nextFloat))

  def double: Cmd[RandomValue] =
    makeCmd(RandomValue.NextDouble(r.nextDouble))

  def alphaNumeric(length: Int): Cmd[RandomValue] =
    makeCmd(RandomValue.NextAlphaNumeric(r.alphanumeric.take(length).mkString))

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def resultToMessage[A]: Either[_, A] => RandomValue = {
    case Right(v: Int)    => RandomValue.NextInt(v)
    case Right(v: Long)   => RandomValue.NextLong(v)
    case Right(v: Float)  => RandomValue.NextFloat(v)
    case Right(v: Double) => RandomValue.NextDouble(v)
    case Right(v: String) => RandomValue.NextAlphaNumeric(v)
    case _                => throw new Exception("Unfailable random has failed!")
  }

  private def makeCmd[A](make: => A): Cmd[RandomValue] =
    Cmd.Run(resultToMessage) { observable =>
      observable.onNext(make)
      () => ()
    }

  final case class Seeded(seed: Long):

    private val r: scala.util.Random =
      new scala.util.Random(seed)

    def int(seed: Long): Cmd[RandomValue] =
      makeCmd(RandomValue.NextInt(r.nextInt))
    def int(seed: Long, upperLimit: Int): Cmd[RandomValue] =
      makeCmd(RandomValue.NextInt(r.nextInt(upperLimit)))

    def long: Cmd[RandomValue] =
      makeCmd(RandomValue.NextLong(r.nextLong))
    def long(upperLimit: Long): Cmd[RandomValue] =
      makeCmd(RandomValue.NextLong(r.nextLong(upperLimit)))

    def float: Cmd[RandomValue] =
      makeCmd(RandomValue.NextFloat(r.nextFloat))

    def double: Cmd[RandomValue] =
      makeCmd(RandomValue.NextDouble(r.nextDouble))

    def alphaNumeric(length: Int): Cmd[RandomValue] =
      makeCmd(RandomValue.NextAlphaNumeric(r.alphanumeric.take(length).mkString))

enum RandomValue derives CanEqual:
  case NextInt(value: Int) extends RandomValue
  case NextLong(value: Long) extends RandomValue
  case NextFloat(value: Float) extends RandomValue
  case NextDouble(value: Double) extends RandomValue
  case NextAlphaNumeric(value: String) extends RandomValue
