package com.busymachines.commons.dao

import com.busymachines.commons.domain.HasId
import scala.concurrent.Future
import com.busymachines.commons.domain.Id
import scala.collection.concurrent.TrieMap
import scala.concurrent.Await
import scala.concurrent.ExecutionContext

class DaoCache [T <: HasId[T]](val dao : Dao[T])(implicit ec : ExecutionContext) {
  
  private val _idCache = TrieMap[Id[T], Future[Option[Versioned[T]]]]()
  private val _searchCache = TrieMap[Seq[SearchCriteria[T]], List[T]]()
  
  def retrieve(id: Id[T]): Future[Option[Versioned[T]]] = 
    _idCache.getOrElseUpdate(id, dao.retrieve(id))
    
  
  def retrieve(ids: Seq[Id[T]]): Future[List[Versioned[T]]] = 
    Future.sequence(ids.toList.map(retrieve(_))).map(_.flatMap(_.toSeq))
    
//    val cached = ids.map(id => id -> _idCache.get(id))
//    val missingIds = cached.collect { case (id, None) => id }
//    val readItems = missingIds match {
//      case Seq() => Future.successful(Seq())
//      case ids => dao.retrieve(ids)
//    }
//    val result = cached.collect { case (_, Some(item)) => item } ++ readItems
//    _itemCache ++= result.map(item => item.id -> item)
//    result
//  } 
    

  def search(criteria : SearchCriteria[T]): Future[List[Versioned[T]]]

}