package com.ruchij.services.person

import com.ruchij.services.person.model.Person
import fs2.Stream

trait PersonService[F[_]] {
  def create(firstName: String, lastName: String, email: String): F[Person]

  def findByEmail(email: String): F[Option[Person]]

  val retrieveAll: Stream[F, Person]
}
