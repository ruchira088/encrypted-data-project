package com.ruchij.services.person

import com.ruchij.services.person.model.Person

trait PersonService[F[_]] {
  def create(firstName: String, lastName: String, email: String): F[Person]

  def findByEmail(email: String): F[Option[Person]]
}
