package com.busymachines.commons.dao

object Versioned {
  implicit def toEntity[T](v: Versioned[T]) = v.entity
}

case class Versioned[T](entity: T, version: Long)
