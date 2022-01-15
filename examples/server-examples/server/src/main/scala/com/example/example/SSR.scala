package com.example.example

import cats.Applicative
import cats.implicits.*
import org.http4s.EntityEncoder
import tyrian.*
import tyrian.Html.*

trait SSR[F[_]]:
  def render(n: SSR.Input): F[SSR.Output]
  def render: F[SSR.Output]

object SSR:
  implicit def apply[F[_]](using ev: SSR[F]): SSR[F] = ev

  val styles  = style(Style("font-family" -> "Arial, Helvetica, sans-serif"))
  val topLine = p(b(text("HTML fragment rendered by Tyrian on the server.")))

  def impl[F[_]: Applicative]: SSR[F] = new SSR[F]:
    def render(in: SSR.Input): F[SSR.Output] =
      val html = Tyrian.render(
        div(styles)(
          topLine,
          p(text("Was sent the following: " + in.toString))
        )
      )
      (SSR.Output(html)).pure[F]

    def render: F[SSR.Output] =
      val html = Tyrian.render(div(styles)(topLine))
      (SSR.Output(html)).pure[F]

  opaque type Input = String
  object Input:
    inline def apply(s: String): Input        = s
    extension (n: Input) def toString: String = n

  opaque type Output = String
  object Output:
    inline def apply(s: String): Output      = s
    extension (o: Output) def toHtml: String = o
