package com.ruchij.daos.person

import com.ruchij.daos.doobie.models.EncryptedField
import com.ruchij.daos.doobie.models.EncryptedField.InitializationVector.DefaultIV
import com.ruchij.daos.person.models.EncryptedPerson

import java.util.UUID

trait EncryptedPersonDao[F[_]] {
  def insert(encryptedPerson: EncryptedPerson): F[Int]

  def findByEmail(email: EncryptedField[String, DefaultIV.type]): F[Option[EncryptedPerson]]

  def findById(id: UUID): F[Option[EncryptedPerson]]

  def retrieveAll(offset: Int, pageSize: Int): F[List[EncryptedPerson]]
}
