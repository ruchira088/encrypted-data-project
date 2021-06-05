package com.ruchij.daos.person.models

import com.ruchij.daos.doobie.models.EncryptedField
import com.ruchij.daos.doobie.models.EncryptedField.InitializationVector.{DefaultIV, RandomIV}
import org.joda.time.DateTime

import java.util.UUID

case class EncryptedPerson(
  id: UUID,
  createdAt: DateTime,
  modifiedAt: DateTime,
  username: EncryptedField[String, DefaultIV.type],
  firstName: EncryptedField[String, RandomIV.type],
  lastName: EncryptedField[String, RandomIV.type],
  email: EncryptedField[String, DefaultIV.type]
)
