package com.ruchij.services.person

import cats.{Monad, MonadError, ~>}
import cats.effect.Clock
import cats.implicits._
import com.ruchij.daos.models.EncryptedField.InitializationVector.{DefaultIV, RandomIV}
import com.ruchij.daos.person.EncryptedPersonDao
import com.ruchij.daos.person.models.EncryptedPerson
import com.ruchij.services.encryption.EncryptionService
import com.ruchij.services.encryption.models.{ByteDecoder, ByteEncoder}
import com.ruchij.services.person.model.Person
import com.ruchij.types.FunctionKTypes._
import com.ruchij.types.Random
import org.joda.time.DateTime

import java.util.UUID
import java.util.concurrent.TimeUnit

class PersonServiceImpl[F[+ _]: Clock: Random[*[_], UUID]: MonadError[*[_], Throwable]: ByteEncoder[*[_], String]: ByteDecoder[*[_], String], G[_]: Monad](
  encryptionService: EncryptionService[F],
  encryptedPersonDao: EncryptedPersonDao[G]
)(implicit transaction: G ~> F) extends PersonService[F] {

  override def create(firstName: String, lastName: String, email: String): F[Person] =
    for {
      id <- Random[F, UUID].generate
      timestamp <- Clock[F].realTime(TimeUnit.MILLISECONDS).map(milliseconds => new DateTime(milliseconds))

      firstNameEncrypted <- encryptionService.encrypt(firstName, RandomIV)
      lastNameEncrypted <- encryptionService.encrypt(lastName, RandomIV)

      emailEncrypted <- encryptionService.encrypt(email, DefaultIV)
      usernameEncrypted <- encryptionService.encrypt(s"$firstName.$lastName".toLowerCase, DefaultIV)

      encryptedPerson =
        EncryptedPerson(id, timestamp, timestamp, usernameEncrypted, firstNameEncrypted, lastNameEncrypted, emailEncrypted)

      maybePerson <-
        transaction {
          encryptedPersonDao.insert(encryptedPerson)
            .productR {
              encryptedPersonDao.findById(id)
            }
        }

      encryptedPerson <-
        maybePerson.toG[F](optionToF[Throwable, F](new InternalError("Unable to persist person")))

      person <- decrypt(encryptedPerson)

    } yield person

  override def findByEmail(email: String): F[Option[Person]] = ???

  private def decrypt(encryptedPerson: EncryptedPerson): F[Person] =
    for {
      firstName <- encryptionService.decrypt(encryptedPerson.firstName)
      lastName <- encryptionService.decrypt(encryptedPerson.lastName)
      username <- encryptionService.decrypt(encryptedPerson.username)
      email <- encryptionService.decrypt(encryptedPerson.email)

      person =
        Person(encryptedPerson.id, encryptedPerson.createdAt, encryptedPerson.modifiedAt, username, firstName, lastName, email)
    }
    yield person
}
