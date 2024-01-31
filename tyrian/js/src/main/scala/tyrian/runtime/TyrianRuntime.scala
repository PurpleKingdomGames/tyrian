package tyrian.runtime

import cats.effect.kernel.Async
import cats.effect.std.AtomicCell
import cats.effect.std.Dispatcher
import cats.effect.std.Queue
import cats.effect.std.Semaphore
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
  )(using F: Async[F]): F[Nothing] = Dispatcher.sequential[F].use { dispatcher =>
    (
      F.ref(initModel),                                // model
      Semaphore[F](0),                                 // modelUpdateCounter
      AtomicCell[F].of(List.empty[(String, F[Unit])]), // currentSubs
      Queue.unbounded[F, Msg]                          // msgQueue
    )
      .flatMapN { (model, modelUpdateCounter, currentSubs, msgQueue) =>

        val incrementModelUpdateCount = modelUpdateCounter.release
        val resetModelUpdateCount     = modelUpdateCounter.available.flatMap(modelUpdateCounter.releaseN(_))
        val awaitModelUpdate          = modelUpdateCounter.acquire

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
            .flatModify { oldModel =>
              val (newModel, cmd) = update(oldModel)(msg)
              val sub             = subscriptions(newModel)
              (newModel, incrementModelUpdateCount *> runCmd(cmd) *> runSub(sub))
            }
        }.foreverM
        // end msgLoop

        val renderLoop =
          val onMsg = (msg: Msg) => dispatcher.unsafeRunAndForget(msgQueue.offer(msg))

          @nowarn("msg=discarded")
          val requestAnimationFrame = F.async_ { cb =>
            dom.window.requestAnimationFrame(_ => cb(Either.unit))
            ()
          }

          def redraw(vnode: VNode) =
            model.get.flatMap(m => F.delay(Rendering.render(vnode, m, view, onMsg, router)))

          def loop(vnode: VNode): F[Nothing] =
            requestAnimationFrame *>
              resetModelUpdateCount *>
              redraw(vnode).flatMap(awaitModelUpdate *> loop(_))

          F.delay(toVNode(node)).flatMap(loop)
        // end renderLoop

        renderLoop.background.surround {
          msgLoop.background.surround {
            runCmd(initCmd) *> F.never
          }
        }
      }

  }
