package com.busymachines.commons.elasticsearch

import spray.json.JsonFormat
import com.busymachines.commons.domain.{Money, HasId}
import com.busymachines.commons.Implicits._
import com.busymachines.commons.dao._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import com.busymachines.commons.logging.Logging

object MoneyMapping extends ESMapping[Money] {
  val currency = "currency" :: String
  val amount = "amount" :: Double
}

abstract class ESDao[T <: HasId[T]: JsonFormat](val typeName: String)(implicit ec: ExecutionContext) extends Dao[T] with Logging {

  def all: SearchCriteria[T] = ESSearchCriteria.All[T]()

  def defaultSort: SearchSort = ESSearchSort.asc("_id")

  def findSingle(criteria: SearchCriteria[T], onMany: List[T] => T): Future[Option[T]] =
    findSingleVersioned(criteria, v => Versioned(onMany(v.map(_.entity)), -1)).map(_.map(_.entity))
    
  def findSingleVersioned(criteria: SearchCriteria[T], onMany: List[Versioned[T]] => Versioned[T]): Future[Option[Versioned[T]]] = {
    searchVersioned(criteria).map {
      case VersionedSearchResult(Nil, _, _) => None
      case VersionedSearchResult(first :: Nil, _, _) => Some(first)
      case VersionedSearchResult(many, _, _) =>
        try {
          Some(onMany(many))
        } catch {
          case t: MoreThanOneResultException =>
            throw new MoreThanOneResultException(s"Search criteria $criteria returned more than one result and should return at most one result. Database probably inconsistent. The returned results were :$many")
        }
    }
  }
} 
