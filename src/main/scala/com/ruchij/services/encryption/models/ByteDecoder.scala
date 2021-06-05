package com.ruchij.services.encryption.models

trait ByteDecoder[F[+ _], +A] {
  def decode(bytes: Array[Byte]): F[A]
}
