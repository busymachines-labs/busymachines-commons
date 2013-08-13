package com.busymachines.commons.event

import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.actor.Props
import com.busymachines.commons.Logging
import scala.concurrent.Future

class LocalEventBus(actorSystem: ActorSystem) extends EventBus with Logging {
  def createEndpoint: EventBusEndpoint = {
    val endPoint = new DefaultBusEndpoint(this)
    this.subscribe(endPoint)
    endPoint
  }

  def subscribe(endPoint: EventBusEndpoint): ActorRef = {
    val actorRef = actorSystem.actorOf(Props(classOf[EventBusEndpointActor], endPoint))
    actorSystem.eventStream.subscribe(actorRef, classOf[BusEvent])
    actorRef
  }
  
  def publish(event: BusEvent):Future[Unit] = {
    debug(s"Published event $event")
    actorSystem.eventStream.publish(event)
    Future.successful()
  }
}
