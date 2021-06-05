package com.ruchij.config

import com.ruchij.types.FunctionKTypes._
import pureconfig.ConfigReader

import java.util.Base64
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
import scala.util.Try

object PureConfigReaders {
  val EncryptionAlgorithm = "AES"

  implicit val secretKeyConfigReader: ConfigReader[SecretKeySpec] =
    ConfigReader[String].flatMap { key =>
      Try(Base64.getDecoder.decode(key))
        .flatMap { bytes =>
          Try(new SecretKeySpec(bytes, EncryptionAlgorithm))
        }
        .toG[ConfigReader]
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
