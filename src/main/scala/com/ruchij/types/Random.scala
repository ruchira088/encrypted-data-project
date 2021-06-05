package com.ruchij.types

trait Random[F[_], +A] {
  def generate[B >: A]: F[B]
}

object Random {
  def apply[F[_], A](implicit random: Random[F, A]): Random[F, A] = random
}