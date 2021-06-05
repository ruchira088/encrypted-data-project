package com.ruchij.types

import cats.effect.Sync
import cats.implicits._
import cats.{Applicative, Monad}
import com.github.javafaker.Faker
import com.ruchij.App.PersonEntry

import java.util.UUID

trait Random[F[_], +A] {
  def generate[B >: A]: F[B]
}

object Random {
  private val faker: Faker = Faker.instance()

  implicit def randomMonad[F[_]: Monad]: Monad[Random[F, *]] =
    new Monad[Random[F, *]] {
      override def pure[A](value: A): Random[F, A] = new Random[F, A] {
        override def generate[B >: A]: F[B] = Applicative[F].pure[B](value)
      }

      override def flatMap[A, B](random: Random[F, A])(f: A => Random[F, B]): Random[F, B] =
        new Random[F, B] {
          override def generate[C >: B]: F[C] =
            random.generate.flatMap { valueA => f(valueA).generate[C] }
        }

      override def tailRecM[A, B](value: A)(f: A => Random[F, Either[A, B]]): Random[F, B] =
        new Random[F, B] {
          override def generate[C >: B]: F[C] =
            f(value).generate.flatMap {
              case Left(value) => tailRecM(value)(f).generate[C]

              case Right(value) => pure(value).generate[C]
            }
        }
    }

  def apply[F[_], A](implicit random: Random[F, A]): Random[F, A] = random

  def eval[F[_]: Sync, A](thunk: => A): Random[F, A] =
    new Random[F, A] {
      override def generate[B >: A]: F[B] = Sync[F].delay(thunk)
    }

  implicit def randomUuid[F[_]: Sync]: Random[F, UUID] = Random.eval[F, UUID](UUID.randomUUID())

  implicit def randomPerson[F[_]: Sync]: Random[F, PersonEntry] =
    for {
      firstName <- eval(faker.name().firstName())
      lastName <- eval(faker.name().lastName())
      email <- eval(faker.internet().emailAddress())
    }
    yield PersonEntry(firstName, lastName, email)
}
