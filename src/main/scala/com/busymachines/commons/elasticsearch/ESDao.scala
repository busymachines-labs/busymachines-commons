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

abstract class ESDao[T <: HasId[T]: JsonFormat](val typeName: String)(implicit ec: ExecutionContext) extends Dao[T] with Logging {

  def searchSingle(criteria: SearchCriteria[T], onMany : List[Versioned[T]] => Versioned[T]): Future[Option[Versioned[T]]] = {
    search(criteria).map(_ match {
      case SearchResult(Nil, _, _) => None
      case SearchResult(first :: Nil, _, _) => Some(first)
      case SearchResult(many, _, _) => 
        try {
          Some(onMany(many))
      } catch {
        case t : MoreThanOneResultException => 
          throw new MoreThanOneResultException(s"Search criteria $criteria returned more than one result and should return at most one result. Database probably inconsistent.")
      }
    })
  }

} 
