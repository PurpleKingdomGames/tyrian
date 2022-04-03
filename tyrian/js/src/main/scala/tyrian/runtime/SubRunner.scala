package tyrian.runtime

import cats.effect.IO
import tyrian.Sub

import scala.annotation.tailrec

object SubRunner:

  // Flatten all the subs into a list of indvidual subs.
  def flatten[Msg](sub: Sub[Msg]): List[Sub.OfObservable[_, Msg]] =
    @tailrec
    def rec(remaining: List[Sub[Msg]], acc: List[Sub.OfObservable[_, Msg]]): List[Sub.OfObservable[_, Msg]] =
      remaining match
        case Nil =>
          acc

        case Sub.Empty :: ss =>
          rec(ss, acc)

        case Sub.Combine(s1, s2) :: ss =>
          rec(s1 :: s1 :: ss, acc)

        case Sub.Batch(sbs) :: ss =>
          rec(sbs ++ ss, acc)

        case (s: Sub.OfObservable[_, _]) :: ss =>
          rec(ss, s.asInstanceOf[Sub.OfObservable[_, Msg]] :: acc)

    rec(List(sub), Nil)

  def aliveAndDead[Msg](
      subs: List[Sub.OfObservable[_, Msg]],
      current: List[(String, IO[Unit])]
  ): (List[(String, IO[Unit])], List[IO[Unit]]) =
    val (a, d) = current.partition { case (id, _) => subs.exists(_.id == id) }
    (a, d.map(_._2))

  def findNewSubs[Msg](
      subs: List[Sub.OfObservable[_, Msg]],
      alive: List[String],
      inProgress: List[String]
  ): List[Sub.OfObservable[_, Msg]] =
    subs.filter(s => alive.forall(_ != s.id) && !inProgress.contains(s.id))

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def toRun[Msg](newSubs: List[Sub.OfObservable[_, Msg]], callback: Msg => Unit): List[IO[SubToRun]] =
    newSubs.map { case Sub.OfObservable(id, observable, toMsg) =>
      // Fire off the new sub
      observable
        .map { run =>
          val cancel = run {
            case Left(e)  => throw e
            case Right(m) => callback(toMsg(m))
          }

          SubToRun(id, cancel)
        }
    }

  final case class SubToRun(id: String, cancel: IO[Unit])
