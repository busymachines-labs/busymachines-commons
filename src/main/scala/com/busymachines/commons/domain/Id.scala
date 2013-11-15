package com.busymachines.commons.domain

import java.util.UUID
import java.nio.ByteBuffer

trait HasId[A] {
  def id: Id[A]
}

object Id {
  def generate[A] = new Id[A](UUID.randomUUID.toString)
  def static[A](id : String) = new Id[A](id) // for backward compatibility with existing code
}

case class Id[A](value : String) {
  override def toString = value
  def uuid = 
    UUID.nameUUIDFromBytes(value.getBytes("UTF-8"))
}
