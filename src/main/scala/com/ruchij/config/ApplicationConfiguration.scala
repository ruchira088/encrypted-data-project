package com.ruchij.config

import cats.{Applicative, ApplicativeError}
import com.ruchij.config.PureConfigReaders._
import pureconfig.ConfigObjectSource
import pureconfig.error.ConfigReaderException
import pureconfig.generic.auto._

case class ApplicationConfiguration(
  database: DatabaseConfiguration,
  encryption: EncryptionConfiguration,
  data: DataConfiguration
)

object ApplicationConfiguration {
  def load[F[_]: ApplicativeError[*[_], Throwable]](
    configObjectSource: ConfigObjectSource
  ): F[ApplicationConfiguration] =
    configObjectSource
      .load[ApplicationConfiguration]
      .fold(
        configFailure => ApplicativeError[F, Throwable].raiseError(ConfigReaderException(configFailure)),
        applicationConfiguration => Applicative[F].pure(applicationConfiguration)
      )
}
