package com.ruchij.daos.models

import com.ruchij.daos.models.EncryptedField.InitializationVector

case class EncryptedField[A, B <: InitializationVector](data: Array[Byte])

object EncryptedField {
  sealed trait InitializationVector

  object InitializationVector {
    case object DefaultIV extends InitializationVector
    case object RandomIV extends InitializationVector
  }
}
