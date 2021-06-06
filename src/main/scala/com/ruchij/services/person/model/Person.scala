package com.ruchij.services.person.model

import org.joda.time.DateTime

import java.util.UUID

case class Person(
  id: UUID,
  createdAt: DateTime,
  modifiedAt: DateTime,
  username: String,
  firstName: String,
  lastName: String,
  email: String
) {
  override def toString: String =
    s"Person(id=$id, createdAt=$createdAt, modifiedAt=$modifiedAt, username=$username, firstName=$firstName, lastName=$lastName, email=$email)"
}
