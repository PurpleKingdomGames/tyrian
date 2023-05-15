package tyrian.runtime

import cats.data.OptionT
import cats.effect.kernel.Async
import cats.effect.kernel.Ref
import cats.effect.kernel.Resource
import cats.effect.std.AtomicCell
import cats.effect.std.Dispatcher
import cats.effect.std.Queue
import cats.effect.syntax.all.*
import cats.syntax.all.*
import fs2.Stream
import fs2.concurrent.Channel
import org.scalajs.dom
import org.scalajs.dom.Element
import snabbdom.VNode
import snabbdom.toVNode
import tyrian.Cmd
import tyrian.Html
import tyrian.Sub

object TyrianRuntime:

  def apply[F[_], Model, Msg](
      node: Element,
      initModel: Model,
      initCmd: Cmd[F, Msg],
      update: Model => Msg => (Model, Cmd[F, Msg]),
      view: Model => Html[Msg],
      subscriptions: Model => Sub[F, Msg]
  )(using F: Async[F]): F[Nothing] = Dispatcher.sequential[F].use { dispatcher =>
    (F.ref(ModelHolder(initModel, true)), AtomicCell[F].of(List.empty[(String, F[Unit])]), Queue.unbounded[F, Msg])
      .flatMapN { (model, currentSubs, msgQueue) =>

        def runCmd(cmd: Cmd[F, Msg]): F[Unit] =
          CmdHelper.cmdToTaskList(cmd).foldMapM { task =>
            task.handleError(_ => None).flatMap(_.traverse_(msgQueue.offer(_))).start.void
          }

        def runSub(sub: Sub[F, Msg]): F[Unit] =
          currentSubs.evalUpdate { oldSubs =>
            val allSubs                 = SubHelper.flatten(sub)
            val (stillAlive, discarded) = SubHelper.aliveAndDead(allSubs, oldSubs)

            val newSubs = SubHelper
              .findNewSubs(allSubs, stillAlive.map(_._1), Nil)
              .traverse(SubHelper.runObserve(_) { result =>
                dispatcher.unsafeRunAndForget(
                  result.toOption.flatten.foldMapM(msgQueue.offer(_).void)
                )
              })

            discarded.foldMapM(_.start.void) *> newSubs.map(_ ++ stillAlive)
          }
        // end runSub

        val msgLoop = msgQueue.take.flatMap { msg =>
          model
            .modify { case ModelHolder(oldModel, _) =>
              val (newModel, cmd) = update(oldModel)(msg)
              val sub             = subscriptions(newModel)
              (ModelHolder(newModel, true), (cmd, sub))
            }
            .flatMap { (cmd, sub) =>
              runCmd(cmd) *> runSub(sub)
            }
            .void
        }.foreverM
        // end msgLoop

        val renderLoop =
          val onMsg = (msg: Msg) => dispatcher.unsafeRunAndForget(msgQueue.offer(msg))

          val requestAnimationFrame = F.async_ { cb =>
            dom.window.requestAnimationFrame(_ => cb(Either.unit))
          }

          def redraw(vnode: VNode) =
            model.getAndUpdate(m => ModelHolder(m.model, false)).flatMap { m =>
              if m.updated then F.delay(Rendering.render(vnode, m.model, view, onMsg))
              else F.pure(vnode)
            }

          def loop(vnode: VNode): F[Nothing] =
            requestAnimationFrame *> redraw(vnode).flatMap(loop(_))

          F.delay(toVNode(node)).flatMap(loop)
        // end renderLoop

        renderLoop.background.surround {
          msgLoop.background.surround {
            runCmd(initCmd) *> F.never
          }
        }
      }

  }
