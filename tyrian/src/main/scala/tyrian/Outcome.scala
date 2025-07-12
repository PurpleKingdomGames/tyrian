// package tyrian

// import scala.annotation.tailrec
// import scala.util.control.NonFatal

// /** An `Outcome` represents the result of some part of a frame update. It contains a value or an error (exception), and
//   * optionally a list of events to be processed on the next frame.
//   */
// sealed trait Outcome[F[_], +A, Msg] derives CanEqual:

//   def isResult: Boolean
//   def isError: Boolean

//   def unsafeGet: A
//   def getOrElse[B >: A](b: => B): B
//   def orElse[B >: A](b: => Outcome[F, B, Msg]): Outcome[F, B, Msg]

//   def unsafeGlobalEvents: List[Msg]
//   def globalEventsOrNil: List[Msg]

//   def handleError[B >: A](recoverWith: Throwable => Outcome[F, B, Msg]): Outcome[F, B, Msg]

//   def logCrash(reporter: PartialFunction[Throwable, String]): Outcome[F, A, Msg]

//   def addGlobalEvents(newMsgs: Msg*): Outcome[F, A, Msg]

//   def addGlobalEvents(newMsgs: => List[Msg]): Outcome[F, A, Msg]

//   def createGlobalEvents(f: A => List[Msg]): Outcome[F, A, Msg]

//   def clearGlobalEvents: Outcome[F, A, Msg]

//   def replaceGlobalEvents(f: List[Msg] => List[Msg]): Outcome[F, A, Msg]

//   def eventsAsOutcome: Outcome[F, List[Msg], Msg]

//   def mapAll[B](f: A => B, g: List[Msg] => List[Msg]): Outcome[F, B, Msg]

//   def map[B](f: A => B): Outcome[F, B, Msg]

//   def mapGlobalEvents(f: Msg => Msg): Outcome[F, A, Msg]

//   def ap[B](of: Outcome[F, A => B, Msg]): Outcome[F, B, Msg]

//   def merge[B, C](other: Outcome[F, B, Msg])(f: (A, B) => C): Outcome[F, C, Msg]

//   def combine[B](other: Outcome[F, B, Msg]): Outcome[F, (A, B), Msg]

//   def flatMap[B](f: A => Outcome[F, B, Msg]): Outcome[F, B, Msg]

// object Outcome:

//   final case class Result[F[_], +A, Msg](state: A, globalEvents: List[Msg]) extends Outcome[F, A, Msg] {

//     def isResult: Boolean = true
//     def isError: Boolean  = false

//     def unsafeGet: A =
//       state
//     def getOrElse[B >: A](b: => B): B =
//       state
//     def orElse[B >: A](b: => Outcome[F, B, Msg]): Outcome[F, B, Msg] =
//       this

//     def unsafeGlobalEvents: List[Msg] =
//       globalEvents
//     def globalEventsOrNil: List[Msg] =
//       globalEvents

//     def handleError[B >: A](recoverWith: Throwable => Outcome[F, B, Msg]): Outcome[F, B, Msg] =
//       this

//     def logCrash(reporter: PartialFunction[Throwable, String]): Outcome[F, A, Msg] =
//       this

//     def addGlobalEvents(newMsgs: Msg*): Outcome[F, A, Msg] =
//       addGlobalEvents(newMsgs.toList)

//     def addGlobalEvents(newMsgs: => List[Msg]): Outcome[F, A, Msg] =
//       Outcome(state, globalEvents ++ newMsgs)

//     def createGlobalEvents(f: A => List[Msg]): Outcome[F, A, Msg] =
//       Outcome(state, globalEvents ++ f(state))

//     def clearGlobalEvents: Outcome[F, A, Msg] =
//       Outcome(state)

//     def replaceGlobalEvents(f: List[Msg] => List[Msg]): Outcome[F, A, Msg] =
//       Outcome(state, f(globalEvents))

//     def eventsAsOutcome: Outcome[F, List[Msg], Msg] =
//       Outcome(globalEvents)

