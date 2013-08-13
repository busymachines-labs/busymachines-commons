package com.busymachines.commons.event

import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.actor.Props
import com.busymachines.commons.Logging
import scala.concurrent.Future

class LocalEventBus[E <: BusEvent](actorSystem: ActorSystem) extends EventBus[E] with Logging {
  def createEndpoint: EventBusEndpoint[E] = {
    val endPoint = new DefaultBusEndpoint[E](this)
    this.subscribe(endPoint)
    endPoint
  }

  def subscribe(endPoint: EventBusEndpoint[E]): ActorRef = {
    val actorRef = actorSystem.actorOf(Props(classOf[EventBusEndpointActor[E]], endPoint))
    actorSystem.eventStream.subscribe(actorRef, classOf[BusEvent])
    actorRef
  }
  
  def publish(event: E):Future[Unit] = {
    debug(s"Published event $event")
    actorSystem.eventStream.publish(event)
    Future.successful()
  }
}
