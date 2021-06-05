package com.ruchij.types

import cats.arrow.FunctionK
import cats.{Applicative, ApplicativeError, ~>}
import pureconfig.ConfigReader
import pureconfig.error.{ConfigReaderFailures, ThrowableFailure}

import scala.language.implicitConversions
import scala.util.Try

object FunctionKTypes {
  implicit class FunctionKOps[F[_], A](value: F[A]) {
    def toG[G[_]](implicit functionK: F ~> G): G[A] = functionK(value)
  }

  implicit def optionToF[E, F[_]: ApplicativeError[*[_], E]](onEmpty: => E): Option ~> F =
    new FunctionK[Option, F] {
      override def apply[A](option: Option[A]): F[A] =
        option.fold[F[A]](ApplicativeError[F, E].raiseError(onEmpty)) { value =>
          Applicative[F].pure(value)
        }
    }

  implicit val tryToConfigReader: Try ~> ConfigReader =
    new FunctionK[Try, ConfigReader] {
      override def apply[A](tryValue: Try[A]): ConfigReader[A] =
        tryValue.fold[ConfigReader[A]](
          throwable =>
            ConfigReader.fromCursor { _ =>
              Left(ConfigReaderFailures(ThrowableFailure(throwable, None)))
          },
          value =>
            ConfigReader.fromCursor { _ =>
              Right(value)
          }
        )
    }

}
