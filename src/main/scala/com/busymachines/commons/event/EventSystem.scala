package com.busymachines.commons.event

import akka.actor.ActorRef
import akka.actor.Actor

trait BusEvent

trait EventBus[E] {
  def createEndpoint: EventBusEndpoint[E]
  def subscribe(endPoint: EventBusEndpoint[E]): ActorRef
  def publish(event: E)
}

trait EventBusEndpoint[E] {
  def onReceive(f: E => Any)
  def publish(event: E)
  def receive(event: E)
}

class EventBusEndpointActor[E <: BusEvent](e: EventBusEndpoint[E]) extends Actor {
  def receive = {
    case event: BusEvent => e.receive(event.asInstanceOf[E])
  }
}

class DefaultBusEndpoint[E](eventBus: EventBus[E]) extends EventBusEndpoint[E] {

  private val onReceiveCompletions = scala.collection.mutable.ListBuffer[E => Any]()

  def publish(event: E) =
    eventBus.publish(event)

  def onReceive(f: E => Any) =
    onReceiveCompletions += f

  def receive(event: E) =
    onReceiveCompletions.map(completion => completion(event))
}
