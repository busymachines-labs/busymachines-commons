//package com.kentivo.mdm.db
//
//import scala.concurrent.ExecutionContext
//import com.busymachines.commons
//import com.kentivo.mdm.commons.implicits._
//import com.kentivo.mdm.domain.Repository
//import com.kentivo.mdm.domain.DomainJsonFormats
//import org.scalastuff.esclient.ESClient
//import org.elasticsearch.action.search.SearchRequest
//import org.elasticsearch.client.Client
//import org.elasticsearch.action.search.SearchType
//import org.elasticsearch.index.query.FilterBuilders
//import scala.concurrent.Future
//import com.kentivo.mdm.commons.ESSourceProvider
//import spray.json.pimpString
//import spray.json.pimpAny
//import org.elasticsearch.action.index.IndexRequest
//import com.kentivo.mdm.commons.ESSource
//import com.kentivo.mdm.domain.Source
//
//class SourceDao(provider: ESSourceProvider)(implicit ec: ExecutionContext) extends DomainJsonFormats {
//
//  val source = provider.SOURCE
//  val index = source.index
//  val doctype = source.doctype
//  val client = source.client
//  
//  def createSource(source: Source): Future[Unit] = {
//    val request = new IndexRequest(index, doctype).
//      id(source.id.toString).
//      source(source.toJson.withESIds.toString)
//    val response = client.execute(request)
//    response.map(_ => Unit)
//  }
//
//  def findSources(repository: Option[Id[Repository]]): Future[List[Source]] = {
//    val builder0 = client.javaClient.prepareSearch(index)
//      .setTypes(doctype)
//      .setSearchType(SearchType.DFS_QUERY_AND_FETCH)
//
//    // Filter by repository
//    val builder = repository match {
//      case Some(id) => builder0.setFilter(FilterBuilders.termFilter("repository", repository))
//      case None => builder0
//    }
//
//    val response = client.execute(builder.request)
//    
//    response.map { response =>
//      response.getHits.hits.toList.map { hit =>
//        hit.sourceAsString.asJson.withoutESIds.convertTo[Source]
//      }
//
//    }
//  }
//}