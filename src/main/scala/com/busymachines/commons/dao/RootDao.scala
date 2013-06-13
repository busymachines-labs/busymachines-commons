package com.busymachines.commons.dao

import scala.concurrent.Future
import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.Id

/**
 * DAO specific for ES root types
 *
 * R - root type
 * S - shallow type, used for update only
 */
trait RootDao[T <: HasId[T]] extends Dao[T] {

  def retrieveAll(): Future[List[Versioned[T]]]

  def create(entity: T, reindex : Boolean = true): Future[Versioned[T]]
}

