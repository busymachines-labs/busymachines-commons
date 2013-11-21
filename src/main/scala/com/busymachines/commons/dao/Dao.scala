package com.busymachines.commons.dao

import com.busymachines.commons.domain.Id
import com.busymachines.commons.domain.HasId
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

/**
 * High-level abstraction for DAOs for CRUD operations.
 *
 * T - Entity Type
 */
trait Dao[T <: HasId[T]] {
  
  def defaultSort:SearchSort

  def retrieve(id: Id[T]): Future[Option[Versioned[T]]]
  
  def retrieve(ids: Seq[Id[T]]): Future[List[Versioned[T]]]

  def search(criteria : SearchCriteria[T], page : Page = Page.first, sort:SearchSort = defaultSort, facets: Seq[Facet] = Seq.empty): Future[SearchResult[T]]

  def searchSingle(criteria : SearchCriteria[T], onMany : List[Versioned[T]] => Versioned[T] = _ => throw new MoreThanOneResultException): Future[Option[Versioned[T]]]

  def modify(id: Id[T], reindex : Boolean = true)(f : T => T): Future[Versioned[T]]
  
  def update(entity: Versioned[T], reindex : Boolean = true): Future[Versioned[T]]

  def delete(id: Id[T], reindex : Boolean = true): Future[Unit]
  
  def onChange(f: Id[T] => Unit): Unit
}

