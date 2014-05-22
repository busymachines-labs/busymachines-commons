package com.busymachines.commons.event

import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.actor.Props
import com.busymachines.commons.Logging
import scala.concurrent.Future
import akka.actor.Actor

class LocalEventBus(actorSystem: ActorSystem) extends EventBus with Logging {
  
  def subscribe(f: PartialFunction[BusEvent, Any]): Unit = {
    val actorRef = actorSystem.actorOf(Props(classOf[LocalEventBusEndpointActor], f))
    actorSystem.eventStream.subscribe(actorRef, classOf[BusEvent])
  }
  
  def publish(event: BusEvent):Unit = {
    actorSystem.eventStream.publish(event)
  }
}

class LocalEventBusEndpointActor(f: PartialFunction[BusEvent, Any]) extends Actor with Logging {
  def receive = {
    case event: BusEvent => f.applyOrElse(event, (_ : Any) => Unit )
  }
}
