package com.busymachines.commons.elasticsearch

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.reflect.ClassTag
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.index.engine.VersionConflictEngineException
import org.elasticsearch.index.query.FilterBuilder
import org.elasticsearch.index.query.FilterBuilders
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.QueryStringQueryBuilder
import org.elasticsearch.search.sort.SortOrder
import com.busymachines.commons.Logging
import com.busymachines.commons.dao.FacetField
import com.busymachines.commons.dao.IdNotFoundException
import com.busymachines.commons.dao.Page
import com.busymachines.commons.dao.RetryVersionConflictAsync
import com.busymachines.commons.dao.RootDao
import com.busymachines.commons.dao.SearchCriteria
import com.busymachines.commons.dao.SearchResult
import com.busymachines.commons.dao.SearchSort
import com.busymachines.commons.dao.VersionConflictException
import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.dao.Versioned.toEntity
import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.Id
import com.busymachines.commons.elasticsearch.ESClient.toEsClient
import com.busymachines.commons.elasticsearch.implicits.richEnity
import com.busymachines.commons.elasticsearch.implicits.richJsValue
import com.busymachines.commons.event.BusEvent
import spray.json.JsonFormat
import spray.json.pimpString
import com.busymachines.commons.dao.Facet
import org.elasticsearch.search.facet.FacetBuilders
import org.elasticsearch.search.facet.FacetBuilder

object ESRootDao {
  implicit def toResults[T <: HasId[T]](f: Future[SearchResult[T]])(implicit ec: ExecutionContext) = f.map(_.result)
}

class ESRootDao[T <: HasId[T]: JsonFormat: ClassTag](index: ESIndex, t: ESType[T])(implicit ec: ExecutionContext, tag: ClassTag[T]) extends ESDao[T](t.name) with RootDao[T] with Logging {

  val client = index.client
  val mapping = t.mapping
  // Add mapping.
  index.onInitialize { () =>
    val mappingConfiguration = t.mapping.mappingConfiguration(t.name)
    debug(mappingConfiguration)
    client.admin.indices.putMapping(new PutMappingRequest(index.name).`type`(t.name).source(mappingConfiguration)).get()
  }

  def retrieve(ids: Seq[Id[T]]): Future[List[Versioned[T]]] =
    query(QueryBuilders.idsQuery(t.name).addIds(ids.map(id => id.toString): _*), Page.all) map { result => result.result }

  def retrieve(id: Id[T]): Future[Option[Versioned[T]]] = {
    val request = new GetRequest(index.name, t.name, id.toString)
    client.execute(request) map {
      case response if !response.isExists =>
        None
      case response if response.isSourceEmpty =>
        error(s"No source available for ${t.name} with id $id")
        None
      case response =>
        val json = response.getSourceAsString.asJson
        Some(Versioned(json.convertFromES(mapping), response.getVersion))
    }
  }

  private def toESFacets(facets: Seq[Facet]):Seq[FacetBuilder] = 
    facets.map(_ match {
      case termFacet: ESTermFacet[_, T] =>
        val fieldList = (termFacet.fields.map(field => field.toOptionString match {
          case None => None
          case Some(fieldName) => Some(fieldName)
        })).flatten
        
        FacetBuilders.termsFacet(termFacet.name).size(termFacet.size).fields(fieldList: _*)
      case _ => throw new Exception(s"Unknown facet term instance")
    })
  

  def search(criteria: SearchCriteria[T], page: Page = Page.first, sort: SearchSort = ESSearchSort.asc("_id"), facets: Seq[Facet] = Seq.empty): Future[SearchResult[T]] = {
    criteria match {
      case criteria: ESSearchCriteria[T] =>
        var request =
          client.javaClient.prepareSearch(index.name)
            .setTypes(t.name)
            .setFilter(criteria.toFilter)
            .setSearchType(SearchType.DFS_QUERY_AND_FETCH)
            .setFrom(page.from)
            .setSize(page.size)
            .addSort(sort.field, sort.order)
            .setVersion(true)
            
        for (facet <- toESFacets(facets)) {
          request = request.addFacet(facet)
        }
            
        debug(s"Executing search ${request}")
        client.execute(request.request).map { result =>
          SearchResult(result.getHits.hits.toList.map { hit =>
            val json = hit.sourceAsString.asJson
            Versioned(json.convertFromES(mapping), hit.version)
          }, Some(result.getHits.getTotalHits))
        }
      case _ =>
        throw new Exception("Expected ElasticSearch search criteria")
    }
  }

  def create(entity: T, refreshAfterMutation: Boolean = true): Future[Versioned[T]] = {
    val json = entity.convertToES(mapping)
    val request = new IndexRequest(index.name, t.name)
      .id(entity.id.toString)
      .create(true)
      .source(json.toString)
      .refresh(refreshAfterMutation)

    debug(s"Create $t.name: $json")

    // Call synchronously, useful for debugging: proper stack trace is reported. TODO make config flag.       
    //      val response = client.execute(IndexAction.INSTANCE, request).get
    //      Future.successful(Versioned(entity, response.getVersion.toString))

    preMutate(entity) flatMap { _ =>
      client.execute(request).map(response => Versioned(entity, response.getVersion)) flatMap { storedEntity =>
        postMutate(storedEntity) map { _ => storedEntity }
      }
    }
  }

