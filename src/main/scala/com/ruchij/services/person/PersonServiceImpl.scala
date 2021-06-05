package com.ruchij.services.person

import cats.{Monad, ~>}
import cats.effect.Clock
import cats.implicits._
import com.ruchij.daos.models.EncryptedField.InitializationVector.{DefaultIV, RandomIV}
import com.ruchij.daos.person.EncryptedPersonDao
import com.ruchij.daos.person.models.EncryptedPerson
import com.ruchij.services.encryption.EncryptionService
import com.ruchij.services.encryption.models.ByteEncoder
import com.ruchij.services.person.model.Person
import com.ruchij.types.Random
import org.joda.time.DateTime

import java.util.UUID
import java.util.concurrent.TimeUnit

class PersonServiceImpl[F[+ _]: Clock: Random[*[_], UUID]: Monad: ByteEncoder[*[_], String], G[_]: Monad](
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
            .product {
              encryptedPersonDao.findById(id)
            }
        }

    } yield ???

  override def findByEmail(email: String): F[Option[Person]] = ???
}
