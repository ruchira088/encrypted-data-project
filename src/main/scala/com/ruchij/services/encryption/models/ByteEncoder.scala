package com.ruchij.services.encryption.models

trait ByteEncoder[F[_], -A] {
  def encode(value: A): F[Array[Byte]]
}
