package com.ruchij.config

import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}

case class EncryptionConfiguration(secretKey: SecretKeySpec, defaultIV: IvParameterSpec)
