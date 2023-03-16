package tyrian.runtime

import cats.effect.kernel.Async
import cats.effect.kernel.Ref
import cats.effect.std.Dispatcher
import cats.syntax.all.*
import fs2.Stream
import fs2.concurrent.Channel
import org.scalajs.dom
import org.scalajs.dom.Element
import snabbdom.VNode
import tyrian.Attr
import tyrian.Attribute
import tyrian.Cmd
import tyrian.Event
import tyrian.Html
import tyrian.Sub

final class TyrianRuntime[F[_]: Async, Model, Msg](
    initCmd: Cmd[F, Msg],
    update: Model => Msg => (Model, Cmd[F, Msg]),
    view: Model => Html[Msg],
    subscriptions: Model => Sub[F, Msg],
    model: Ref[F, ModelHolder[Model]],
    vnode: Ref[F, Element | VNode],
    channel: => Channel[F, F[Unit]],
    dispatcher: => Dispatcher[F]
):

  // The currently live subs.
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var currentSubscriptions: List[(String, F[Unit])] = Nil
  // This is a queue of new subs waiting to be run for the first time.
  // In the event that two events happen at once, you can't assume that
  // you would have run all the subs between events.
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var aboutToRunSubscriptions: Set[String] = Set.empty

  // Update

  private def onMsg(msg: Msg): Unit =
    val res: F[Unit] =
      for {
        currentModel <- model.get
        (updatedState, cmd) = update(currentModel.model)(msg)
        _ <- completeUpdate(cmd, updatedState)
      } yield ()

    dispatcher.unsafeRunAndForget(res)

  private def completeUpdate(cmd: Cmd[F, Msg], updatedState: Model): F[Unit] =
    for {
      _           <- model.set(ModelHolder(updatedState, true))
      sideEffects <- gatherSideEffects(cmd, subscriptions(updatedState))
      _           <- Stream.emits(sideEffects).foreach(msg => channel.send(msg).void).compile.drain
    } yield ()

  private def gatherSideEffects(
      cmd: Cmd[F, Msg],
      sub: Sub[F, Msg]
  ): F[List[F[Unit]]] =
    Async[F].delay {
      // Cmds
      val cmdsToRun = CmdHelper.cmdToTaskList(cmd, dispatcher)

      // Subs
      val allSubs                 = SubHelper.flatten(sub)
      val (stillAlive, discarded) = SubHelper.aliveAndDead(allSubs, currentSubscriptions)
      val newSubs                 = SubHelper.findNewSubs(allSubs, stillAlive.map(_._1), aboutToRunSubscriptions.toList)

      // Update the first run queue
      aboutToRunSubscriptions = aboutToRunSubscriptions ++ newSubs.map(_.id)
      // Update the current subs
      currentSubscriptions = stillAlive

      val subsToRun =
        SubHelper
          .toRun(newSubs, onMsg)
          .map { s =>
            Async[F].map(s) {
              case Some(sub) =>
                // Remove from the queue
                aboutToRunSubscriptions = aboutToRunSubscriptions - sub.id
                // Add to the current subs
                currentSubscriptions = (sub.id -> sub.cancel) :: currentSubscriptions

                Option.empty[Msg]

              case None =>
                Option.empty[Msg]
            }
          }

      val subsToDiscard =
        discarded.map { d =>
          Async[F].map(d)(_ => Option.empty[Msg])
        }

      // Run them all
      (cmdsToRun ++ subsToRun ++ subsToDiscard).map { task =>
        Async[F].map(task) {
          case Some(msg) => onMsg(msg)
          case None      => ()
        }
      }
    }

  // Render

  private def renderLoop(): Unit =
    def redraw: F[Unit] =
      Async[F].flatMap(model.get) { m =>
        if m.updated then
          for {
            _ <- vnode.updateAndGet(n => Rendering.render(n, m.model, view, onMsg))
            _ <- model.set(ModelHolder(m.model, false))
          } yield ()
        else Async[F].unit
      }

    dom.window.requestAnimationFrame { _ =>
      dispatcher.unsafeRunAndForget(redraw)
      renderLoop()
    }

  // Start up

  def start(): Unit =
    renderLoop()
    dispatcher.unsafeRunAndForget(
      model.get.flatMap { m =>
        completeUpdate(initCmd, m.model)
      }
    )
