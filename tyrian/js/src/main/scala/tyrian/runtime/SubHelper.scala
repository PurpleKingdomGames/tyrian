package tyrian.runtime

import cats.effect.kernel.Concurrent
import tyrian.Sub

import scala.annotation.tailrec

object SubHelper:

  // Flatten all the subs into a list of indvidual subs.
  def flatten[F[_]: Concurrent, Msg](sub: Sub[F, Msg]): List[Sub.Observe[F, _, Msg]] =
    @tailrec
    def rec(remaining: List[Sub[F, Msg]], acc: List[Sub.Observe[F, _, Msg]]): List[Sub.Observe[F, _, Msg]] =
      remaining match
        case Nil =>
          acc

        case Sub.Empty :: ss =>
          rec(ss, acc)

        case Sub.Combine(s1, s2) :: ss =>
          rec(s1 :: s2 :: ss, acc)

        case Sub.Batch(sbs) :: ss =>
          rec(sbs ++ ss, acc)

        case (s: Sub.Observe[_, _, _]) :: ss =>
          rec(ss, s.asInstanceOf[Sub.Observe[F, _, Msg]] :: acc)

    rec(List(sub), Nil)

  def aliveAndDead[F[_]: Concurrent, Msg](
      subs: List[Sub.Observe[F, _, Msg]],
      current: List[(String, F[Unit])]
  ): (List[(String, F[Unit])], List[F[Unit]]) =
    val (a, d) = current.partition { case (id, _) => subs.exists(_.id == id) }
    (a, d.map(_._2))

  def findNewSubs[F[_]: Concurrent, Msg](
      subs: List[Sub.Observe[F, _, Msg]],
      alive: List[String],
      inProgress: List[String]
  ): List[Sub.Observe[F, _, Msg]] =
    subs.filter(s => alive.forall(_ != s.id) && !inProgress.contains(s.id))

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def toRun[F[_]: Concurrent, Msg](newSubs: List[Sub.Observe[F, _, Msg]], callback: Msg => Unit): List[F[SubToRun[F]]] =
    newSubs.map { case Sub.Observe(id, observable, toMsg) =>
      Concurrent[F].map(observable) { run =>
        val cancel = run {
          case Left(e)  => throw e
          case Right(m) => callback(toMsg(m))
        }

        SubToRun(id, cancel)
      }
    }

  final case class SubToRun[F[_]: Concurrent](id: String, cancel: F[Unit])
