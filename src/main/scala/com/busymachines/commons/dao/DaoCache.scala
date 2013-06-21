package com.busymachines.commons.dao

import com.busymachines.commons.domain.HasId
import scala.concurrent.Future
import com.busymachines.commons.domain.Id
import scala.collection.concurrent.TrieMap
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

class DaoCache [T <: HasId[T]](val dao : Dao[T])(implicit ec : ExecutionContext) {
  
  private val _idCache = TrieMap[Id[T], Option[Versioned[T]]]()
  private val _searchCache = TrieMap[SearchCriteria[T], Future[List[Versioned[T]]]]()
  
  def retrieve(id: Id[T], timeout : Duration): Option[Versioned[T]] = 
    _idCache.getOrElseUpdate(id, Await.result(dao.retrieve(id), timeout))
    
  
  def retrieve(ids: Seq[Id[T]], timeout : Duration): Seq[Versioned[T]] = {
    val cached = ids.map(id => id -> _idCache.get(id))
    val missingIds = cached.collect { case (id, None) => id }
    val retrieved = missingIds match {
      case Seq() => List()
      case ids => Await.result(dao.retrieve(ids), timeout)
    }
    val result = cached.collect { case (id, Some(v)) => (id -> v) } ++ retrieved.map(v => v.entity.id -> v)
    _idCache ++= result
    result
  } 
    

  def search(criteria : SearchCriteria[T]): Future[List[Versioned[T]]] =
    _searchCache.getOrElseUpdate(criteria, dao.search(criteria))

}