package tyrian.runtime

import cats.effect.kernel.Async
import cats.effect.kernel.Clock
import cats.effect.kernel.Ref
import cats.effect.std.Dispatcher
import cats.syntax.all.*
import org.scalajs.dom
import snabbdom.VNode
import tyrian.Html
import tyrian.Location

final case class Renderer(vnode: VNode, state: RendererState):

  def runningAt(t: Long): Renderer =
    this.copy(state = RendererState.Running(t))

object Renderer:

  def init[F[_]](vnode: VNode)(using F: Async[F]): F[Ref[F, Renderer]] =
    F.ref(
      Renderer(vnode, RendererState.Idle)
    )

  private val timeout: Long = 1000

  // This function gets called on every model update
  def redraw[F[_], Model, Msg](
      dispatcher: Dispatcher[F],
      renderer: Ref[F, Renderer],
      model: Ref[F, Model],
      view: Model => Html[Msg],
      onMsg: Msg => Unit,
      router: Location => Msg
  )(using F: Async[F], clock: Clock[F]): F[Unit] =
    clock.realTime.flatMap { time =>
      renderer.modify { r =>
        r.state match
          case RendererState.Idle =>
            // If the render state is idle, update the last triggered time and begin.
            r.runningAt(time.toMillis) ->
              F.delay(
                dom.window.requestAnimationFrame(_ =>
                  render(dispatcher, renderer, model, view, onMsg, router)(time.toMillis)
                )
              ).void

          case RendererState.Running(_) =>
            // If the render state is running, just update the triggered time.
            r.runningAt(time.toMillis) -> F.unit
      }
    }.flatten

  private def render[F[_], Model, Msg](
      dispatcher: Dispatcher[F],
      renderer: Ref[F, Renderer],
      model: Ref[F, Model],
      view: Model => Html[Msg],
      onMsg: Msg => Unit,
      router: Location => Msg
  )(t: Long)(using F: Async[F], clock: Clock[F]): Unit =
    dispatcher.unsafeRunAndForget {
      for {
        time <- clock.realTime.map(_.toMillis)
        m    <- model.get

        res <- renderer.modify { r =>
          r.state match
            case RendererState.Idle =>
              // Something has gone wrong, do nothing.
              r -> F.unit

            case RendererState.Running(lastTriggered) =>
              // If nothing has happened, set to idle and do not loop
              if t - lastTriggered >= timeout then r.copy(state = RendererState.Idle) -> F.unit
              else
                // Otherwise, re-render and set the state appropriately
                r.copy(
                  vnode = Rendering.render(r.vnode, m, view, onMsg, router)
                ) ->
                  F.delay(
                    // Loop
                    dom.window.requestAnimationFrame(_ =>
                      render(dispatcher, renderer, model, view, onMsg, router)(time)
                    )
                  ).void
        }.flatten
      } yield res
    }
