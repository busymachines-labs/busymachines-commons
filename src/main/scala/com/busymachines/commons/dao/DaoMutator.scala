package com.busymachines.commons.dao

import scala.collection.mutable
import scala.concurrent.Await.result
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Future.sequence
import scala.concurrent.Promise
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag
import scala.util.Failure
import scala.util.Success

import com.busymachines.commons.dao.Versioned.toEntity
import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.Id

object DaoMutator {
  def apply[T <: HasId[T] : ClassTag](dao : RootDao[T])(implicit ec : ExecutionContext) = new RootDaoMutator[T](dao)
  def apply[P <: HasId[P], T <: HasId[T] : ClassTag](dao : NestedDao[P, T])(implicit ec : ExecutionContext) = new NestedDaoMutator[P, T](dao)
}

abstract class DaoMutator[T <: HasId[T] :ClassTag](dao : Dao[T])(implicit classTag : ClassTag[T], ec : ExecutionContext) {
  
  protected val _changedEntities = mutable.Map[Id[T], Versioned[T]]()
  protected val _createdEntities = mutable.Map[Id[T], String]()

  val changedEntities: scala.collection.Map[Id[T], T] = 
    _changedEntities.mapValues(_.entity)

  def retrieve(id: Id[T], timeout : Duration): Option[T] = 
    _changedEntities.get(id).map(Some(_)).getOrElse(result(dao.retrieve(id), timeout)).map(_.entity)

  def retrieve(entityIds : Seq[Id[T]], timeout : Duration) : Seq[T] = 
    entityIds.flatMap(id => _changedEntities.get(id).map(Some(_)).getOrElse(result(dao.retrieve(id), timeout))).map(_.entity)
  
  def search(criteria: SearchCriteria[T], timeout : Duration) : List[T] = {
    val entities = result(dao.search(criteria), timeout)
    entities.map(entity => _changedEntities.get(entity.id).getOrElse(entity)).map(_.entity)
  }
    
  def modify(id: Id[T], timeout : Duration)(f: T => T): T = {
    val entity = retrieve(id, timeout).getOrElse(throw new IdNotFoundException(id.toString, classTag.runtimeClass.getSimpleName))
    val modified = f(entity)
    if (entity != modified) {
      _changedEntities += (id -> Versioned(modified, ""))
    }
    modified
  }
  
  def write(timeout : Duration, reindex : Boolean = true) : Seq[Versioned[T]] = {
    val writes = for (versionedEntity <- _changedEntities.values) 
      yield (versionedEntity.entity.id, _createdEntities.get(versionedEntity.entity.id) match {
        case Some(parentId) => createEntity(parentId, versionedEntity.entity)
        case None => dao.update(versionedEntity, false)
      })
    val futures = for ((id, future) <- writes) yield {
      val promise = Promise[(Option[Versioned[T]], Option[(Id[T], Throwable)])]()
      future.onComplete {
        case Success(s) => promise.success((Some(s), None))
        case Failure(f) => promise.success(None, Some((id, f)))
      }
      promise.future
    }
    result(sequence(futures), timeout).toSeq.flatMap {
      case (Some(versionedEntity), _) =>
        _changedEntities.remove(versionedEntity.entity.id)
        Some(versionedEntity)
      case (_, Some((id, t))) =>
        None
      case _ =>
        None
    }
  }
  
  def clear {
    _changedEntities.clear
    _createdEntities.clear
  }
  
  protected def createEntity(parentId : String, entity : T) : Future[Versioned[T]]
}  

class RootDaoMutator[T <: HasId[T] : ClassTag](dao : RootDao[T])(implicit ec : ExecutionContext) extends DaoMutator[T](dao){
  
  def getOrCreate(id: Id[T], create : => T, timeout : Duration): T = {
    _changedEntities.getOrElse(id, result(dao.retrieve(id), timeout) match {
      case Some(versionedInstance) => versionedInstance
      case None => Versioned(create, "")
    }).entity
  }
  
  protected def createEntity(parentId : String, entity : T) =
    dao.create(entity, false)
}

class NestedDaoMutator[P <: HasId[P], T <: HasId[T] : ClassTag](dao : NestedDao[P, T])(implicit ec : ExecutionContext) extends DaoMutator[T](dao){
  
  def getOrCreate(id: Id[T], parent : Id[P], create : => T, timeout : Duration): T = {
    _changedEntities.getOrElse(id, result(dao.retrieve(id), timeout) match {
      case Some(entity) => entity
      case None => Versioned(create, "")
    }).entity
  }
  
  protected def createEntity(parentId : String, entity : T) =
    dao.create(Id(parentId), entity, false)
}
