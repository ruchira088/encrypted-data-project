package com.ruchij.services.encryption.models

import cats.Applicative

trait ByteDecoder[F[_], +A] {
  def decode[B >: A](bytes: Array[Byte]): F[B]
}

object ByteDecoder {
  def apply[F[_], A](implicit byteDecoder: ByteDecoder[F, A]): ByteDecoder[F, A] = byteDecoder

  implicit def stringDecoder[F[_]: Applicative]: ByteDecoder[F, String] = new ByteDecoder[F, String] {
    override def decode[B >: String](bytes: Array[Byte]): F[B] = Applicative[F].pure[B](new String(bytes))
  }
}