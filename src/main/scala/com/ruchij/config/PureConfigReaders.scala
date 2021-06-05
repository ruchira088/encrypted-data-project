package com.ruchij.config

import com.ruchij.types.FunctionKTypes._
import pureconfig.ConfigReader
import pureconfig.error.ExceptionThrown

import java.util.Base64
import javax.crypto.SecretKey
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
import scala.util.Try

object PureConfigReaders {
  val EncryptionAlgorithm = "AES"

  implicit val secretKeyConfigReader: ConfigReader[SecretKey] =
    ConfigReader[String].emap { key =>
      Try(Base64.getDecoder.decode(key))
        .flatMap { bytes =>
          Try(new SecretKeySpec(Base64.getDecoder.decode(bytes), EncryptionAlgorithm))
        }
        .toEither
        .left
        .map(ExceptionThrown)
    }

  implicit val ivParameterSpecConfigReader: ConfigReader[IvParameterSpec] =
    ConfigReader[String].flatMap { iv =>
      Try(Base64.getDecoder.decode(iv))
        .map { bytes =>
          new IvParameterSpec(bytes)
        }
        .toG[ConfigReader]
    }

}
