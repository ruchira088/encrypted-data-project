package com.ruchij.services.person

import cats.effect.{Clock, Sync}
import cats.implicits._
import cats.{Applicative, MonadError, ~>}
import com.ruchij.daos.doobie.models.EncryptedField.InitializationVector.{DefaultIV, RandomIV}
import com.ruchij.daos.person.EncryptedPersonDao
import com.ruchij.daos.person.models.EncryptedPerson
import com.ruchij.services.encryption.EncryptionService
import com.ruchij.services.person.model.Person
import com.ruchij.types.FunctionKTypes._
import com.ruchij.types.Random
import fs2.Stream
import org.joda.time.DateTime

import java.util.UUID
import java.util.concurrent.TimeUnit

class PersonServiceImpl[F[_]: Clock: Random[*[_], UUID]: MonadError[*[_], Throwable], G[_]: Sync](
  encryptionService: EncryptionService[F],
  encryptedPersonDao: EncryptedPersonDao[G]
)(implicit transaction: G ~> F)
    extends PersonService[F] {

  override def create(firstName: String, lastName: String, email: String): F[Person] =
    for {
      id <- Random[F, UUID].generate
      timestamp <- Clock[F].realTime(TimeUnit.MILLISECONDS).map(milliseconds => new DateTime(milliseconds))

      firstNameEncrypted <- encryptionService.encrypt(firstName, RandomIV)
      lastNameEncrypted <- encryptionService.encrypt(lastName, RandomIV)

      emailEncrypted <- encryptionService.encrypt(email, DefaultIV)
      usernameEncrypted <- encryptionService.encrypt(s"$firstName.$lastName".toLowerCase, DefaultIV)

      encryptedPerson = EncryptedPerson(
        id,
        timestamp,
        timestamp,
        usernameEncrypted,
        firstNameEncrypted,
        lastNameEncrypted,
        emailEncrypted
      )

      maybeEncryptedPerson <- transaction {
        encryptedPersonDao
          .insert(encryptedPerson)
          .productR {
            encryptedPersonDao.findById(id)
          }
      }

      encryptedPerson <- maybeEncryptedPerson.toG[F](
        optionToF[Throwable, F](new InternalError("Unable to persist person"))
      )

      person <- decrypt(encryptedPerson)

    } yield person

  override def findByEmail(email: String): F[Option[Person]] =
    for {
      encryptedEmail <- encryptionService.encrypt(email, DefaultIV)
      maybeEncryptedPerson <- transaction(encryptedPersonDao.findByEmail(encryptedEmail))

      maybePerson <- maybeEncryptedPerson.fold[F[Option[Person]]](Applicative[F].pure(None)) { encryptedPerson =>
        decrypt(encryptedPerson).map(Some.apply)
      }
    } yield maybePerson

  override val retrieveAll: Stream[F, Person] = retrieveAll(0, 25)

  private def retrieveAll(offset: Int, pageSize: Int): Stream[F, Person] =
    Stream
      .eval(transaction(encryptedPersonDao.retrieveAll(offset, pageSize)))
      .flatMap {
        encryptedPersons =>
          Stream.emits[F, EncryptedPerson](encryptedPersons).evalMap(decrypt) ++
            (if (encryptedPersons.size < pageSize) Stream.empty else retrieveAll(offset + pageSize, pageSize))
      }

  private def decrypt(encryptedPerson: EncryptedPerson): F[Person] =
    for {
      firstName <- encryptionService.decrypt(encryptedPerson.firstName)
      lastName <- encryptionService.decrypt(encryptedPerson.lastName)
      username <- encryptionService.decrypt(encryptedPerson.username)
      email <- encryptionService.decrypt(encryptedPerson.email)

      person = Person(
        encryptedPerson.id,
        encryptedPerson.createdAt,
        encryptedPerson.modifiedAt,
        username,
        firstName,
        lastName,
        email
      )
    } yield person
}
