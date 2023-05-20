package tyrian.runtime

import cats.effect.kernel.Concurrent
import cats.effect.kernel.Resource
import cats.effect.syntax.all.*
import cats.syntax.all.*
import tyrian.Sub

import scala.annotation.tailrec

object SubHelper:

  // Flatten all the subs into a list of indvidual subs.
  def flatten[F[_], Msg](sub: Sub[F, Msg]): List[Sub.Observe[F, _, Msg]] =
    @tailrec
    def rec(remaining: List[Sub[F, Msg]], acc: List[Sub.Observe[F, _, Msg]]): List[Sub.Observe[F, _, Msg]] =
      remaining match
        case Nil =>
          acc

        case Sub.None :: ss =>
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

  def runObserve[F[_], A, Msg](sub: Sub.Observe[F, A, Msg])(callback: Either[Throwable, Option[Msg]] => Unit)(using
      F: Concurrent[F]
  ): F[(String, F[Unit])] =
    Resource
      .makeFull[F, Option[F[Unit]]] { poll =>
        poll {
          sub.observable.flatMap { run =>
            run(result => callback(result.map(sub.toMsg(_))))
          }
        }
      }(_.getOrElse(F.unit))
      .useForever
      .start
      .map(_.cancel)
      .tupleLeft(sub.id)
