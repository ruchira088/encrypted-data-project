package com.ruchij.services.encryption

import com.ruchij.daos.doobie.models.EncryptedField
import com.ruchij.daos.doobie.models.EncryptedField.InitializationVector
import com.ruchij.services.encryption.models.{ByteDecoder, ByteEncoder}

trait EncryptionService[F[_]] {
  def encrypt[IV <: InitializationVector, A: ByteEncoder[F, *]](data: A, iv: IV): F[EncryptedField[A, IV]]

  def decrypt[A: ByteDecoder[F, *]](encryptedField: EncryptedField[A, _]): F[A]
}
