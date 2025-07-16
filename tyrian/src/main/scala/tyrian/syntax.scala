package tyrian

import cats.Applicative
import cats.effect.Async
import cats.effect.Sync

object syntax:

  /** Make a side effect cmd from any `F[Unit]`
    */
  extension [F[_]: Sync](task: F[Unit])
    def toCmd: Cmd.SideEffect[F, Unit] =
      Cmd.SideEffect(task)

  /** Make a cmd from any `F[A]`
    */
  extension [F[_]: Applicative, A](task: F[A])
    def toCmd: Cmd.Run[F, A, A] =
      Cmd.Run[F, A](task)

  /** Make a sub from an `fs2.Stream`
    */
  extension [F[_]: Async, A](stream: fs2.Stream[F, A])
    def toSub(id: String): Sub[F, A] =
      Sub.make(id, stream)

  extension [M](oa: Option[Elem[M]]) def orEmpty: Elem[M] = oa.getOrElse(tyrian.Empty)

  val ==: = Batch.==:
  val :== = Batch.:==

  // TODO: Where can some of this live?
  extension [A](values: Option[A]) def toBatch: Batch[A] = Batch.fromOption(values)
  //   def toOutcome[F[_], Msg](error: => Throwable): Outcome[F, A, Msg] = Outcome.fromOption(values, error)

  //   extension [A](b: Batch[Outcome[A]]) def sequence: Outcome[Batch[A]]                 = Outcome.sequenceBatch(b)
  //   extension [A](b: NonEmptyBatch[Outcome[A]]) def sequence: Outcome[NonEmptyBatch[A]] = Outcome.sequenceNonEmptyBatch(b)
  //   extension [A](l: List[Outcome[A]]) def sequence: Outcome[List[A]]                   = Outcome.sequenceList(l)
  //   extension [A](l: NonEmptyList[Outcome[A]]) def sequence: Outcome[NonEmptyList[A]]   = Outcome.sequenceNonEmptyList(l)
  extension [A](b: Batch[Option[A]]) def sequence: Option[Batch[A]] = Batch.sequenceOption(b)
  // extension [A](b: NonEmptyBatch[Option[A]]) def sequence: Option[NonEmptyBatch[A]] = NonEmptyBatch.sequenceOption(b)
  extension [A](l: List[Option[A]]) def sequence: Option[List[A]] = Batch.sequenceListOption(l)
  // extension [A](l: NonEmptyList[Option[A]]) def sequence: Option[NonEmptyList[A]]   = NonEmptyList.sequenceOption(l)

  extension [A](values: scalajs.js.Array[A]) def toBatch: Batch[A] = Batch.fromJSArray(values)
  extension [A](values: Array[A]) def toBatch: Batch[A]            = Batch.fromArray(values)
  extension [A](values: List[A]) def toBatch: Batch[A]             = Batch.fromList(values)
  extension [A](values: Set[A]) def toBatch: Batch[A]              = Batch.fromSet(values)
  extension [A](values: Seq[A]) def toBatch: Batch[A]              = Batch.fromSeq(values)
  extension [A](values: IndexedSeq[A]) def toBatch: Batch[A]       = Batch.fromIndexedSeq(values)
  extension [A](values: Iterator[A]) def toBatch: Batch[A]         = Batch.fromIterator(values)
  extension [K, V](values: Map[K, V]) def toBatch: Batch[(K, V)]   = Batch.fromMap(values)
  extension (values: Range) def toBatch: Batch[Int]                = Batch.fromRange(values)
