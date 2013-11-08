package com.busymachines.commons.domain

import java.util.UUID
import java.nio.ByteBuffer

trait HasId[A] {
  def id: Id[A]
}

object Id {
  def generate[A] = new Id[A](UUID.randomUUID)
  def apply[A](id: String) = new Id[A](UUID.fromString(id))
  def apply[A](id: Array[Byte]) = ByteBuffer.wrap(id) match {
    case buffer =>
      new Id[A](new UUID(buffer.getLong(),buffer.getLong()))
  }
  def get[A](id : String) : Option[Id[A]] = {
    try {
      Some(new Id[A](UUID.fromString(id)))
    }
    catch {
      case t : Throwable => None
    }
  }
  def static[A](id : String) = new Id[A](UUID.nameUUIDFromBytes(id.getBytes("UTF-8")))
}

case class Id[A](uuid: UUID) {
  override def toString = uuid.toString
  def toByteArray = ByteBuffer.wrap(Array[Byte](16)) match {
    case buffer =>
      buffer.putLong(uuid.getMostSignificantBits);
      buffer.putLong(uuid.getLeastSignificantBits);
      buffer.array();
    }
}
