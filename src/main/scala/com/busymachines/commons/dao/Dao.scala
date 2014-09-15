package com.busymachines.commons.dao

import com.busymachines.commons.domain.Id
import com.busymachines.commons.domain.HasId
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

trait Facet {
  def name: String
  def searchCriteria: SearchCriteria[_]
}
trait FacetValue
case class TermFacetValue(value: String, count: Int) extends FacetValue
case class HistogramFacetValue(
  key: Long,
  count: Double,
  min: Double,
  max: Double,
  total: Double,
  total_count: Double,
  mean: Double) extends FacetValue

trait SearchSort {
  def asc: SearchSort
  def desc: SearchSort
}

/**
 * The search criteria trait indicates that something is a search criteria in general.
 * One of the consequences of this is that there should be very few assumptions made about what is a "search criteria".
 * For example it might seem intuitive that any search criteria should have "and", "or", "not", etc. operations and as such those
 * should be defined in the trait as well. But in fact it's unclear if all the search criteria implementations could support this.
 */
trait SearchCriteria[T]

/**
 * High-level abstraction for DAOs for CRUD operations.
 *
 * T - Entity Type
 */
trait Dao[T <: HasId[T]] {
  
  def retrieve(id: Id[T]): Future[Option[T]]

  def retrieveVersioned(id: Id[T]): Future[Option[Versioned[T]]]

  def retrieve(ids: Seq[Id[T]]): Future[List[T]]

  def retrieveVersioned(ids: Seq[Id[T]]): Future[List[Versioned[T]]]

  def search(criteria: SearchCriteria[T], page: Page = Page.first, sort: SearchSort = defaultSort, facets: Seq[Facet] = Seq.empty): Future[SearchResult[T]]

  def searchVersioned(criteria: SearchCriteria[T], page: Page = Page.first, sort: SearchSort = defaultSort, facets: Seq[Facet] = Seq.empty): Future[VersionedSearchResult[T]]

  def find(criteria: SearchCriteria[T], page: Page = Page.first, sort: SearchSort = defaultSort): Future[List[T]]

  def findVersioned(criteria: SearchCriteria[T], page: Page = Page.first, sort: SearchSort = defaultSort): Future[List[Versioned[T]]]

  def findSingle(criteria: SearchCriteria[T], onMany: List[T] => T = _ => throw new MoreThanOneResultException): Future[Option[T]]

  def findSingleVersioned(criteria: SearchCriteria[T], onMany: List[Versioned[T]] => Versioned[T] = _ => throw new MoreThanOneResultException): Future[Option[Versioned[T]]]

  def modify(id: Id[T], reindex: Boolean = true)(f: T => T): Future[T]

  def modifyVersioned(id: Id[T], reindex: Boolean = true)(f: Versioned[T] => T): Future[Versioned[T]]

  def modifyOptionally(id: Id[T], refresh: Boolean = true)(f: T => Option[T]): Future[T]

  def modifyOptionallyVersioned (id: Id[T], refresh: Boolean = true)(f: Versioned[T] => Option[Versioned[T]]): Future[Versioned[T]]

  def update(entity: Versioned[T], refresh: Boolean = true): Future[T]

  def updateVersioned(entity: Versioned[T], refresh: Boolean = true): Future[Versioned[T]]

  def delete(id: Id[T], refresh: Boolean = true): Future[Unit]

  def deleteVersioned(id: Id[T], refresh: Boolean = true): Future[Long]

  def onChange(f: Id[T] => Unit): Unit

  def all: SearchCriteria[T]

  def defaultSort: SearchSort
}

