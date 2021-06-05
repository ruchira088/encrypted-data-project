package com.ruchij.daos.doobie

import cats.ApplicativeError
import com.ruchij.types.FunctionKTypes._
import org.h2.{Driver => H2Driver}
import org.postgresql.{Driver => PostgreSqlDriver}

import java.sql.Driver
import scala.reflect.ClassTag
import scala.util.matching.Regex

abstract class DatabaseDriver[A <: Driver](implicit val classTag: ClassTag[A])

object DatabaseDriver {
  case object H2 extends DatabaseDriver[H2Driver]
  case object PostgreSQL extends DatabaseDriver[PostgreSqlDriver]

  val values: Set[DatabaseDriver[_]] = Set(H2, PostgreSQL)

  val DatabaseType: Regex = "jdbc:([^:]+):.*".r

  def unapply(databaseDriver: DatabaseDriver[_]): Option[String] =
    databaseDriver.classTag.runtimeClass.getCanonicalName match {
      case s"org.$dbName.Driver" => Some(dbName)
      case _ => None
    }

  def parse[F[_]: ApplicativeError[*[_], Throwable]](url: String): F[DatabaseDriver[_]] = {
    url match {
      case DatabaseType(dbType) =>
        values
          .find {
            case DatabaseDriver(dbName) => dbName == dbType
            case _ => false
          }
          .toG[F] {
            optionToF[Throwable, F] {
              new IllegalArgumentException(s"Unable to find Database driver for $dbType")
            }
          }

      case _ =>
        ApplicativeError[F, Throwable].raiseError {
          new IllegalArgumentException(s"Unable to extract Database type for URL: $url")
        }
    }
  }

}
