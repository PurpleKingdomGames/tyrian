package util

import scala.scalajs.js
import scala.scalajs.js.Any

object Functions {

  @inline def fun0[A](f: () => A): js.Function0[A] = Any.fromFunction0(f)

  @inline def fun[A, B](f: A => B): js.Function1[A, B] = Any.fromFunction1(f)

  @inline def fun2[A, B, C](f: (A, B) => C): js.Function2[A, B, C] = Any.fromFunction2(f)

}
