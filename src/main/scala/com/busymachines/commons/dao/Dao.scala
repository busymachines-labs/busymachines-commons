package com.busymachines.commons.dao

import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.Id
import scala.concurrent.Future

/**
 * High-level abstraction for DAOs for CRUD operations.
 *
 * T - Entity Type
 */
trait Dao[T <: HasId[T]] {
  
  def retrieve(id: Id[T]): Future[Option[Versioned[T]]]

  def modify(id: Id[T], reindex : Boolean = true)(f : T => T): Future[Versioned[T]]
  
  def update(entity: Versioned[T], reindex : Boolean = true): Future[Versioned[T]]

  def delete(id: Id[T], reindex : Boolean = true): Future[Unit]
}

