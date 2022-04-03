package tyrian.runtime

import cats.effect.kernel.Async
import tyrian.Sub

import scala.annotation.tailrec

object SubRunner:

  // Flatten all the subs into a list of indvidual subs.
  def flatten[F[_]: Async, Msg](sub: Sub[F, Msg]): List[Sub.OfObservable[F, _, Msg]] =
    @tailrec
    def rec(remaining: List[Sub[F, Msg]], acc: List[Sub.OfObservable[F, _, Msg]]): List[Sub.OfObservable[F, _, Msg]] =
      remaining match
        case Nil =>
          acc

        case Sub.Empty() :: ss =>
          rec(ss, acc)

        case Sub.Combine(s1, s2) :: ss =>
          rec(s1 :: s1 :: ss, acc)

        case Sub.Batch(sbs) :: ss =>
          rec(sbs ++ ss, acc)

        case (s: Sub.OfObservable[F, _, _]) :: ss =>
          rec(ss, s.asInstanceOf[Sub.OfObservable[F, _, Msg]] :: acc)

    rec(List(sub), Nil)

  def aliveAndDead[F[_]: Async, Msg](
      subs: List[Sub.OfObservable[F, _, Msg]],
      current: List[(String, F[Unit])]
  ): (List[(String, F[Unit])], List[F[Unit]]) =
    val (a, d) = current.partition { case (id, _) => subs.exists(_.id == id) }
    (a, d.map(_._2))

  def findNewSubs[F[_]: Async, Msg](
      subs: List[Sub.OfObservable[F, _, Msg]],
      alive: List[String],
      inProgress: List[String]
  ): List[Sub.OfObservable[F, _, Msg]] =
    subs.filter(s => alive.forall(_ != s.id) && !inProgress.contains(s.id))

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def toRun[F[_]: Async, Msg](newSubs: List[Sub.OfObservable[F, _, Msg]], callback: Msg => Unit): List[F[SubToRun[F]]] =
    newSubs.map { case Sub.OfObservable(id, observable, toMsg) =>
      Async[F].map(observable) { run =>
        val cancel = run {
          case Left(e)  => throw e
          case Right(m) => callback(toMsg(m))
        }

        SubToRun(id, cancel)
      }
    }

  final case class SubToRun[F[_]: Async](id: String, cancel: F[Unit])
