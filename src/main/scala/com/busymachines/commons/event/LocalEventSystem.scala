package com.busymachines.commons.event

import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.actor.Props
import com.busymachines.commons.Logging
import scala.concurrent.Future

class LocalEventBus(actorSystem: ActorSystem) extends EventBus with Logging {

  def subscribe(f:BusEvent => Any): Unit = {
    val actorRef = actorSystem.actorOf(Props(classOf[EventBusEndpointActor], f))
    actorSystem.eventStream.subscribe(actorRef, classOf[BusEvent])
  }
  
  def publish(event: BusEvent):Unit = {
    debug(s"Published event $event")
    actorSystem.eventStream.publish(event)
  }
}
