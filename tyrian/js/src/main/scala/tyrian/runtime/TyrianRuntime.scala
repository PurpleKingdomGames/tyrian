package tyrian.runtime

import cats.effect.kernel.Async
import cats.effect.kernel.Ref
import cats.effect.std.AtomicCell
import cats.effect.std.Dispatcher
import cats.effect.std.Queue
import cats.effect.syntax.all.*
import cats.syntax.all.*
import org.scalajs.dom
import org.scalajs.dom.Element
import snabbdom.VNode
import snabbdom.toVNode
import tyrian.Cmd
import tyrian.Html
import tyrian.Location
import tyrian.Sub

import scala.annotation.nowarn

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
      val loop        = mainLoop(dispatcher, router, node, initModel, initCmd, update, view, subscriptions)
      val model       = F.ref(initModel)
      val currentSubs = AtomicCell[F].of(List.empty[(String, F[Unit])])
      val msgQueue    = Queue.unbounded[F, Msg]

      (model, currentSubs, msgQueue).flatMapN(loop)
    }

  def mainLoop[F[_], Model, Msg](
      dispatcher: Dispatcher[F],
      router: Location => Msg,
      node: Element,
      initModel: Model,
      initCmd: Cmd[F, Msg],
      update: Model => Msg => (Model, Cmd[F, Msg]),
      view: Model => Html[Msg],
      subscriptions: Model => Sub[F, Msg]
  )(
      model: Ref[F, Model],
      currentSubs: AtomicCell[F, List[(String, F[Unit])]],
      msgQueue: Queue[F, Msg]
  )(using F: Async[F]): F[Nothing] =
    val runCmd: Cmd[F, Msg] => F[Unit] = runCommands(msgQueue)
    val runSub: Sub[F, Msg] => F[Unit] = runSubscriptions(currentSubs, msgQueue, dispatcher)
    val onMsg: Msg => Unit             = postMsg(dispatcher, msgQueue)

    val renderLoop: F[Nothing] =
      def redraw(vnode: VNode): F[VNode] =
        model.get.flatMap { m =>
          F.delay(Rendering.render(vnode, m, view, onMsg, router))
        }

      def loop(vnode: VNode): F[Nothing] =
        model.get.flatMap { m =>
          requestAnimationFrame *> redraw(vnode).flatMap(loop)
        }

      F.delay(toVNode(node)).flatMap(loop)

    val msgLoop: F[Nothing] =
      msgQueue.take.flatMap { msg =>
        model
          .modify { oldModel =>
            val (newModel, cmd) = update(oldModel)(msg)
            val sub             = subscriptions(newModel)

            (newModel, (cmd, sub))
          }
          .flatMap { (cmd, sub) =>
            runCmd(cmd) *> runSub(sub)
          }
          .void
      }.foreverM

    renderLoop.background.surround {
      msgLoop.background.surround {
        runCmd(initCmd) *> F.never
      }
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

  // Triggers another render tick, but otherwise does nothing.
  @nowarn("msg=discarded")
  def requestAnimationFrame[F[_]](using F: Async[F]): F[Unit] =
    F.async_ { cb =>
      dom.window.requestAnimationFrame(_ => cb(Either.unit))
      ()
    }

  // Send messages to the queue.
  def postMsg[F[_], Msg](dispatcher: Dispatcher[F], msgQueue: Queue[F, Msg]): Msg => Unit =
    msg => dispatcher.unsafeRunAndForget(msgQueue.offer(msg))
