package com.busymachines.commons.event

import com.busymachines.commons.logging.Logging
import akka.actor.ActorRef
import scala.concurrent.Future

object DoNothingEventSystem extends EventBus{

  def subscribe(f: PartialFunction[BusEvent, Any]): Unit = {
//    debug(s"Subscribed to endpoint but in fact did nothing")
  }

  def publish(event: BusEvent): Unit = {}
//    debug(s"Published event $event")

}