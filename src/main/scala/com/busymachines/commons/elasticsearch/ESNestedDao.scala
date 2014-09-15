package com.busymachines.commons.elasticsearch

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import com.busymachines.commons.dao._
import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.Id
import com.busymachines.commons.Implicits._
import spray.json.JsonFormat

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

  def retrieve(id: Id[T]): Future[Option[T]] = 
    retrieveVersioned(id).map(_.map(_.entity))

  def retrieveVersioned(id: Id[T]): Future[Option[Versioned[T]]] = {
    retrieveParent(id) map {
      case Some(Versioned(parent, version)) =>
        findEntity(parent, id).map(Versioned(_, version))
      case None =>
        None
    }
  }

  def retrieveWithParent(id: Id[T]): Future[Option[(P, T)]] = 
    retrieveWithParentVersioned(id).map(_.map(t => t._1.entity -> t._2.entity))
    
  def retrieveWithParentVersioned(id: Id[T]): Future[Option[(Versioned[P], Versioned[T])]] = {
    retrieveParent(id) map {
      case Some(Versioned(parent, version)) =>
        findEntity(parent, id).map(Versioned(parent, version) -> Versioned(_, version))
      case None =>
        None
    }
  }

  def retrieve(ids: Seq[Id[T]]): Future[List[T]] = 
    retrieveVersioned(ids).map(_.map(_.entity))

  def retrieveVersioned(ids: Seq[Id[T]]): Future[List[Versioned[T]]] = {
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

  def create(id : Id[P], entity: T, refresh : Boolean = true): Future[T] =
    createVersioned(id, entity, refresh).map(_.entity)

  def createVersioned(id : Id[P], entity: T, refresh : Boolean = true): Future[Versioned[T]] = {
    retrieveVersioned(entity.id) flatMap {
      case Some(Versioned(entity, _)) => 
        throw new IdAlreadyExistsException(entity.id.toString, typeName)
      case None =>
	      parentDao.retrieveVersioned(id) flatMap {
    	    case Some(Versioned(parent, version)) =>
    	      val modifiedParent = createEntity(parent, entity)
    	      parentDao.updateVersioned(Versioned(modifiedParent, version), refresh) map {
    	        case Versioned(parent, version) => Versioned(entity, version)
    	      }
    	    case None =>
    	      throw new IdNotFoundException(id.toString, parentDao.typeName)   
  	    }
	  }
  }
  
  def modify(id : Id[T], refresh : Boolean = true)(f : T => T) : Future[T] =
    modifyVersioned(id, refresh)(v => f(v.entity)).map(_.entity)

  def modifyVersioned(id : Id[T], refresh : Boolean = true)(f : Versioned[T] => T) : Future[Versioned[T]] = {
    retrieveParent(id) flatMap {
      case None => throw new IdNotFoundException(id.toString, typeName)
      case Some(Versioned(parent, version)) =>
        val found = new Found
        val modifiedParent = modifyEntity(parent, id, found, p => f(Versioned(p, version)))
        found.entity match {
          case Some(entity) =>
            parentDao.updateVersioned(Versioned(modifiedParent, version), refresh)
            	.map(_.copy(entity = entity))
          case None =>
            throw new IdNotFoundException(id.toString, typeName)
        }
    }
  }
  
  def modifyOptionally(id : Id[T], refresh : Boolean = true)(modify : T => Option[T]) : Future[T] =
    modifyOptionallyVersioned(id, refresh)(v => modify(v.entity).map(Versioned(_, v.version))).map(_.entity)

  def modifyOptionallyVersioned(id : Id[T], refresh : Boolean = true)(f : Versioned[T] => Option[Versioned[T]]) : Future[Versioned[T]] = {
    retrieveParent(id) flatMap {
      case None => throw new IdNotFoundException(id.toString, typeName)
      case Some(Versioned(parent, version)) =>
        val found = new Found
        var modified = false
        val modifiedParent = modifyEntity(parent, id, found, e => f(Versioned(e, version)) match {
          case Some(e2) => e2
          case None =>
            modified = true
            e
        })
        found.entity match {
          case Some(entity) =>
            if (modified) 
              parentDao.updateVersioned(Versioned(modifiedParent, version), refresh)
                .map(_.copy(entity = entity))
            else
              Future.successful(Versioned(entity, version))  
          case None =>
            throw new IdNotFoundException(id.toString, typeName)
        }
    }
  }
  
  def update(entity: Versioned[T], refresh : Boolean = true): Future[T] =
    updateVersioned(entity, refresh).map(_.entity)

  def updateVersioned(entity: Versioned[T], refresh : Boolean = true): Future[Versioned[T]] = {
    retrieveParent(entity.entity.id) flatMap {
      case None => throw new IdNotFoundException(entity.entity.id.toString, typeName)
      case Some(Versioned(parent, v)) =>
        val found = new Found
        val modifiedParent = modifyEntity(parent, entity.entity.id, found, _ => entity.entity)
        found.entity match {
          case Some(modifiedEntity) =>
           	parentDao.updateVersioned(Versioned(modifiedParent, entity.version), refresh).map(_.copy(entity = modifiedEntity))
          case None =>
          	throw new IdNotFoundException(entity.entity.id.toString, typeName)
        }
    }
  }

  def delete(id : Id[T], refresh : Boolean = true) : Future[Unit] =
    deleteVersioned(id, refresh).map(_ => Unit)

  def deleteVersioned(id : Id[T], refresh : Boolean = true) : Future[Long] = {
    retrieveParent(id) flatMap {
      case None => throw new IdNotFoundException(id.toString, typeName)
      case Some(Versioned(parent, version)) =>
        val found = new Found
        val modifiedParent = deleteEntity(parent, id, found)
        found.entity match {
          case Some(_) =>
          	parentDao.update(Versioned(modifiedParent, version), refresh).map(_ => version)
          case None =>
          	throw new IdNotFoundException(id.toString, typeName)
        }
    }
  }

  def search(criteria: SearchCriteria[T], page : Page = Page.first, sort:SearchSort = ESSearchSort.asc("_id"), facets : Seq[Facet] = Seq.empty): Future[SearchResult[T]] =
    ???

  def searchVersioned(criteria: SearchCriteria[T], page : Page = Page.first, sort:SearchSort = ESSearchSort.asc("_id"), facets : Seq[Facet] = Seq.empty): Future[VersionedSearchResult[T]] =
  		???

  def find(criteria: SearchCriteria[T], page: Page = Page.first, sort: SearchSort = defaultSort): Future[List[T]] =
    ???

  def findVersioned(criteria: SearchCriteria[T], page: Page = Page.first, sort: SearchSort = defaultSort): Future[List[Versioned[T]]] =
    ???


  def onChange(f : Id[T] => Unit) : Unit = 
    ???
}
