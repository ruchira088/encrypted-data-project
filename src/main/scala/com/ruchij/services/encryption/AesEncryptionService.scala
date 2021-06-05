package com.ruchij.services.encryption

import cats.effect.{Blocker, ContextShift, Sync}
import cats.implicits._
import com.ruchij.daos.dao.models.EncryptedField
import com.ruchij.daos.dao.models.EncryptedField.InitializationVector
import com.ruchij.daos.dao.models.EncryptedField.InitializationVector.DefaultIV
import com.ruchij.services.encryption.AesEncryptionService.Algorithm
import com.ruchij.services.encryption.models.{ByteDecoder, ByteEncoder}

import javax.crypto.spec.IvParameterSpec
import javax.crypto.{Cipher, SecretKey}

class AesEncryptionService[F[_]: Sync: ContextShift](
  secretKey: SecretKey,
  cpuBlocker: Blocker,
  defaultIV: IvParameterSpec
) extends EncryptionService[F] {

  val ivLength: Int = defaultIV.getIV.length

  override def encrypt[IV <: InitializationVector, A: ByteEncoder[F, *]](data: A, iv: IV): F[EncryptedField[A, IV]] =
    for {
      cipher <- Sync[F].delay(Cipher.getInstance(Algorithm))

      _ <- cpuBlocker.delay {
        if (iv == DefaultIV) cipher.init(Cipher.ENCRYPT_MODE, secretKey, defaultIV)
        else cipher.init(Cipher.ENCRYPT_MODE, secretKey)
      }

      bytes <- ByteEncoder[F, A].encode(data)

      encryptedData <- cpuBlocker.delay {
        cipher.getIV ++ cipher.doFinal(bytes)
      }

      encryptedField = EncryptedField[A, IV](encryptedData)

    } yield encryptedField

  override def decrypt[A: ByteDecoder[F, *]](encryptedField: EncryptedField[A, _]): F[A] =
    for {
      cipher <- Sync[F].delay(Cipher.getInstance(Algorithm))

      _ <- cpuBlocker.delay {
        cipher.init(
          Cipher.DECRYPT_MODE,
          secretKey,
          new IvParameterSpec(encryptedField.data.take(ivLength))
        )
      }

      bytes <-
        cpuBlocker.delay {
          cipher.doFinal(encryptedField.data.drop(ivLength))
        }

      value <- ByteDecoder[F, A].decode(bytes)

    } yield value

}

object AesEncryptionService {
  val Algorithm = "AES/CBC/PKCS5Padding"
}
