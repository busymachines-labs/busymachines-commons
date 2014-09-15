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
 * TODO make async
 */
class DaoCache[T <: HasId[T]](dao: Dao[T], autoInvalidate : Boolean = false, onInvalidate : Id[T] => Unit = (id : Id[T]) => ())(implicit ec: ExecutionContext) {

  private val maxSearchResultSize = 1000
  private val _idCache = TrieMap.empty[Id[T], Option[Versioned[T]]] 
  private val _searchCache = TrieMap.empty[(SearchCriteria[T], Page), List[Versioned[T]]]

  if (autoInvalidate) 
    dao.onChange(invalidate)
  
  def retrieve(id: Id[T], timeout: Duration, onRetrieve : T => T = t => t): Option[Versioned[T]] =
    _idCache.getOrElseUpdate(id, {
      Await.result(dao.retrieveVersioned(id), timeout) map {
        case Versioned(entity, version) => 
          Versioned(onRetrieve(entity), version)
      }
    })

  def retrieve(ids: Seq[Id[T]], timeout: Duration): Seq[Versioned[T]] = {
    val cached = ids.map(id => id -> _idCache.get(id))
    val missingIds = cached.collect { case (id, None) => id }
    val retrieved = missingIds match {
      case Seq() => List()
      case ids => Await.result(dao.retrieveVersioned(ids), timeout)
    }
    val result = cached.collect { case (id, Some(v)) => id -> v } ++ retrieved.map(v => v.entity.id -> Some(v))
    _idCache ++= result
    result.collect { case (_, Some(v)) => v }
  }

  def search(criteria: SearchCriteria[T], page : Page = Page.all, timeout: Duration, maxSearchResultSize: Int = this.maxSearchResultSize): List[Versioned[T]] = {
    val key = (criteria, page)
    val result = _searchCache.getOrElseUpdate(key, Await.result(dao.searchVersioned(criteria, page), timeout).result.map {
        case v @ Versioned(obj, version) => _idCache.getOrElseUpdate(obj.id, Some(v)) match {
          case Some(v) => v
          case None => v
        }
      })
    if (result.size > maxSearchResultSize)
      _searchCache.remove(key)
    result
  }
    
  def invalidate(id: Id[T]) : Unit = {
    _idCache.remove(id)
    _searchCache.clear()
    onInvalidate(id)
  }
}