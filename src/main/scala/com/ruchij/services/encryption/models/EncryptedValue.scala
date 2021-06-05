package com.ruchij.services.encryption.models

import com.ruchij.daos.models.EncryptedField.InitializationVector

case class EncryptedValue[A <: InitializationVector](data: Array[Byte], iv: A)
