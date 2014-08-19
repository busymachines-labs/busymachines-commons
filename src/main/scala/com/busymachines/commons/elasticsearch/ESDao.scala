package com.busymachines.commons.elasticsearch

import spray.json.JsonFormat
import com.busymachines.commons.domain.{Money, HasId}
import com.busymachines.commons.Implicits._
import com.busymachines.commons.dao.Dao
import scala.concurrent.ExecutionContext
import com.busymachines.commons.dao.SearchCriteria
import scala.concurrent.Future
import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.logger.Logging
import com.busymachines.commons.dao.SearchResult
import com.busymachines.commons.dao.MoreThanOneResultException
import com.busymachines.commons.dao.SearchSort

object MoneyMapping extends ESMapping[Money] {
  val currency = "currency" :: String
  val amount = "amount" :: Double
}

abstract class ESDao[T <: HasId[T]: JsonFormat](val typeName: String)(implicit ec: ExecutionContext) extends Dao[T] with Logging {

  def all: SearchCriteria[T] = ESSearchCriteria.All[T]()

  def defaultSort: SearchSort = ESSearchSort.asc("_id")

  def searchSingle(criteria: SearchCriteria[T], onMany: List[Versioned[T]] => Versioned[T]): Future[Option[Versioned[T]]] = {
    search(criteria).map {
      case SearchResult(Nil, _, _) => None
      case SearchResult(first :: Nil, _, _) => Some(first)
      case SearchResult(many, _, _) =>
        try {
          Some(onMany(many))
        } catch {
          case t: MoreThanOneResultException =>
            throw new MoreThanOneResultException(s"Search criteria $criteria returned more than one result and should return at most one result. Database probably inconsistent. The returned results were :$many")
        }
    }
  }
} 
