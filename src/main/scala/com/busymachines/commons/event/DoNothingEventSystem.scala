package com.busymachines.commons.event

import com.busymachines.commons.Logging
import akka.actor.ActorRef
import scala.concurrent.Future

class DoNothingEventSystem extends EventBus with Logging {
  def createEndpoint: EventBusEndpoint = {
    new EventBusEndpoint {
      def onReceive(f: BusEvent => Any) = {}
      def publish(event: BusEvent): Future[Unit] = { 
    	debug(s"Published event $event")  
        Future.successful()
      }
      def receive(event: BusEvent) = {
    	debug(s"Received event $event")  
      }

    }
  }

  def subscribe(endPoint: EventBusEndpoint): ActorRef = {
    debug(s"Subscribed to endpoint but in fact did nothing")
    null
  }

  def publish(event: BusEvent): Future[Unit] = 
    Future.successful(debug(s"Published event $event"))

}