  def getOrCreate(id: Id[T], refreshAfterMutation: Boolean = true)(_create: => T): Future[Versioned[T]] = {
    retrieve(id) flatMap {
      case Some(entity) => Future.successful(entity)
      case None => create(_create, refreshAfterMutation)
    }
  }

  def getOrCreateAndModify(id: Id[T], refreshAfterMutation: Boolean = true)(_create: => T)(_modify: T => T): Future[Versioned[T]] =
    getOrCreate(id, refreshAfterMutation)(_create).flatMap(entity => update(entity.copy(entity = _modify(entity)), refreshAfterMutation))

  def modify(id: Id[T], refreshAfterMutation: Boolean = true)(modify: T => T): Future[Versioned[T]] = {
    RetryVersionConflictAsync(10) {
      retrieve(id).flatMap {
        case None => throw new IdNotFoundException(id.toString, t.name)
        case Some(Versioned(entity, version)) =>
          update(Versioned(modify(entity), version), refreshAfterMutation)
      }
    }
  }

  /**
   * @throws VersionConflictException
   */
  def update(entity: Versioned[T], refreshAfterMutation: Boolean = true): Future[Versioned[T]] = {
    val newJson = entity.entity.convertToES(mapping)
    val request = new IndexRequest(index.name, t.name)
      .refresh(refreshAfterMutation)
      .id(entity.entity.id.toString)
      .source(newJson.toString)
      .version(entity.version)

    debug(s"Update $t.name: $newJson")

    // Call synchronously, useful for debugging: proper stack trace is reported. TODO make config flag.       
    //      val response = client.execute(IndexAction.INSTANCE, request).get
    //      Future.successful(Versioned(entity, response.getVersion.toString))
    preMutate(entity).flatMap { entity: T =>
      client.execute(request).map(response => Versioned(entity.entity, response.getVersion)) flatMap { mutatedEntity =>
        postMutate(mutatedEntity.entity) map { _ => mutatedEntity }
      }
    }.recover { case e: VersionConflictEngineException => throw new VersionConflictException(e) }
  }

  def delete(id: Id[T], refreshAfterMutation: Boolean = true): Future[Unit] =
    retrieve(id) map (entity => {
      val request = new DeleteRequest(index.name, t.name, id.toString).refresh(refreshAfterMutation)
      (entity match {
        case None => Future.successful()
        case Some(e) => preMutate(e)
      }) flatMap { _ =>
        client.execute(request) map {
          response =>
            if (response.isNotFound) {
              throw new IdNotFoundException(id.toString, t.name)
            }
        }
      } flatMap { _ =>
        (entity match {
          case None => Future.successful()
          case Some(e) => preMutate(e)
        })
      }
    })

  def query(queryBuilder: QueryBuilder, page: Page): Future[SearchResult[T]] = {
    val request = client.javaClient.prepareSearch(index.name).setTypes(t.name).setQuery(queryBuilder).addSort("_id", SortOrder.ASC).setFrom(page.from).setSize(page.size).setVersion(true).request
    client.execute(request).map { result =>
      SearchResult(result.getHits.hits.toList.map { hit =>
        val json = hit.sourceAsString.asJson
        Versioned(json.convertFromES(mapping), hit.getVersion)
      }, Some(result.getHits.getTotalHits))
    }
  }

  def queryWithString(queryStr: String, page: Page): Future[SearchResult[T]] =
    query(new QueryStringQueryBuilder(queryStr), page)

  protected def doSearch(filter: FilterBuilder): Future[List[Versioned[T]]] = {
    val request = client.javaClient.prepareSearch(index.name).addSort("_id", SortOrder.ASC).setTypes(t.name).setFilter(filter).setVersion(true).request
    client.execute(request).map(_.getHits.hits.toList.map { hit =>
      val json = hit.sourceAsString.asJson
      Versioned(json.convertFromES(mapping), hit.getVersion)
    })
  }

  protected def retrieve(filter: FilterBuilder, error: => String): Future[Option[Versioned[T]]] = {
    doSearch(filter).map(_ match {
      case Nil => None
      case entity :: Nil => Some(entity)
      case entities => throw new Exception(error)
    })
  }

  def retrieveAll: Future[List[Versioned[T]]] = retrieveAll(FilterBuilders.matchAllFilter())

  def retrieveAll(filter: FilterBuilder): Future[List[Versioned[T]]] = {
    doSearch(filter).map(_ match {
      case Nil => Nil
      case entities => entities
    })
  }

  def onChange(f: Id[T] => Unit) {
    index.bus.subscribe {
      case ESRootDaoMutationEvent(n, id) if n == eventName =>
        f(Id[T](id))
    }
  }

  protected val eventName = tag.runtimeClass.getName.replaceAllLiterally("$", "") + "_" + index.name + "_" + t.name

  protected def preMutate(entity: T): Future[T] =
    Future.successful(entity)

  protected def postMutate(entity: T): Future[Unit] =
    Future.successful(index.bus.publish(ESRootDaoMutationEvent(eventName, entity.id.toString)))

  implicit def toResults(f: Future[SearchResult[T]]) = ESRootDao.toResults(f)
}

case class ESRootDaoMutationEvent(eventName: String, id: String) extends BusEvent 
