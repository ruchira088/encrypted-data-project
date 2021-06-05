package com.ruchij.daos.dao.models

import com.ruchij.daos.dao.models.EncryptedField.InitializationVector

case class EncryptedField[A, B <: InitializationVector](data: Array[Byte])

object EncryptedField {
  sealed trait InitializationVector

  object InitializationVector {
    case object DefaultIV extends InitializationVector
    case object RandomIV extends InitializationVector
  }
}
