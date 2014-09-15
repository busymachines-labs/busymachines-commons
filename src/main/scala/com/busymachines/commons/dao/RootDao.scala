package com.busymachines.commons.dao

import scala.concurrent.Future
import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.Id
import scala.concurrent.duration.Duration

/**
 * DAO specific for ES root types
 *
 * R - root type
 * S - shallow type, used for update only
 */
trait RootDao[T <: HasId[T]] extends Dao[T] {

  def retrieveAll: Future[List[Versioned[T]]]

  def create(entity: T, refresh : Boolean = true, ttl: Option[Duration] = None): Future[T]

  def createVersioned(entity: T, refresh : Boolean = true, ttl: Option[Duration] = None): Future[Versioned[T]]

  def retrieveOrCreate(id : Id[T], refresh : Boolean = true)(create : => T): Future[T]

  def retrieveOrCreateAndModify(id : Id[T], refresh : Boolean = true)(create : => T)(modify : T => T): Future[T]

  def retrieveOrCreateAndModifyOptionally(id : Id[T], refresh : Boolean = true)(create : => T)(modify : T => Option[T]): Future[T]

  def retrieveOrCreateVersioned(id : Id[T], refresh : Boolean = true)(create : => T): Future[Versioned[T]]

  def retrieveOrCreateAndModifyVersioned(id : Id[T], refresh : Boolean = true)(create : => T)(modify : T => T): Future[Versioned[T]]

  def retrieveOrCreateAndModifyOptionallyVersioned(id : Id[T], refresh : Boolean = true)(create : => T)(modify : T => Option[T]): Future[Versioned[T]]

  def reindexAll()
}

