package com.ruchij.config

import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

case class EncryptionConfiguration(secretKey: SecretKey, defaultIV: IvParameterSpec)
