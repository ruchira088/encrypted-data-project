package com.ruchij.services.encryption.models

trait ByteDecoder[F[+ _], +A] {
  def decode(bytes: Array[Byte]): F[A]
}

object ByteDecoder {
  def apply[F[+ _], A](implicit byteDecoder: ByteDecoder[F, A]): ByteDecoder[F, A] = byteDecoder
}