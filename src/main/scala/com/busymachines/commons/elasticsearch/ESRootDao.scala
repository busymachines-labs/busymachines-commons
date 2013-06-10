package com.busymachines.commons.elasticsearch

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.FilterBuilder
import org.scalastuff.esclient.ESClient
import spray.json.JsonFormat
import spray.json.pimpAny
import spray.json.pimpString
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.action.index.IndexAction
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryStringQueryBuilder
import java.io.File
import org.elasticsearch.common.io.stream.DataOutputStreamOutput
import java.io.DataOutputStream
import java.io.FileOutputStream
import org.elasticsearch.index.query.FilterBuilders
import com.busymachines.commons.domain.HasId
import com.busymachines.commons.dao.RootDao
import com.busymachines.commons.domain.Id
import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.dao.NonExistentEntityException
import com.busymachines.commons.Logging
import com.busymachines.commons.elasticsearch.implicits._

abstract class EsRootDao[T <: HasId[T] :JsonFormat](implicit ec: ExecutionContext) extends ESDao[T] with RootDao[T] with Logging {

  val indexName : String
  val client : Client
  
  def retrieve(ids: Seq[Id[T]]): Future[List[Versioned[T]]] = 
    query(QueryBuilders.idsQuery(typeName).addIds(ids.map(id=>id.toString):_*))
  
  
  def retrieve(id: Id[T]): Future[Option[Versioned[T]]] = {
    val request = new GetRequest(indexName, typeName, id.toString)
    client.execute(request).map(response => Option(response.getSourceAsString)) map {
      case None => None
      case Some(source) => 
        val json = source.asJson
        val version = json.getESVersion
        Some(Versioned(json.withoutESIds.convertTo[T], version))
    }
  }
  
  def create(entity: T, refreshAfterMutation : Boolean): Future[Versioned[T]] = {
    val json = entity.toJson.withESIds
    val request = new IndexRequest(indexName, typeName).
      id(entity.id.toString).
      create(true).
      source(json.toString).refresh(refreshAfterMutation)

      debug(s"Create $typeName: $json")
      
// Call synchronously, useful for debugging: proper stack trace is reported. TODO make config flag.       

//      val response = client.execute(IndexAction.INSTANCE, request).get
//      Future.successful(Versioned(entity, response.getVersion.toString))
      
    client.execute(request).map(response => Versioned(entity, response.getVersion.toString))
  }

  def modify(id: Id[T], refreshAfterMutation : Boolean)(modify: T => T): Future[Versioned[T]] = {
    retrieve(id).flatMap {
      case None => throw new NonExistentEntityException(id.toString, typeName)
      case Some(Versioned(entity, version)) =>
        update(Versioned(modify(entity), version), refreshAfterMutation)
    }
  }

  def update(entity : Versioned[T], refreshAfterMutation : Boolean) : Future[Versioned[T]] = {
    val newJson = entity.entity.toJson.withESVersion(entity.version).withESIds
    val request = new IndexRequest(indexName, typeName)
      .refresh(refreshAfterMutation)
      .id(entity.entity.id.toString)
      .source(newJson.toString)
      
      debug(s"Update $typeName: $newJson")
      
// Call synchronously, useful for debugging: proper stack trace is reported. TODO make config flag.       
//      val response = client.execute(IndexAction.INSTANCE, request).get
//      Future.successful(Versioned(entity, response.getVersion.toString))
    client.execute(request).map(response => Versioned(entity.entity, response.getVersion.toString))
  }
  
  def delete(id: Id[T], refreshAfterMutation : Boolean): Future[Unit] = {
    val request = new DeleteRequest(indexName, typeName, id.toString).refresh(refreshAfterMutation)

    client.execute(request) map {
      response =>
        if (response.isNotFound) {
          throw new NonExistentEntityException(id.toString, typeName)
        }
    }
  }
  

  def query(queryBuilder : QueryBuilder) : Future[List[Versioned[T]]] = {
    val request = client.javaClient.prepareSearch(indexName).setTypes(typeName).setQuery(queryBuilder).request
    client.execute(request).map(_.getHits.hits.toList.map { hit =>
      val json = hit.sourceAsString.asJson
      val version = json.getESVersion
      Versioned(json.withoutESIds.convertTo[T], version)
    })
  }
  
  def query(queryStr : String) : Future[List[Versioned[T]]] = query(new QueryStringQueryBuilder(queryStr))

  protected def search(filter : FilterBuilder) : Future[List[Versioned[T]]] = {
    val request = client.javaClient.prepareSearch(indexName).setTypes(typeName).setFilter(filter).request
    client.execute(request).map(_.getHits.hits.toList.map { hit =>
      val json = hit.sourceAsString.asJson
      val version = json.getESVersion
      Versioned(json.withoutESIds.convertTo[T], version)
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