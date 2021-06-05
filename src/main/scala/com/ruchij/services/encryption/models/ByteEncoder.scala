package com.ruchij.services.encryption.models

trait ByteEncoder[F[_], -A] {
  def encode(value: A): F[Array[Byte]]
}

object ByteEncoder {
  def apply[F[_], A](implicit byteEncoder: ByteEncoder[F, A]): ByteEncoder[F, A] = byteEncoder
}
