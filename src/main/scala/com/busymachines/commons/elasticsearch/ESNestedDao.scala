package com.busymachines.commons.elasticsearch

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import com.busymachines.commons.dao.IdAlreadyExistsException
import com.busymachines.commons.dao.IdNotFoundException
import com.busymachines.commons.dao.NestedDao
import com.busymachines.commons.dao.Page
import com.busymachines.commons.dao.SearchCriteria
import com.busymachines.commons.dao.SearchResult
import com.busymachines.commons.dao.SearchSort
import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.Id
import com.busymachines.commons.implicits._
import spray.json.JsonFormat
import com.busymachines.commons.dao.Facet

abstract class ESNestedDao[P <: HasId[P], T <: HasId[T] : JsonFormat](typeName : String)(implicit ec: ExecutionContext) extends ESDao[T](typeName) with NestedDao[P, T] {

  protected def parentDao : ESDao[P]

  protected def findEntity(parent : P, id : Id[T]) : Option[T]
  protected def createEntity(parent : P, entity : T) : P
  protected def modifyEntity(parent : P, id : Id[T], found : Found, modify : T => T) : P
  protected def deleteEntity(parent : P, id : Id[T], found : Found) : P
  
  protected class Found {
    var entity : Option[T] = None
    def apply(t : T) : T = { entity = Some(t); t }
  }

  def retrieve(id: Id[T]): Future[Option[Versioned[T]]] = {
    retrieveParent(id) map {
      case Some(Versioned(parent, version)) =>
        findEntity(parent, id).map(Versioned(_, version))
      case None =>
        None
    }
  }

  def retrieveWithParent(id: Id[T]): Future[Option[(Versioned[P], Versioned[T])]] = {
    retrieveParent(id) map {
      case Some(Versioned(parent, version)) =>
        findEntity(parent, id).map(Versioned(parent, version) -> Versioned(_, version))
      case None =>
        None
    }
  }

  def retrieve(ids: Seq[Id[T]]): Future[List[Versioned[T]]] = {
    Future.sequence {
      ids.toList.map { id =>
        retrieveParent(id) map {
          case Some(Versioned(parent, version)) =>
            findEntity(parent, id).map(Versioned(_, version)).toList
          case None =>
            Nil
        }
      }
    }.map(_.flatten)
  }

  def create(id : Id[P], entity: T, refreshAfterMutation : Boolean = true): Future[Versioned[T]] = {
    retrieve(entity.id) flatMap {
      case Some(Versioned(entity, _)) => 
        throw new IdAlreadyExistsException(entity.id.toString, typeName)
      case None =>
	      parentDao.retrieve(id) flatMap {
	    case Some(Versioned(parent, version)) =>
	      val modifiedParent = createEntity(parent, entity)
	      parentDao.update(Versioned(modifiedParent, version), refreshAfterMutation) map {
	        case Versioned(parent, version) => Versioned(entity, version)
	      }
	    case None =>
	      throw new IdNotFoundException(id.toString, parentDao.typeName)   
	    }
	  }
  }
  
  def modify(id : Id[T], refreshAfterMutation : Boolean = true)(f : T => T) : Future[Versioned[T]] = {
    retrieveParent(id) flatMap {
      case None => throw new IdNotFoundException(id.toString, typeName)
      case Some(Versioned(parent, version)) =>
        val found = new Found
        val modifiedParent = modifyEntity(parent, id, found, f)
        found.entity match {
          case Some(entity) =>
            parentDao.update(Versioned(modifiedParent, version), refreshAfterMutation)
            	.map(_.copy(entity = entity))
          case None =>
            throw new IdNotFoundException(id.toString, typeName)
        }
    }
  }
  
  def update(entity: Versioned[T], refreshAfterMutation : Boolean = true): Future[Versioned[T]] = {
    retrieveParent(entity.entity.id) flatMap {
      case None => throw new IdNotFoundException(entity.entity.id.toString, typeName)
      case Some(Versioned(parent, _)) =>
        val found = new Found
        val modifiedParent = modifyEntity(parent, entity.entity.id, found, _ => entity.entity)
        found.entity match {
          case Some(modifiedEntity) =>
          	parentDao.update(Versioned(modifiedParent, entity.version), refreshAfterMutation).map(_.copy(entity = modifiedEntity))
          case None =>
          	throw new IdNotFoundException(entity.entity.id.toString, typeName)
        }
    }
  }

  def delete(id : Id[T], refreshAfterMutation : Boolean = true) : Future[Unit] = {
    retrieveParent(id) flatMap {
      case None => throw new IdNotFoundException(id.toString, typeName)
      case Some(Versioned(parent, version)) =>
        val found = new Found
        val modifiedParent = deleteEntity(parent, id, found)
        found.entity match {
          case Some(_) =>
          	parentDao.update(Versioned(modifiedParent, version), refreshAfterMutation)
          case None =>
          	throw new IdNotFoundException(id.toString, typeName)
        }
    }
  }

  def search(criteria: SearchCriteria[T], page : Page = Page.first, sort:SearchSort = ESSearchSort.asc("_id"), facets : Seq[Facet] = Seq.empty): Future[SearchResult[T]] =
    ???
    
  def onChange(f : Id[T] => Unit) : Unit = 
    ???
}
