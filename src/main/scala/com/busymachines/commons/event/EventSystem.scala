package com.busymachines.commons.event

import akka.actor.ActorRef
import akka.actor.Actor
import scala.concurrent.Future
import com.busymachines.commons.domain.Id
import scala.reflect.ClassTag
import java.util.UUID

trait BusEvent

trait EventBus {
  def createEndpoint: EventBusEndpoint
  def subscribe(endPoint: EventBusEndpoint): ActorRef
  def publish(event: BusEvent):Future[Unit]
}

trait EventBusEndpoint {
  def onReceive(f: BusEvent => Any)
  def publish(event: BusEvent):Future[Unit]
  def receive(event: BusEvent)
}

class EventBusEndpointActor(e: EventBusEndpoint) extends Actor {
  def receive = {
    case event: BusEvent => e.receive(event.asInstanceOf[BusEvent])
  }
}

class DefaultBusEndpoint(eventBus: EventBus) extends EventBusEndpoint {

  private val onReceiveCompletions = scala.collection.mutable.ListBuffer[BusEvent => Any]()

  def publish(event: BusEvent) =
    eventBus.publish(event)

  def onReceive(f: BusEvent => Any) =
    onReceiveCompletions += f

  def receive(event: BusEvent) =
    onReceiveCompletions.map(completion => completion(event))
}

object DaoMutationEvent {
  def apply(entityType:Class[_],indexName:String,typeName:String,id:String) = 
    new DaoMutationEvent(entityType.getName.replaceAllLiterally("$", ""),indexName,typeName,id)  
}

case class DaoMutationEvent(entityType:String,indexName:String,typeName:String,id:String) extends BusEvent {
    
  def forEntityType[T](implicit tag: ClassTag[T]) = 
    entityType == tag.runtimeClass.getCanonicalName
  
}
