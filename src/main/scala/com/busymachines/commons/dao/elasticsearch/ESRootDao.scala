package com.busymachines.commons.dao.elasticsearch

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
import org.scalastuff.esclient.ESClient
import com.busymachines.commons.Logging
import com.busymachines.commons.dao.IdNotFoundException
import com.busymachines.commons.dao.RootDao
import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.dao.elasticsearch.implicits._
import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.Id
import spray.json.JsonFormat
import spray.json.pimpAny
import spray.json.pimpString
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest
import com.busymachines.commons.dao.SearchCriteria
import org.elasticsearch.action.index.IndexAction

class EsRootDao[T <: HasId[T] :JsonFormat](index : Index, t: Type[T])(implicit ec: ExecutionContext) extends ESDao[T](t.name) with RootDao[T] with Logging {

  val client = index.client
  val mapping = t.mapping

  // Add mapping.
  val mappingConfiguration = t.mapping.mappingConfiguration(t.name)
  debug(mappingConfiguration)
  client.admin.indices.putMapping(new PutMappingRequest(index.name).`type`(t.name).source(mappingConfiguration)).get()

  def retrieve(ids: Seq[Id[T]]): Future[List[Versioned[T]]] = 
    query(QueryBuilders.idsQuery(t.name).addIds(ids.map(id=>id.toString):_*))
  
  
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
  
  def search(criteria : SearchCriteria[T]): Future[List[Versioned[T]]] = {
    criteria match {
      case criteria : ESSearchCriteria[T] =>
        val request = client.javaClient.prepareSearch(index.name).setTypes(t.name).setFilter(criteria.toFilter).request
        client.execute(request).map(_.getHits.hits.toList.map { hit =>
          val json = hit.sourceAsString.asJson
          val version = json.getESVersion
          Versioned(json.convertFromES(mapping), version)
        })
      case _ =>
        throw new Exception("Expected ElasticSearch search criteria")
    }
  }
  
  def create(entity: T, refreshAfterMutation : Boolean): Future[Versioned[T]] = {
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
      
    client.execute(request).map(response => Versioned(entity, response.getVersion.toString))
  }

  def modify(id: Id[T], refreshAfterMutation : Boolean)(modify: T => T): Future[Versioned[T]] = {
    retrieve(id).flatMap {
      case None => throw new IdNotFoundException(id.toString, t.name)
      case Some(Versioned(entity, version)) =>
        update(Versioned(modify(entity), version), refreshAfterMutation)
    }
  }

  def update(entity : Versioned[T], refreshAfterMutation : Boolean) : Future[Versioned[T]] = {
    val newJson = entity.entity.convertToES(mapping).withESVersion(entity.version)
    val request = new IndexRequest(index.name, t.name)
      .refresh(refreshAfterMutation)
      .id(entity.entity.id.toString)
      .source(newJson.toString)
      
      debug(s"Update $t.name: $newJson")
      
// Call synchronously, useful for debugging: proper stack trace is reported. TODO make config flag.       
//      val response = client.execute(IndexAction.INSTANCE, request).get
//      Future.successful(Versioned(entity, response.getVersion.toString))
    client.execute(request).map(response => Versioned(entity.entity, response.getVersion.toString))
  }
  
  def delete(id: Id[T], refreshAfterMutation : Boolean): Future[Unit] = {
    val request = new DeleteRequest(index.name, t.name, id.toString).refresh(refreshAfterMutation)

    client.execute(request) map {
      response =>
        if (response.isNotFound) {
          throw new IdNotFoundException(id.toString, t.name)
        }
    }
  }
  

  def query(queryBuilder : QueryBuilder) : Future[List[Versioned[T]]] = {
    val request = client.javaClient.prepareSearch(index.name).setTypes(t.name).setQuery(queryBuilder).request
    client.execute(request).map(_.getHits.hits.toList.map { hit =>
      val json = hit.sourceAsString.asJson
      val version = json.getESVersion
      Versioned(json.convertFromES(mapping), version)
    })
  }
  
  def query(queryStr : String) : Future[List[Versioned[T]]] = query(new QueryStringQueryBuilder(queryStr))

  protected def search(filter : FilterBuilder) : Future[List[Versioned[T]]] = {
    val request = client.javaClient.prepareSearch(index.name).setTypes(t.name).setFilter(filter).request
    client.execute(request).map(_.getHits.hits.toList.map { hit =>
      val json = hit.sourceAsString.asJson
      val version = json.getESVersion
      Versioned(json.convertFromES(mapping), version)
    })
  }
  

  protected def retrieve(filter : FilterBuilder, error : => String) : Future[Option[Versioned[T]]] = {
    search(filter).map(_ match {
      case Nil => None
      case entity :: Nil => Some(entity)
      case entities => throw new Exception(error)
    })
  }

 def retrieveAll : Future[List[Versioned[T]]] = retrieveAll(FilterBuilders.matchAllFilter())

  def retrieveAll(filter : FilterBuilder) : Future[List[Versioned[T]]] = {
    search(filter).map(_ match {
      case Nil => Nil
      case entities => entities
    })
  }
}