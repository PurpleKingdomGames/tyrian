package tyrian.next

import cats.effect.IO
import tyrian.Cmd
import tyrian.Sub

object syntax:

  extension (cmd: Cmd[IO, GlobalMsg]) def toAction: Action = Action.fromCmd(cmd)

  extension (sub: Sub[IO, GlobalMsg]) def toWatcher: Watcher = Watcher.fromSub(sub)

  val ==: = Batch.==:
  val :== = Batch.:==

  extension [A](values: Option[A])
    def toBatch: Batch[A]                          = Batch.fromOption(values)
    def toOutcome(error: => Throwable): Outcome[A] = Outcome.fromOption(values, error)

  extension [A](b: Batch[Outcome[A]]) def sequence: Outcome[Batch[A]] = Outcome.sequenceBatch(b)
  extension [A](l: List[Outcome[A]]) def sequence: Outcome[List[A]]   = Outcome.sequenceList(l)
  extension [A](b: Batch[Option[A]]) def sequence: Option[Batch[A]]   = Batch.sequenceOption(b)
  extension [A](l: List[Option[A]]) def sequence: Option[List[A]]     = Batch.sequenceListOption(l)

  extension [A](values: scalajs.js.Array[A]) def toBatch: Batch[A] = Batch.fromJSArray(values)
  extension [A](values: Array[A]) def toBatch: Batch[A]            = Batch.fromArray(values)
  extension [A](values: List[A]) def toBatch: Batch[A]             = Batch.fromList(values)
  extension [A](values: Set[A]) def toBatch: Batch[A]              = Batch.fromSet(values)
  extension [A](values: Seq[A]) def toBatch: Batch[A]              = Batch.fromSeq(values)
  extension [A](values: IndexedSeq[A]) def toBatch: Batch[A]       = Batch.fromIndexedSeq(values)
  extension [A](values: Iterator[A]) def toBatch: Batch[A]         = Batch.fromIterator(values)
  extension [K, V](values: Map[K, V]) def toBatch: Batch[(K, V)]   = Batch.fromMap(values)
  extension (values: Range) def toBatch: Batch[Int]                = Batch.fromRange(values)
