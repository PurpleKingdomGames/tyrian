package tyrian.runtime

import cats.effect.kernel.Async
import cats.effect.kernel.Ref
import cats.effect.std.Dispatcher
import cats.effect.std.Queue
import cats.syntax.all.*
import fs2.Stream
import org.scalajs.dom
import org.scalajs.dom.Element
import snabbdom._
import snabbdom.modules._
import tyrian.Attr
import tyrian.Attribute
import tyrian.Cmd
import tyrian.Event
import tyrian.Html
import tyrian.NamedAttribute
import tyrian.Property
import tyrian.Sub
import tyrian.Tag
import tyrian.Text
import util.Functions.fun

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => obj}

final class TyrianRuntime[F[_]: Async, Model, Msg](
    init: (Model, Cmd[F, Msg]),
    update: Model => Msg => (Model, Cmd[F, Msg]),
    view: Model => Html[Msg],
    subscriptions: Model => Sub[F, Msg],
    node: Element,
    model: Ref[F, ModelHolder[Model]],
    vnode: Ref[F, Option[VNode]],
    queue: => Queue[F, F[Unit]],
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

  // Initialisation

  private def initialise(cmd: Cmd[F, Msg]): Unit =
    val res: F[Unit] =
      for {
        currentModel <- model.get
        _            <- Async[F].delay(renderLoop(0)) // Do first render before first cmds are run
        _            <- completeUpdate(cmd, currentModel.model)
      } yield ()

    dispatcher.unsafeRunAndForget(res)

  // Update

  private def onMsg(msg: Msg): Unit =
    val res: F[Unit] =
      for {
        currentModel <- model.get
        (updatedState, cmd) = update(currentModel.model)(msg)
        complete <- completeUpdate(cmd, updatedState)
      } yield complete

    dispatcher.unsafeRunAndForget(res)

  private def completeUpdate(cmd: Cmd[F, Msg], updatedState: Model): F[Unit] =
    val results: F[Stream[F, F[Unit]]] =
      for {
        _           <- model.set(ModelHolder(updatedState, true))
        sideEffects <- gatherSideEffects(cmd, subscriptions(updatedState))
      } yield Stream.emits(sideEffects)

    Async[F].flatMap(results) { (stream: Stream[F, F[Unit]]) =>
      stream.foreach(queue.offer).compile.drain
    }

  private given CanEqual[Option[_], Option[_]] = CanEqual.derived

  private def gatherSideEffects(
      cmd: Cmd[F, Msg],
      sub: Sub[F, Msg]
  ): F[List[F[Unit]]] =
    Async[F].delay {
      // Cmds
      val cmdsToRun = CmdHelper.cmdToTaskList(cmd)

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

  private def toVNode(html: Html[Msg]): VNode =
    html match
      case Tag(name, attrs, children) =>
        val as: List[(String, String)] =
          attrs.collect {
            case Attribute(n, v)   => (n, v)
            case NamedAttribute(n) => (n, "")
          }

        val props: List[(String, PropValue)] =
          attrs.collect { case Property(n, v) => (n, v) }

        val events: List[(String, EventHandler)] =
          attrs.collect { case Event(n, msg) =>
            (n, EventHandler((e: dom.Event) => onMsg(msg.asInstanceOf[dom.Event => Msg](e))))
          }

        val data: VNodeData =
          VNodeData.empty.copy(
            props = props.toMap,
            attrs = as.toMap,
            on = events.toMap
          )

        val childrenElem: Array[VNode] =
          children.toArray.map {
            case t: Text            => VNode.text(t.value)
            case subHtml: Html[Msg] => toVNode(subHtml)
          }

        h(name, data, childrenElem)

  private lazy val patch: Patch =
    snabbdom.init(
      Seq(
        Attributes.module,
        Classes.module,
        Props.module,
        Styles.module,
        EventListeners.module,
        Dataset.module
      )
    )

  private def render(oldNode: Element | VNode, model: Model): VNode =
    oldNode match
      case em: Element => patch(em, toVNode(view(model)))
      case vn: VNode   => patch(vn, toVNode(view(model)))

  private def renderLoop(time: Double): Unit =
    def res: F[Unit] =
      Async[F].flatMap(model.get) { m =>
        if m.updated then
          for {
            n <- vnode.get
            _ <- vnode.set(n match {
              case Some(existingNode) => Some(render(existingNode, m.model))
              case None               => Some(render(node, m.model))
            })
            _ <- model.set(ModelHolder(m.model, false))
          } yield ()
        else Async[F].unit
      }

    dom.window.requestAnimationFrame { t =>
      dispatcher.unsafeRunAndForget(res)
      renderLoop(t)
    }

  // Start up

  def start(): Unit =
    dispatcher.unsafeRunAndForget(
      model.get.map { currentState =>
        initialise(init._2)
      }
    )

final class ModelHolder[Model](val model: Model, val updated: Boolean)
