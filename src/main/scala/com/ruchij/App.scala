package com.ruchij

import cats.effect.{Async, Blocker, Clock, ContextShift, ExitCode, IO, IOApp, Resource, Sync}
import cats.implicits._
import com.ruchij.config.ApplicationConfiguration
import com.ruchij.daos.doobie.Transactor
import com.ruchij.daos.person.DoobieEncryptedPersonDao
import com.ruchij.migration.MigrationApp
import com.ruchij.services.encryption.AesEncryptionService
import com.ruchij.services.person.{PersonService, PersonServiceImpl}
import com.ruchij.types.Random
import doobie.ConnectionIO
import fs2.Stream
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object App extends IOApp {
  case class PersonEntry(firstName: String, lastName: String, email: String)

  override def run(args: List[String]): IO[ExitCode] =
    Slf4jLogger.create[IO].flatMap { implicit logger =>
      for {
        configObjectSource <- IO.delay(ConfigSource.defaultApplication)
        applicationConfiguration <- ApplicationConfiguration.load[IO](configObjectSource)

        _ <- personService[IO](applicationConfiguration).use { service =>
          execute[IO](service, applicationConfiguration.data.insertionCount)
        }

        _ <- Logger[IO].info("Application execution completed")
      } yield ExitCode.Success
    }

  def execute[F[_]: Sync: Logger](personService: PersonService[F], insertionCount: Int): F[Unit] =
    Stream
      .eval(Random[F, PersonEntry].generate)
      .repeat
      .evalMap { personEntry =>
        personService
          .create(personEntry.firstName, personEntry.lastName, personEntry.email)
          .flatMap { person =>
            personService
              .findByEmail(person.email)
              .flatMap {
                case Some(retrievePerson) => Logger[F].info(retrievePerson.toString)

                case _ => Logger[F].error(s"Unable to find persisted person: $person")
              }
          }
      }
      .take(insertionCount)
      .compile
      .drain
      .productR {
        Logger[F].info("---------------------- Listing all the saved entries ----------------------")
      }
      .productR {
        personService.retrieveAll
          .evalMap { person =>
            Logger[F].info(person.toString)
          }
          .compile
          .drain
      }

  def personService[F[_]: Async: ContextShift: Clock](
    applicationConfiguration: ApplicationConfiguration
  ): Resource[F, PersonService[F]] =
    Transactor
      .create(applicationConfiguration.database)
      .map(_.trans)
      .flatMap { implicit transaction =>
        for {
          _ <- Resource.eval(MigrationApp.migrate(applicationConfiguration.database))

          cpuCount <- Resource.eval(Sync[F].delay(Runtime.getRuntime.availableProcessors()))
          cpuThreadPool <- Resource.eval(Sync[F].delay(Executors.newFixedThreadPool(cpuCount)))
          cpuBlocker = Blocker.liftExecutionContext(ExecutionContext.fromExecutor(cpuThreadPool))

          encryptionService = new AesEncryptionService[F](
            applicationConfiguration.encryption.secretKey,
            applicationConfiguration.encryption.defaultIV,
            cpuBlocker
          )
        } yield new PersonServiceImpl[F, ConnectionIO](encryptionService, DoobieEncryptedPersonDao)
      }
}
