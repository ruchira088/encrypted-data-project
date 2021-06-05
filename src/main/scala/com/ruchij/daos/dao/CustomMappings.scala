package com.ruchij.daos.dao

import com.ruchij.daos.dao.models.EncryptedField
import com.ruchij.daos.dao.models.EncryptedField.InitializationVector
import doobie.implicits.javasql.TimestampMeta
import doobie.util.{Get, Put}
import org.joda.time.DateTime

import java.sql.Timestamp
import java.util.UUID
import scala.util.Try

object CustomMappings {
  implicit val dateTimeGet: Get[DateTime] = Get[Timestamp].tmap(timestamp => new DateTime(timestamp.getTime))
  implicit val dateTimePut: Put[DateTime] = Put[Timestamp].tcontramap[DateTime](dateTime => new Timestamp(dateTime.getMillis))

  implicit val uuidGet: Get[UUID] =
    Get[String].temap { uuidString => Try(UUID.fromString(uuidString)).toEither.left.map(_.getMessage) }

  implicit val uuidPut: Put[UUID] = Put[String].tcontramap[UUID](_.toString)

  implicit def encryptedFieldGet[A, IV <: InitializationVector]: Get[EncryptedField[A, IV]] =
    Get[Array[Byte]].map[EncryptedField[A, IV]](bytes => EncryptedField(bytes))

  implicit def encryptedFieldPut[A, IV <: InitializationVector]: Put[EncryptedField[A, IV]] =
    Put[Array[Byte]].contramap[EncryptedField[A, IV]](_.data)
}
