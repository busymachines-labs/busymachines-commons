package com.kentivo.mdm.db

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import org.elasticsearch.index.query.FilterBuilders.nestedFilter
import org.elasticsearch.index.query.FilterBuilders.termFilter
import com.busymachines.commons.dao.elasticsearch.ESSearchCriteria
import com.busymachines.commons.dao.elasticsearch.EsRootDao
import com.busymachines.commons.dao.elasticsearch.Index
import com.busymachines.commons.domain.Id
import com.kentivo.mdm.domain.DomainJsonFormats
import com.kentivo.mdm.domain.DomainJsonFormats.itemFormat
import com.kentivo.mdm.domain.Item
import com.kentivo.mdm.domain.Property
import spray.json._
import com.busymachines.commons.dao.elasticsearch.Type

class ItemDao2(index2 : Index)(implicit val ec2: ExecutionContext) extends EsRootDao[Item](index2, Type("item", ItemMapping))(scala.concurrent.ExecutionContext.Implicits.global)

case class HasValueForProperty(propertyId : Id[Property], value : Option[String] = None, locale : Option[Option[String]] = None, unit : Option[Unit] = None) extends ESSearchCriteria.Delegate (
  ItemMapping.values / PropertyValueMapping.property === propertyId.toString
  && ItemMapping.values / PropertyValueMapping.property === propertyId.toString
)


class ItemDao(index : Index)(implicit ec: ExecutionContext) extends EsRootDao[Item](index=index, t=Type("item", ItemMapping))(ec = ec) {
//
////  val (indexName, typeName) = es.indexAndTypeOf("item")
//
//  //Await.ready(createItem(Item(Id("0"), repository=Id("0"), owner=Id("0"))), 1 minute)
//      //es.admin().cluster().health(new ClusterHealthRequest().waitForGreenStatus()).actionGet();
//
//  def createItem(item : Item): Future[Unit] = 
//    super.create(item)
//
//  def getItems(ids : Seq[Id[Item]]) : Future[List[Item]] = {
//    val request = ids.foldLeft(new MultiGetRequest){ case (request, id) => request.add(index, doctype, id.toString)}
//    val response = client.execute(request)
//    response map { response =>
//      response.getResponses.toList.flatMap { response =>
//        if (response.isFailed) throw new ESException(index, doctype, s"Couldn't fetch item ${response.getFailure().getId()}: ${response.getFailure().getMessage()}") 
//        else if (!response.getResponse.isExists) Seq.empty
//        else Seq(response.getResponse.getSourceAsString.asJson.withoutESIds.convertTo[Item])
//      }      
//    }
//  }
//    
//  def searchItems(filters : ItemDaoFilter*): Future[List[Item]] = {
//    super.find(filters.map {
//      case RepositoryDaoFilter(id) => Some(termFilter("repository", id))
//      case HasValueForPropertyDaoFilter(propertyId, value, locale, unit) => Some(nestedFilter("values", termFilter("values.property", propertyId)))
//    }:_*)
//  }
//  
//  def findItems(repository: Option[Id[Repository]], isCategory : Option[Boolean] = None): Future[List[Item]] = 
//    super.find(
//      repository.map(termFilter("repository", _)),
//      isCategory.map(termFilter("isCategory", _)))
//  
//  def storeItems(items : Seq[Item]): Future[Seq[(Id[Item], Option[Exception])]] =
//    super.storeAll(items)
//      
//  def findItems2(repository: Option[Id[Repository]], isCategory : Option[Boolean] = None): Future[List[Item]] = {
//    var builder = es.prepareSearch(indexName)
//      .setTypes(doctype)
//      .setSearchType(SearchType.DFS_QUERY_AND_FETCH)
//
//    // Filter by repository
//    builder = repository.map(r => builder.setFilter(termFilter("repository", r))).getOrElse(builder)
//    
//    // Filter by is abstract
//    builder = isCategory.map(isAbstract => builder.setFilter(termFilter("isCategory", isCategory))).getOrElse(builder)
//    
//    val response = es.execute(builder.request)
//    
//    response.map { response =>
//      response.getHits.hits.toList.map { hit =>
//        hit.sourceAsString.asJson.withoutESIds.convertTo[Item]
//      }
//
//    }
//  }
}