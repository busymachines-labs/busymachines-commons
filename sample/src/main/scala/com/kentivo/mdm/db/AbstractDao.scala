//package com.kentivo.mdm.db
//
//import scala.concurrent.ExecutionContext
//import scala.concurrent.Future
//import org.elasticsearch.action.index.IndexRequest
//import org.elasticsearch.action.search.SearchType
//import org.elasticsearch.index.query.FilterBuilder
//import com.kentivo.mdm.commons.ESSource
//import com.kentivo.mdm.commons.HasId
//import com.kentivo.mdm.commons.implicits.ESRichJsValue
//import spray.json.JsonFormat
//import spray.json.pimpAny
//import spray.json.pimpString
//import com.busymachines.commons
//import org.elasticsearch.action.get.GetRequest
//import org.elasticsearch.action.bulk.BulkRequest
//
//class AbstractDao[A <: HasId[A]](source: ESSource) {
//  val client = source.client
//  val index = source.index
//  val doctype = source.doctype
//  
//  protected def get(id : Id[A])(implicit ec: ExecutionContext, format: JsonFormat[A]) : Future[Option[A]] = {
//    val request = new GetRequest(index, doctype, id.toString)
//    client.execute(request).map(response => Option(response.getSourceAsString).map(_.asJson.withoutESIds.convertTo[A])) 
//  }
//  
//  protected def create(obj: A)(implicit ec: ExecutionContext, format: JsonFormat[A]): Future[Unit] = {
//    val request = new IndexRequest(index, doctype).
//      id(obj.id.toString).
//      source(obj.toJson.withESIds.toString)
//    val response = client.execute(request)
//    response.map(_ => Unit)
//  }
//
//  protected def storeAll(seq : Seq[A])(implicit ec: ExecutionContext, format: JsonFormat[A]): Future[Seq[(Id[A], Option[Exception])]] = {
//    val requests = seq.map { obj =>  new IndexRequest(index, doctype).
//      id(obj.id.toString).
//      source(obj.toJson.withESIds.toString)
//    }
//    val request = new BulkRequest().add(requests:_*)
//    client.execute(request).map(_.getItems.toSeq.map(item => (Id[A](item.getId), if (item.isFailed) Some(new Exception(item.getFailureMessage)) else None)))
//  }
//  
//  protected type Filter[A] = (Option[A], A => FilterBuilder)
//
//  protected def find(filters: Option[FilterBuilder]*)(implicit ec: ExecutionContext, format: JsonFormat[A]): Future[List[A]] = {
//      
//    val builder = filters.foldLeft(client.javaClient.prepareSearch(index)
//      .setTypes(doctype)
//      .setSearchType(SearchType.DFS_QUERY_AND_FETCH)) {
//      case (builder, Some(filter)) => builder.setFilter(filter)
//      case (builder, None) => builder
//    }   
//      
//    val response = client.execute(builder.request)
//
//    response.map { response =>
//      response.getHits.hits.toList.map { hit =>
//        hit.sourceAsString.asJson.withoutESIds.convertTo[A]
//      }
//
//    }
//  }
//}