//     def mapAll[B](f: A => B, g: List[Msg] => List[Msg]): Outcome[F, B, Msg] =
//       Outcome(f(state), g(globalEvents))

//     def map[B](f: A => B): Outcome[F, B, Msg] =
//       Outcome(f(state), globalEvents)

//     def mapGlobalEvents(f: Msg => Msg): Outcome[F, A, Msg] =
//       Outcome(state, globalEvents.map(f))

//     def ap[B](of: Outcome[F, A => B, Msg]): Outcome[F, B, Msg] =
//       of match {
//         case Error(e, r) =>
//           Error(e, r)

//         case Result(s, es) =>
//           map(s).addGlobalEvents(es)
//       }

//     def merge[B, C](other: Outcome[F, B, Msg])(f: (A, B) => C): Outcome[F, C, Msg] =
//       flatMap(a => other.map(b => (a, b))).map(p => f(p._1, p._2))

//     def combine[B](other: Outcome[F, B, Msg]): Outcome[F, (A, B), Msg] =
//       other match {
//         case Error(e, r) =>
//           Error(e, r)

//         case Result(s, es) =>
//           Outcome((state, s), globalEvents ++ es)
//       }

//     def flatMap[B](f: A => Outcome[F, B, Msg]): Outcome[F, B, Msg] =
//       f(state) match {
//         case Error(e, r) =>
//           Error(e, r)

//         case Result(s, es) =>
//           Outcome(s, globalEvents ++ es)
//       }

//   }

//   @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
//   final case class Error[F[_], Msg](e: Throwable, crashReporter: PartialFunction[Throwable, String]) extends Outcome[F, Nothing, Msg] {

//     def isResult: Boolean = false
//     def isError: Boolean  = true

//     def unsafeGet: Nothing =
//       throw e
//     def getOrElse[B >: Nothing](b: => B): B =
//       b
//     def orElse[B >: Nothing](b: => Outcome[F, B, Msg]): Outcome[F, B, Msg] =
//       b

//     def unsafeGlobalEvents: List[Msg] =
//       throw e
//     def globalEventsOrNil: List[Msg] =
//       List.empty

//     def handleError[B >: Nothing](recoverWith: Throwable => Outcome[F, B, Msg]): Outcome[F, B, Msg] =
//       recoverWith(e)

//     def logCrash(reporter: PartialFunction[Throwable, String]): Outcome[F, Nothing, Msg] =
//       this.copy(crashReporter = reporter)

//     def reportCrash: String =
//       crashReporter.orElse[Throwable, String] { case (e: Throwable) =>
//         e.getMessage + "\n" + e.getStackTrace.mkString("\n")
//       }(e)

//     def addGlobalEvents(newMsgs: Msg*): Error[F, Msg]                                = this
//     def addGlobalEvents(newMsgs: => List[Msg]): Error[F, Msg]                       = this
//     def createGlobalEvents(f: Nothing => List[Msg]): Error[F, Msg]                    = this
//     def clearGlobalEvents: Error[F, Msg]                                                       = this
//     def replaceGlobalEvents(f: List[Msg] => List[Msg]): Error[F, Msg]        = this
//     def eventsAsOutcome: Outcome[F, List[Msg], Msg]                                   = this
//     def mapAll[B](f: Nothing => B, g: List[Msg] => List[Msg]): Error[F, Msg] = this
//     def map[B](f: Nothing => B): Error[F, Msg]                                                 = this
//     def mapGlobalEvents(f: Msg => Msg): Error[F, Msg]                          = this
//     def ap[B](of: Outcome[F, Nothing => B, Msg]): Outcome[F, B, Msg]                                   = this
//     def merge[B, C](other: Outcome[F, B, Msg])(f: (Nothing, B) => C): Error[F, Msg]                    = this
//     def combine[B](other: Outcome[F, B, Msg]): Error[F, Msg]                                           = this
//     def flatMap[B](f: Nothing => Outcome[F, B, Msg]): Error[F, Msg]                                    = this

