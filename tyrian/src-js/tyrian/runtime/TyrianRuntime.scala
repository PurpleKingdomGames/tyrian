package tyrian.runtime

import cats.effect.kernel.Async
import cats.effect.kernel.Clock
import cats.effect.kernel.Ref
import cats.effect.std.AtomicCell
import cats.effect.std.Dispatcher
import cats.effect.std.Queue
import cats.effect.syntax.all.*
import cats.syntax.all.*
import org.scalajs.dom.Element
import snabbdom.toVNode
import tyrian.Cmd
import tyrian.Html
import tyrian.Location
import tyrian.Sub

object TyrianRuntime:

  def apply[F[_], Model, Msg](
      router: Location => Msg,
      node: Element,
      initModel: Model,
      initCmd: Cmd[F, Msg],
      update: Model => Msg => (Model, Cmd[F, Msg]),
      view: Model => Html[Msg],
      subscriptions: Model => Sub[F, Msg]
  )(using F: Async[F]): F[Nothing] =
    Dispatcher.sequential[F].use { dispatcher =>
      val loop        = mainLoop(dispatcher, router, initCmd, update, view, subscriptions)
      val model       = F.ref(initModel)
      val currentSubs = AtomicCell[F].of(List.empty[(String, F[Unit])])
      val msgQueue    = Queue.unbounded[F, Msg]
      val renderer    = Renderer.init(toVNode(node))

      (model, currentSubs, msgQueue, renderer).flatMapN(loop)
    }

  def mainLoop[F[_], Model, Msg](
      dispatcher: Dispatcher[F],
      router: Location => Msg,
      initCmd: Cmd[F, Msg],
      update: Model => Msg => (Model, Cmd[F, Msg]),
      view: Model => Html[Msg],
      subscriptions: Model => Sub[F, Msg]
  )(
      model: Ref[F, Model],
      currentSubs: AtomicCell[F, List[(String, F[Unit])]],
      msgQueue: Queue[F, Msg],
      renderer: Ref[F, Renderer]
  )(using F: Async[F], clock: Clock[F]): F[Nothing] =
    val runCmd: Cmd[F, Msg] => F[Unit] = runCommands(msgQueue)
    val runSub: Sub[F, Msg] => F[Unit] = runSubscriptions(currentSubs, msgQueue, dispatcher)
    val onMsg: Msg => Unit             = postMsg(dispatcher, msgQueue)

    val msgLoop: F[Nothing] =
      msgQueue.take.flatMap { msg =>
        for {
          cmdsAndSubs <- model.modify { oldModel =>
            val (newModel, cmd) = update(oldModel)(msg)
            val sub             = subscriptions(newModel)

            (newModel, (cmd, sub))
          }

          _ <- runCmd(cmdsAndSubs._1) *> runSub(cmdsAndSubs._2)
          _ <- Renderer.redraw(dispatcher, renderer, model, view, onMsg, router)(using F, clock)
        } yield ()
      }.foreverM

    msgLoop.background.surround {
      runCmd(initCmd) *> F.never
    }

  def runCommands[F[_], Msg](msgQueue: Queue[F, Msg])(cmd: Cmd[F, Msg])(using F: Async[F]): F[Unit] =
    CmdHelper.cmdToTaskList(cmd).foldMapM { task =>
      task.handleError(_ => None).flatMap(_.traverse_(msgQueue.offer(_))).start.void
    }

  def runSubscriptions[F[_], Msg](
      currentSubs: AtomicCell[F, List[(String, F[Unit])]],
      msgQueue: Queue[F, Msg],
      dispatcher: Dispatcher[F]
  )(sub: Sub[F, Msg])(using F: Async[F]): F[Unit] =
    currentSubs.evalUpdate { oldSubs =>
      val allSubs                 = SubHelper.flatten(sub)
      val (stillAlive, discarded) = SubHelper.aliveAndDead(allSubs, oldSubs)

      val newSubs = SubHelper
        .findNewSubs(allSubs, stillAlive.map(_._1), Nil)
        .traverse(
          SubHelper.runObserve(_) { result =>
            dispatcher.unsafeRunAndForget(
              result.toOption.flatten.foldMapM(msgQueue.offer(_).void)
            )
          }
        )

      discarded.foldMapM(_.start.void) *> newSubs.map(_ ++ stillAlive)
    }

  def postMsg[F[_], Msg](dispatcher: Dispatcher[F], msgQueue: Queue[F, Msg]): Msg => Unit =
    msg => dispatcher.unsafeRunAndForget(msgQueue.offer(msg))
