package com.busymachines.commons.elasticsearch

import collection.JavaConversions._
import com.busymachines.commons.logging.Logging
import com.busymachines.commons.dao._
import com.busymachines.commons.dao.Versioned.toEntity
import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.Id
import com.busymachines.commons.event.BusEvent
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.common.xcontent.XContentHelper
import org.elasticsearch.index.engine.VersionConflictEngineException
import org.elasticsearch.index.query.FilterBuilder
import org.elasticsearch.index.query.FilterBuilders
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.facet.FacetBuilder
import org.elasticsearch.search.facet.FacetBuilders
import org.elasticsearch.search.facet.histogram.HistogramFacet
import org.elasticsearch.search.facet.terms.TermsFacet
import org.elasticsearch.search.sort.SortOrder
import org.elasticsearch.transport.RemoteTransportException
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, Duration}
import scala.reflect.ClassTag
import spray.json._
import scala.concurrent.duration.DurationInt
import scala.Some

object ESRootDao {
  implicit def toResults[T <: HasId[T]] (f: Future[SearchResult[T]])(implicit ec: ExecutionContext) = f.map (_.result)
}

class ESRootDao[T <: HasId[T] : JsonFormat : ClassTag] (index: ESIndex, t: ESType[T])(implicit ec: ExecutionContext, tag: ClassTag[T]) extends ESDao[T](t.name) with RootDao[T] with Logging {

  val mapping = t.mapping
  val collection = new ESCollection[T](index, t.name, mapping)

  def allSearchText (searchText: String): ESSearchCriteria[T] =
    propertySearchText (mapping._all, searchText)

  def allSearchQuery (searchQuery: String): ESSearchCriteria[T] =
    propertySearchQuery (mapping._all, searchQuery)

  def propertySearchText (field: ESField[_, String], searchText: String): ESSearchCriteria[T] =
    propertySearchQuery (field, s"*${ESSearchCriteria.escapeQueryString (searchText)}*")

  def propertySearchQuery (property: ESField[_, String], searchQuery: String): ESSearchCriteria[T] =
    (property query searchQuery).asInstanceOf[ESSearchCriteria[T]]

	def retrieve(ids: Seq[Id[T]]): Future[List[T]] =
	  collection.retrieve(ids.map (_.value))
	  
  def retrieveVersioned(ids: Seq[Id[T]]): Future[List[Versioned[T]]] =
    collection.versioned.retrieve(ids.map (_.value))

  def retrieve (id: Id[T]): Future[Option[T]] =
    collection.retrieve(id.value)

  def retrieveVersioned(id: Id[T]): Future[Option[Versioned[T]]] =
    collection.versioned.retrieve(id.value)

  def reindexAll () =
    collection.reindexAll ()

  def scan (criteria: SearchCriteria[T], duration: FiniteDuration = 5 minutes, batchSize: Int = 100): Iterator[T] =
    collection.scan (criteria, duration, batchSize)

  def find(criteria: SearchCriteria[T], page: Page = Page.first, sort: SearchSort = defaultSort): Future[List[T]] =
    collection.find(criteria, page, sort)

  def findVersioned(criteria: SearchCriteria[T], page: Page = Page.first, sort: SearchSort = defaultSort): Future[List[Versioned[T]]] =
    collection.versioned.find(criteria, page, sort)

  def search(criteria: SearchCriteria[T], page: Page = Page.first, sort: SearchSort = defaultSort, facets: Seq[Facet] = Seq.empty): Future[SearchResult[T]] =
    collection.search(criteria, page, sort, facets)

  def searchVersioned(criteria: SearchCriteria[T], page: Page = Page.first, sort: SearchSort = defaultSort, facets: Seq[Facet] = Seq.empty): Future[VersionedSearchResult[T]] =
    collection.versioned.search(criteria, page, sort, facets)

  def create (entity: T, refresh: Boolean, ttl: Option[Duration]): Future[T] =
    collection.versioned.create (entity, refresh, ttl).map(_.entity)

  def createVersioned (entity: T, refresh: Boolean, ttl: Option[Duration]): Future[Versioned[T]] =
    collection.versioned.create (entity, refresh, ttl)

