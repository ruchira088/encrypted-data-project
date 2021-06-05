package com.ruchij.daos.doobie

import cats.effect.{Async, Blocker, ContextShift, Resource}
import com.ruchij.config.DatabaseConfiguration
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

import scala.concurrent.ExecutionContext

object Transactor {

  def create[F[_]: Async: ContextShift](
    databaseConfiguration: DatabaseConfiguration
  ): Resource[F, HikariTransactor[F]] =
    for {
      connectEC <- ExecutionContexts.fixedThreadPool(6)
      blocker <- Blocker[F]
      transactor <- create(databaseConfiguration, connectEC, blocker)
    } yield transactor

  def create[F[_]: Async: ContextShift](
    databaseConfiguration: DatabaseConfiguration,
    connectEC: ExecutionContext,
    blocker: Blocker
  ): Resource[F, HikariTransactor[F]] =
    Resource
      .eval(DatabaseDriver.parse[F](databaseConfiguration.url))
      .flatMap { databaseDriver =>
        HikariTransactor.newHikariTransactor(
          databaseDriver.classTag.runtimeClass.getCanonicalName,
          databaseConfiguration.url,
          databaseConfiguration.user,
          databaseConfiguration.password,
          connectEC,
          blocker
        )
      }

}
