package com.busymachines.commons.event

import akka.actor.ActorRef
import akka.actor.Actor
import scala.concurrent.Future
import com.busymachines.commons.domain.Id
import scala.reflect.ClassTag
import java.util.UUID

trait BusEvent

trait EventBus {
  def subscribe(f: BusEvent => Any): Unit
  def publish(event: BusEvent):Unit
}

object DaoMutationEvent {
  def apply(entityType:Class[_],indexName:String,typeName:String,id:String) = 
    new DaoMutationEvent(entityType.getName.replaceAllLiterally("$", ""),indexName,typeName,id)  
}

class EventBusEndpointActor(f:BusEvent => Any) extends Actor {
  def receive = {
    case event: BusEvent => f(event)
  }
}

case class DaoMutationEvent(entityType:String,indexName:String,typeName:String,id:String) extends BusEvent {
    
  def forEntityType[T](implicit tag: ClassTag[T]) = 
    entityType == tag.runtimeClass.getCanonicalName
  
}