//   }

//   object Error {
//     def apply[F[_], Msg](e: Throwable): Error[F, Msg] =
//       Error(e, { case (ee: Throwable) => ee.getMessage })
//   }

//   extension [F[_], A, Msg](l: List[Outcome[F, A, Msg]]) def sequence: Outcome[F, List[A], Msg]                   = Outcome.sequenceList(l)

//   extension [F[_], A, B, Msg](t: (Outcome[F, A, Msg], Outcome[F, B, Msg]))
//     def combine: Outcome[F, (A, B), Msg] =
//       t._1.combine(t._2)
//     def merge[C](f: (A, B) => C): Outcome[F, C, Msg] =
//       t._1.merge(t._2)(f)
//     def map2[C](f: (A, B) => C): Outcome[F, C, Msg] =
//       merge(f)

//   extension [F[_], A, B, C, Msg](t: (Outcome[F, A, Msg], Outcome[F, B, Msg], Outcome[F, C, Msg]))
//     def combine: Outcome[F, (A, B, C), Msg] =
//       t match {
//         case (Result(s1, es1), Result(s2, es2), Result(s3, es3)) =>
//           Outcome((s1, s2, s3), es1 ++ es2 ++ es3)

//         case (Error(e, r), _, _) =>
//           Error(e, r)

//         case (_, Error(e, r), _) =>
//           Error(e, r)

//         case (_, _, Error(e, r)) =>
//           Error(e, r)
//       }
//     def merge[D](f: (A, B, C) => D): Outcome[F, D, Msg] =
//       for {
//         aa <- t._1
//         bb <- t._2
//         cc <- t._3
//       } yield f(aa, bb, cc)
//     def map3[D](f: (A, B, C) => D): Outcome[F, D, Msg] =
//       merge(f)

//   def apply[F[_], A, Msg](state: => A): Outcome[F, A, Msg] =
//     try Outcome.Result[F, A, Msg](state, List.empty)
//     catch {
//       case NonFatal(e) =>
//         Outcome.Error(e)
//     }

//   def apply[F[_], A, Msg](state: => A, globalEvents: => List[Msg]): Outcome[F, A, Msg] =
//     try Outcome.Result[F, A, Msg](state, globalEvents)
//     catch {
//       case NonFatal(e) =>
//         Outcome.Error(e)
//     }

//   def unapply[F[_], A, Msg](outcome: Outcome[F, A, Msg]): Option[(A, List[Msg])] =
//     outcome match {
//       case Outcome.Error(_, _) =>
//         None

//       case Outcome.Result(s, es) =>
//         Some((s, es))
//     }

//   def fromOption[F[_], A, Msg](opt: Option[A], error: => Throwable): Outcome[F, A, Msg] =
//     opt match
//       case None        => Outcome.raiseError(error)
//       case Some(value) => Outcome(value)

//   def raiseError[F[_], Msg](throwable: Throwable): Outcome.Error[F, Msg] =
//     Outcome.Error(throwable)

//   // def sequenceBatch[F[_], A, Msg](l: List[Outcome[F, A, Msg]]): Outcome[F, List[A], Msg] =
//   //   given CanEqual[Outcome[F, A, Msg], Outcome[F, A, Msg]] = CanEqual.derived

//   //   @tailrec
//   //   def rec(remaining: List[Outcome[F, A, Msg]], accA: List[A], accEvents: List[Msg]): Outcome[F, List[A], Msg] =
//   //     if remaining.isEmpty then Outcome(accA).addGlobalEvents(accEvents)
//   //     else
//   //       val h = remaining.head
//   //       val t = remaining.tail
//   //       h match
//   //         case Error(e, r) => Error(e, r)
//   //         case Result(s, es) =>
//   //           rec(t, accA ++ List(s), accEvents ++ es)

//   //   rec(l, List.empty, List.empty)

