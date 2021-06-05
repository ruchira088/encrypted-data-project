package com.ruchij.types

import cats.arrow.FunctionK
import cats.{Applicative, ApplicativeError, ~>}

import scala.language.implicitConversions

object FunctionKTypes {
  implicit class FunctionKOps[F[_], A](value: F[A]) {
    def toG[G[_]](implicit functionK: F ~> G): G[A] = functionK(value)
  }

  implicit def optionToF[E, F[_]: ApplicativeError[*[_], E]](onEmpty: => E): Option ~> F =
    new FunctionK[Option, F] {
      override def apply[A](option: Option[A]): F[A] =
        option.fold[F[A]](ApplicativeError[F, E].raiseError(onEmpty)) {
          value => Applicative[F].pure(value)
        }
    }

}