  def retrieveOrCreate (id: Id[T], refreshAfterMutation: Boolean)(create: => T): Future[T] =
    collection.retrieveOrCreate (id.value, refreshAfterMutation)(create)

  def retrieveOrCreateAndModify (id: Id[T], refresh: Boolean)(create: => T)(modify: T => T): Future[T] =
    collection.retrieveOrCreateAndModify (id.value, refresh)(create)(modify)

  def retrieveOrCreateAndModifyOptionally (id: Id[T], refresh: Boolean)(create: => T)(modify: T => Option[T]): Future[T] =
    collection.retrieveOrCreateAndModifyOptionally (id.value, refresh)(create)(modify)

  def retrieveOrCreateVersioned (id: Id[T], refreshAfterMutation: Boolean)(create: => T): Future[Versioned[T]] =
    collection.versioned.retrieveOrCreate (id.value, refreshAfterMutation)(create)

  def retrieveOrCreateAndModifyVersioned (id: Id[T], refresh: Boolean)(create: => T)(modify: T => T): Future[Versioned[T]] =
    collection.versioned.retrieveOrCreateAndModify (id.value, refresh)(create)(modify)

  def retrieveOrCreateAndModifyOptionallyVersioned (id: Id[T], refresh: Boolean)(create: => T)(modify: T => Option[T]): Future[Versioned[T]] =
    collection.versioned.retrieveOrCreateAndModifyOptionally (id.value, refresh)(create)(modify)

  def modify (id: Id[T], refresh: Boolean)(modify: T => T): Future[T] =
    collection.modify (id.value, refresh)(modify)

  def modifyVersioned (id: Id[T], refresh: Boolean)(modify: Versioned[T] => T): Future[Versioned[T]] =
    collection.versioned.modify (id.value, refresh)(modify)

  def modifyOptionally (id: Id[T], refresh: Boolean)(modify: T => Option[T]): Future[T] =
    collection.modifyOptionally (id.value, refresh)(modify)

  def modifyOptionallyVersioned (id: Id[T], refresh: Boolean)(modify: Versioned[T] => Option[Versioned[T]]): Future[Versioned[T]] =
    collection.versioned.modifyOptionally (id.value, refresh)(modify)

  /**
   * @throws VersionConflictException
   */
  def update (entity: Versioned[T], refresh: Boolean): Future[T] =
    collection.update (entity, refresh)

  def updateVersioned (entity: Versioned[T], refresh: Boolean): Future[Versioned[T]] =
    collection.versioned.update (entity, refresh)

  def delete (id: Id[T], refresh: Boolean): Future[Unit] =
    collection.delete (id.value, refresh)

  def deleteVersioned (id: Id[T], refresh: Boolean): Future[Long] =
    collection.versioned.delete (id.value, refresh)

  //  def query(queryBuilder: QueryBuilder, page: Page): Future[SearchResult[T]] = {
  //    val request = client.javaClient.prepareSearch(index.name).setTypes(t.name).setQuery(queryBuilder).addSort("_id", SortOrder.ASC).setFrom(page.from).setSize(page.size).setVersion(true).request
  //    client.execute(request).map { result =>
  //      SearchResult(result.getHits.hits.toList.map { hit =>
  //        val json = hit.sourceAsString.asJson
  //        Versioned(json.convertFromES(mapping), hit.getVersion)
  //      }, Some(result.getHits.getTotalHits))
  //    }
  //  }
  //
  //  def queryWithString(queryStr: String, page: Page): Future[SearchResult[T]] =
  //    query(new QueryStringQueryBuilder(queryStr), page)

  //  protected def retrieve(filter: FilterBuilder, error: => String): Future[Option[Versioned[T]]] = {
  //    doSearch(filter).map {
  //      case Nil => None
  //      case entity :: Nil => Some(entity)
  //      case entities => throw new Exception(error)
  //    }
  //  }

  @deprecated("Use ESCollection.scan", "0.6")
  def retrieveAll: Future[List[Versioned[T]]] =
    collection.retrieveAll

  def onChange (f: Id[T] => Unit) =
    collection.onChange (id => f (Id (id)))
}

case class ESRootDaoMutationEvent (eventName: String, id: String) extends BusEvent
