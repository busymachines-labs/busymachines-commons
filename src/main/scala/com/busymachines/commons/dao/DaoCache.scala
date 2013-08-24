package com.busymachines.commons.dao

import com.busymachines.commons.domain.HasId
import scala.concurrent.Future
import com.busymachines.commons.domain.Id
import scala.collection.concurrent.TrieMap
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

/**
 * This cache has unbounded size and lifetime. Mainly exists for use inside mutator.
 */
class DaoCache[T <: HasId[T]](val dao: Dao[T])(implicit ec: ExecutionContext) {

  private val _idCache = TrieMap[Id[T], Option[Versioned[T]]]()
  private val _searchCache = TrieMap[(SearchCriteria[T], Page), List[Versioned[T]]]()

  def retrieve(id: Id[T], timeout: Duration): Option[Versioned[T]] =
    _idCache.getOrElseUpdate(id, Await.result(dao.retrieve(id), timeout))

  def retrieve(ids: Seq[Id[T]], timeout: Duration): Seq[Versioned[T]] = {
    val cached = ids.map(id => id -> _idCache.get(id))
    val missingIds = cached.collect { case (id, None) => id }
    val retrieved = missingIds match {
      case Seq() => List()
      case ids => Await.result(dao.retrieve(ids), timeout)
    }
    val result = cached.collect { case (id, Some(v)) => (id -> v) } ++ retrieved.map(v => v.entity.id -> Some(v))
    _idCache ++= result
    result.collect { case (_, Some(v)) => v }
  }

  def search(criteria: SearchCriteria[T], page : Page, timeout: Duration): List[Versioned[T]] =
    _searchCache.getOrElseUpdate((criteria, page), Await.result(dao.search(criteria, page), timeout).map {
      case v @ Versioned(obj, version) => _idCache.getOrElseUpdate(obj.id, Some(v)) match {
        case Some(v) => v
        case None => v
      }
    })
}