package com.busymachines.commons.event

import com.busymachines.commons.Logging
import akka.actor.ActorRef
import scala.concurrent.Future

class DoNothingEventSystem extends EventBus with Logging {

  def subscribe(f:BusEvent => Any): Unit = {
    debug(s"Subscribed to endpoint but in fact did nothing")
  }

  def publish(event: BusEvent): Unit = 
    debug(s"Published event $event")

}