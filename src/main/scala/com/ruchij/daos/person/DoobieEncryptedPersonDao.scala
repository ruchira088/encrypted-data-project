package com.ruchij.daos.person

import com.ruchij.daos.doobie.CustomMappings._
import com.ruchij.daos.doobie.models.EncryptedField
import com.ruchij.daos.doobie.models.EncryptedField.InitializationVector
import com.ruchij.daos.person.models.EncryptedPerson
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator

import java.util.UUID

object DoobieEncryptedPersonDao extends EncryptedPersonDao[ConnectionIO] {
  val SelectQuery =
    fr"SELECT id, created_at, modified_at, username, first_name, last_name, email FROM person"

  override def insert(encryptedPerson: EncryptedPerson): ConnectionIO[Int] =
    sql"""
        INSERT INTO person (id, created_at, modified_at, username, first_name, last_name, email)
            VALUES (
                ${encryptedPerson.id},
                ${encryptedPerson.createdAt},
                ${encryptedPerson.modifiedAt},
                ${encryptedPerson.username.data},
                ${encryptedPerson.firstName.data},
                ${encryptedPerson.lastName.data},
                ${encryptedPerson.email.data}
            )
    """
      .update
      .run

  override def findByEmail(email: EncryptedField[String, InitializationVector.DefaultIV.type]): ConnectionIO[Option[EncryptedPerson]] =
    (SelectQuery ++ fr"WHERE email = ${email.data}").query[EncryptedPerson].option

  override def findById(id: UUID): ConnectionIO[Option[EncryptedPerson]] =
    (SelectQuery ++ fr"WHERE id = $id").query[EncryptedPerson].option

  override def retrieveAll(offset: Int, pageSize: Int): ConnectionIO[List[EncryptedPerson]] =
    (SelectQuery ++ fr"LIMIT $pageSize OFFSET ${offset * pageSize}").query[EncryptedPerson].to[List]
}
