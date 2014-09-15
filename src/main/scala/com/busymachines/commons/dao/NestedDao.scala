package com.busymachines.commons.dao

import com.busymachines.commons.domain.Id
import scala.concurrent.Future
import com.busymachines.commons.domain.HasId

/**
 * DAO specific for ES nested types
 *
 * R - root type
 * S - shallow type, used for update only
 */
trait NestedDao[P <: HasId[P], T <: HasId[T]] extends Dao[T] {

  def retrieveParent(id : Id[T]) : Future[Option[Versioned[P]]]

  def retrieveWithParent(id: Id[T]): Future[Option[(P, T)]]

  def retrieveWithParentVersioned(id: Id[T]): Future[Option[(Versioned[P], Versioned[T])]]

  def create(parent : Id[P], entity: T, reindex : Boolean = true): Future[T]

  def createVersioned(parent : Id[P], entity: T, reindex : Boolean = true): Future[Versioned[T]]
}