package com.busymachines.commons.event

import akka.actor.ActorRef
import akka.actor.Actor
import scala.concurrent.Future
import com.busymachines.commons.domain.Id

trait BusEvent

trait EventBus[E<:BusEvent] {
  def createEndpoint: EventBusEndpoint[E]
  def subscribe(endPoint: EventBusEndpoint[E]): ActorRef
  def publish(event: E):Future[Unit]
}

trait EventBusEndpoint[E<:BusEvent] {
  def onReceive(f: E => Any)
  def publish(event: E):Future[Unit]
  def receive(event: E)
}

class EventBusEndpointActor[E <: BusEvent](e: EventBusEndpoint[E]) extends Actor {
  def receive = {
    case event: BusEvent => e.receive(event.asInstanceOf[E])
  }
}

class DefaultBusEndpoint[E<:BusEvent](eventBus: EventBus[E]) extends EventBusEndpoint[E] {

  private val onReceiveCompletions = scala.collection.mutable.ListBuffer[E => Any]()

  def publish(event: E) =
    eventBus.publish(event)

  def onReceive(f: E => Any) =
    onReceiveCompletions += f

  def receive(event: E) =
    onReceiveCompletions.map(completion => completion(event))
}

case class DaoMutationEvent(entityType:String,indexName:String,typeName:String,id:Id[_]) extends BusEvent