//   // def sequenceNonEmptyBatch[F[_], A, Msg](l: NonEmptyBatch[Outcome[F, A, Msg]]): Outcome[F, NonEmptyBatch[A], Msg] =
//   //   sequence(l.toBatch).map(bb => NonEmptyBatch.fromBatch(bb).get) // Use of get is safe, we know it is non-empty

//   def sequenceList[F[_], A, Msg](l: List[Outcome[F, A, Msg]]): Outcome[F, List[A], Msg] =
//     given CanEqual[Outcome[F, A, Msg], Outcome[F, A, Msg]] = CanEqual.derived

//     @tailrec
//     def rec(remaining: List[Outcome[F, A, Msg]], accA: List[A], accEvents: List[Msg]): Outcome[F, List[A], Msg] =
//       remaining match {
//         case Nil =>
//           Outcome(accA).addGlobalEvents(accEvents)

//         case Error(e, r) :: _ =>
//           Error(e, r)

//         case Result(s, es) :: xs =>
//           rec(xs, accA ++ List(s), accEvents ++ es.toList)
//       }

//     rec(l, Nil, Nil)

//   // def sequenceNonEmptyList[F[_], A, Msg](l: NonEmptyList[Outcome[F, A, Msg]]): Outcome[F, NonEmptyList[A], Msg] =
//   //   sequence(l.toList).map(ll => NonEmptyList.fromList(ll).get) // Use of get is safe, we know it is non-empty

//   def merge[F[_], A, B, C, Msg](oa: Outcome[F, A, Msg], ob: Outcome[F, B, Msg])(f: (A, B) => C): Outcome[F, C, Msg] =
//     oa.merge(ob)(f)
//   def map2[F[_], A, B, C, Msg](oa: Outcome[F, A, Msg], ob: Outcome[F, B, Msg])(f: (A, B) => C): Outcome[F, C, Msg] =
//     merge(oa, ob)(f)
//   def merge3[F[_], A, B, C, D, Msg](oa: Outcome[F, A, Msg], ob: Outcome[F, B, Msg], oc: Outcome[F, C, Msg])(f: (A, B, C) => D): Outcome[F, D, Msg] =
//     for {
//       aa <- oa
//       bb <- ob
//       cc <- oc
//     } yield f(aa, bb, cc)
//   def map3[F[_], A, B, C, D, Msg](oa: Outcome[F, A, Msg], ob: Outcome[F, B, Msg], oc: Outcome[F, C, Msg])(f: (A, B, C) => D): Outcome[F, D, Msg] =
//     merge3(oa, ob, oc)(f)

//   def combine[F[_], A, B, Msg](oa: Outcome[F, A, Msg], ob: Outcome[F, B, Msg]): Outcome[F, (A, B), Msg] =
//     oa.combine(ob)
//   def combine3[F[_], A, B, C, Msg](oa: Outcome[F, A, Msg], ob: Outcome[F, B, Msg], oc: Outcome[F, C, Msg]): Outcome[F, (A, B, C), Msg] =
//     (oa, ob, oc) match {
//       case (Result(s1, es1), Result(s2, es2), Result(s3, es3)) =>
//         Outcome((s1, s2, s3), es1 ++ es2 ++ es3)

//       case (Error(e, r), _, _) =>
//         Error(e, r)

//       case (_, Error(e, r), _) =>
//         Error(e, r)

//       case (_, _, Error(e, r)) =>
//         Error(e, r)
//     }

//   def join[F[_], A, Msg](faa: Outcome[F, Outcome[F, A, Msg], Msg]): Outcome[F, A, Msg] =
//     faa match {
//       case Error(e, r) =>
//         Error(e, r)

//       case Result(outcome, es) =>
//         Outcome(outcome.unsafeGet, es ++ outcome.unsafeGlobalEvents)
//     }
//   def flatten[F[_], A, Msg](faa: Outcome[F, Outcome[F, A, Msg], Msg]): Outcome[F, A, Msg] =
//     join(faa)