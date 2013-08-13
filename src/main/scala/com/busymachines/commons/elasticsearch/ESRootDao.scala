package com.busymachines.commons.elasticsearch

import scala.annotation.implicitNotFound
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.FilterBuilder
import org.elasticsearch.index.query.FilterBuilders
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.QueryStringQueryBuilder
import com.busymachines.commons.Logging
import com.busymachines.commons.dao.IdNotFoundException
import com.busymachines.commons.dao.RootDao
import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.elasticsearch.implicits._
import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.Id
import spray.json.JsonFormat
import spray.json.pimpAny
import spray.json.pimpString
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest
import com.busymachines.commons.dao.SearchCriteria
import org.elasticsearch.action.index.IndexAction
import org.elasticsearch.common.io.stream.BytesStreamOutput
import org.elasticsearch.action.search.SearchType
import com.busymachines.commons.dao.SearchResult
import com.busymachines.commons.dao.FacetField
import com.busymachines.commons.dao.Page
import com.busymachines.commons.event.DaoMutationEvent

object ESRootDao {
  implicit def toResults[T <: HasId[T]](f: Future[SearchResult[T]])(implicit ec: ExecutionContext) = f.map(_.result)
}

class ESRootDao[T <: HasId[T]: JsonFormat](index: ESIndex, t: ESType[T])(implicit ec: ExecutionContext) extends ESDao[T](t.name) with RootDao[T] with Logging {

  val client = index.client
  val mapping = t.mapping
  //val busEndpoint = index.bus.createEndpoint
  // Add mapping.
  index.onInitialize { () =>
    val mappingConfiguration = t.mapping.mappingConfiguration(t.name)
    debug(mappingConfiguration)
    client.admin.indices.putMapping(new PutMappingRequest(index.name).`type`(t.name).source(mappingConfiguration)).get()
  }

  protected def preMutate(entity: T): Future[Unit] =
    Future.successful()

  protected def postMutate(entity: T): Future[Unit] =
    Future.successful()
    /*
    busEndpoint.publish(DaoMutationEvent(
        entityType = getClass.toString,
        indexName = index.name,
        typeName = "",
        id = entity.id
        ))
	*/
  def retrieve(ids: Seq[Id[T]]): Future[List[Versioned[T]]] =
    query(QueryBuilders.idsQuery(t.name).addIds(ids.map(id => id.toString): _*))

  def retrieve(id: Id[T]): Future[Option[Versioned[T]]] = {
    val request = new GetRequest(index.name, t.name, id.toString)
    client.execute(request).map(response => Option(response.getSourceAsString)) map {
      case None => None
      case Some(source) =>
        val json = source.asJson
        val version = json.getESVersion
        Some(Versioned(json.convertFromES(mapping), version))
    }
  }

  def search(criteria: SearchCriteria[T], page: Page = Page.first, facets: Seq[FacetField] = Seq.empty): Future[SearchResult[T]] = {
    criteria match {
      case criteria: ESSearchCriteria[T] =>
        val request =
          client.javaClient.prepareSearch(index.name)
            .setTypes(t.name)
            .setFilter(criteria.toFilter)
            .setSearchType(SearchType.DFS_QUERY_AND_FETCH)
            .setFrom(page.from)
            .setSize(page.size)
        client.execute(request.request).map { result =>
          SearchResult(result.getHits.hits.toList.map { hit =>
            val json = hit.sourceAsString.asJson
            val version = json.getESVersion
            Versioned(json.convertFromES(mapping), version)
          }, Some(result.getHits.getTotalHits))
        }
      case _ =>
        throw new Exception("Expected ElasticSearch search criteria")
    }
  }

  def create(entity: T, refreshAfterMutation: Boolean): Future[Versioned[T]] = {
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
      client.execute(request).map(response => Versioned(entity, response.getVersion.toString)) flatMap { storedEntity =>
        postMutate(storedEntity) map { _ => storedEntity }
      }
    }
  }

  def modify(id: Id[T], refreshAfterMutation: Boolean)(modify: T => T): Future[Versioned[T]] = {
    retrieve(id).flatMap {
      case None => throw new IdNotFoundException(id.toString, t.name)
      case Some(Versioned(entity, version)) =>
        update(Versioned(modify(entity), version), refreshAfterMutation)
    }
  }

  def update(entity: Versioned[T], refreshAfterMutation: Boolean): Future[Versioned[T]] = {
    val newJson = entity.entity.convertToES(mapping).withESVersion(entity.version)
    val request = new IndexRequest(index.name, t.name)
      .refresh(refreshAfterMutation)
      .id(entity.entity.id.toString)
      .source(newJson.toString)

    debug(s"Update $t.name: $newJson")

    // Call synchronously, useful for debugging: proper stack trace is reported. TODO make config flag.       
    //      val response = client.execute(IndexAction.INSTANCE, request).get
    //      Future.successful(Versioned(entity, response.getVersion.toString))
    preMutate(entity) flatMap { _ =>
      client.execute(request).map(response => Versioned(entity.entity, response.getVersion.toString)) flatMap { mutatedEntity =>
        postMutate(mutatedEntity.entity) map { _ => mutatedEntity }
      }
    }
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

  def query(queryBuilder: QueryBuilder): Future[List[Versioned[T]]] = {
    val request = client.javaClient.prepareSearch(index.name).setTypes(t.name).setQuery(queryBuilder).request
    client.execute(request).map(_.getHits.hits.toList.map { hit =>
      val json = hit.sourceAsString.asJson
      val version = json.getESVersion
      Versioned(json.convertFromES(mapping), version)
    })
  }

  def query(queryStr: String): Future[List[Versioned[T]]] = query(new QueryStringQueryBuilder(queryStr))

  protected def doSearch(filter: FilterBuilder): Future[List[Versioned[T]]] = {
    val request = client.javaClient.prepareSearch(index.name).setTypes(t.name).setFilter(filter).request
    client.execute(request).map(_.getHits.hits.toList.map { hit =>
      val json = hit.sourceAsString.asJson
      val version = json.getESVersion
      Versioned(json.convertFromES(mapping), version)
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

  implicit def toResults(f: Future[SearchResult[T]]) = ESRootDao.toResults(f)
}
