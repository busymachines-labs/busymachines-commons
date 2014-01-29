package com.busymachines.commons.elasticsearch

import org.elasticsearch.client.Client
import spray.json.JsonFormat
import spray.json.JsObject
import com.busymachines.commons.domain.HasId
import com.busymachines.commons.dao.Dao
import scala.concurrent.ExecutionContext
import com.busymachines.commons.dao.SearchCriteria
import scala.concurrent.Future
import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.Logging
import com.busymachines.commons.dao.SearchResult
import com.busymachines.commons.dao.MoreThanOneResultException
import com.busymachines.commons.dao.SearchSort
import com.busymachines.commons.domain.CommonJsonFormats._

abstract class ESDao[T <: HasId[T]: JsonFormat](val typeName: String)(implicit ec: ExecutionContext) extends Dao[T] with Logging {

  /**
   * Escapes a string special ES characters as specified here : {@link http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html#_reserved_characters}
   */
  def escapeQueryText(queryText: String) =
    new StringBuilder(queryText).
      replaceAllLiterally("\\", "\\\\").
      replaceAllLiterally("/", "\\/").
      replaceAllLiterally(" ", "\\ ").
      replaceAllLiterally("+", "\\+").
      replaceAllLiterally("-", "\\-").
      replaceAllLiterally("&&", "\\&&").
      replaceAllLiterally("||", "\\||").
      replaceAllLiterally("!", "\\!").
      replaceAllLiterally("(", "\\(").
      replaceAllLiterally(")", "\\)").
      replaceAllLiterally("{", "\\{").
      replaceAllLiterally("}", "\\}").
      replaceAllLiterally("[", "\\[").
      replaceAllLiterally("]", "\\]").
      replaceAllLiterally("^", "\\^").
      replaceAllLiterally("\"", "\\\"").
      replaceAllLiterally("~", "\\~").
      replaceAllLiterally("*", "\\*").
      replaceAllLiterally("?", "\\?").
      replaceAllLiterally(":", "\\:").toString

  def allSearchTextOrSearchQuery(searchText: Option[String], searchQuery: Option[String]):Option[SearchCriteria[T]] =
  	propertySearchTextOrSearchQuery(ESMapping._all,searchText,searchQuery)

  def allSearchText(searchText: String): SearchCriteria[T] =
    propertySearchText(ESMapping._all,searchText)

  def allSearchQuery(searchQuery: String): SearchCriteria[T] =
    propertySearchQuery(ESMapping._all,searchQuery)
  
  def propertySearchTextOrSearchQuery(property:ESProperty[_,String],searchText: Option[String], searchQuery: Option[String]):Option[SearchCriteria[T]] =
    (searchText, searchQuery) match {
      case (Some(qT), None) => Some(propertySearchText(property,qT))
      case (None, Some(q)) => Some(propertySearchQuery(property,q))
      case (Some(qT), Some(q)) => Some(propertySearchQuery(property,q))
      case _ => None
    }

  def propertySearchText(property:ESProperty[_,String],searchText: String): SearchCriteria[T] =
    propertySearchQuery(property,s"*${escapeQueryText(searchText)}*")

  def propertySearchQuery(property:ESProperty[_,String],searchQuery: String): SearchCriteria[T] =
    (property queryString searchQuery).asInstanceOf[ESSearchCriteria[T]]
  
  def all: SearchCriteria[T] = ESSearchCriteria.All[T]

  def defaultSort: SearchSort = ESSearchSort.asc("_id")

  def searchSingle(criteria: SearchCriteria[T], onMany: List[Versioned[T]] => Versioned[T]): Future[Option[Versioned[T]]] = {
    search(criteria).map(_ match {
      case SearchResult(Nil, _, _) => None
      case SearchResult(first :: Nil, _, _) => Some(first)
      case SearchResult(many, _, _) =>
        try {
          Some(onMany(many))
        } catch {
          case t: MoreThanOneResultException =>
            throw new MoreThanOneResultException(s"Search criteria $criteria returned more than one result and should return at most one result. Database probably inconsistent.")
        }
    })
  }
} 
