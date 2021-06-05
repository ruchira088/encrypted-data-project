package com.ruchij.config

import cats.{Applicative, ApplicativeError}
import pureconfig.ConfigObjectSource
import pureconfig.error.ConfigReaderException
import pureconfig.generic.auto._

case class DatabaseConfiguration(url: String, user: String, password: String)

object DatabaseConfiguration {
  def load[F[_]: ApplicativeError[*[_], Throwable]](configObjectSource: ConfigObjectSource): F[DatabaseConfiguration] =
    configObjectSource.at("database")
      .load[DatabaseConfiguration]
      .fold(
        configFailure => ApplicativeError[F, Throwable].raiseError(ConfigReaderException[DatabaseConfiguration](configFailure)),
        configuration => Applicative[F].pure(configuration)
      )
}
