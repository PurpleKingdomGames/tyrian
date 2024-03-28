package tyrian.runtime

import cats.effect.kernel.Async
import cats.effect.kernel.Ref
import cats.effect.kernel.Resource
import snabbdom.VNode
import tyrian.Html
import tyrian.Location

final case class Renderer(vnode: VNode, state: RendererState, lastTriggered: Long):
  def redraw[Model, Msg](
      time: Long,
      model: Model,
      view: Model => Html[Msg],
      onMsg: Msg => Unit,
      router: Location => Msg
  ): Renderer =
    this.copy(
      vnode = Rendering.render(vnode, model, view, onMsg, router),
      state = RendererState.Running,
      lastTriggered = time
    )

object Renderer:

  def init[F[_]](vnode: VNode)(using F: Async[F]): Resource[F, F[Ref[F, Renderer]]] =
    Resource.eval(
      F.delay(
        F.ref(
          Renderer(vnode, RendererState.Idle, 0)
        )
      )
    )
