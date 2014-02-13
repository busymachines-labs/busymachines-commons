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

  def retry(future: => Future[Versioned[T]], maxAttempts: Int = 3, attempt: Int = 1): Future[Versioned[T]] 
  
  def retrieveAll: Future[List[Versioned[T]]]

  def create(entity: T, reindex : Boolean = true, ttl: Option[Duration] = None): Future[Versioned[T]]

  def getOrCreate(id : Id[T], reindex : Boolean = true)(create : => T): Future[Versioned[T]] 
  
  def getOrCreateAndModify(id : Id[T], reindex : Boolean = true)(create : => T)(modify : T => T): Future[Versioned[T]]

  def getOrCreateAndModifyOptionally(id : Id[T], reindex : Boolean = true)(create : => T)(modify : T => Option[T]): Future[Versioned[T]]
  
  def reindexAll()
}

