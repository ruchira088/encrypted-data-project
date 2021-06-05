package com.ruchij.services.encryption.models

import cats.Applicative

trait ByteEncoder[F[_], -A] {
  def encode(value: A): F[Array[Byte]]
}

object ByteEncoder {
  def apply[F[_], A](implicit byteEncoder: ByteEncoder[F, A]): ByteEncoder[F, A] = byteEncoder

  implicit def stringEncoder[F[_]: Applicative]: ByteEncoder[F, String] =
    (value: String) => Applicative[F].pure(value.getBytes)
}
