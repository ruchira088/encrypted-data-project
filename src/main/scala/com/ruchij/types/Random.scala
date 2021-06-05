package com.ruchij.types

trait Random[F[+ _], +A] {
  val generate: F[A]
}

object Random {
  def apply[F[+ _], A](implicit random: Random[F, A]): Random[F, A] = random
}