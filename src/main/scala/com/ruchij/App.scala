package com.ruchij

import cats.effect.{ExitCode, IO, IOApp, Sync}
import cats.implicits._
import com.ruchij.config.DatabaseConfiguration
import com.ruchij.migration.MigrationApp
import pureconfig.ConfigSource

object App extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    for {
      configObjectSource <- IO.delay(ConfigSource.defaultApplication)
      databaseConfiguration <- DatabaseConfiguration.load[IO](configObjectSource)
    }
    yield ExitCode.Success

  def run[F[_]: Sync](databaseConfiguration: DatabaseConfiguration) =
    for {
      _ <- MigrationApp.migrate(databaseConfiguration)
    }
    yield ???
}
