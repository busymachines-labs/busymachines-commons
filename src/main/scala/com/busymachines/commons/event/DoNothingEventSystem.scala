package com.busymachines.commons.event

import com.busymachines.commons.logging.Logging

object DoNothingEventSystem extends EventBus with Logging {

  def subscribe(f: PartialFunction[BusEvent, Any]): Unit = {
    logger.debug(s"Subscribed to endpoint but in fact did nothing")
  }

  def publish(event: BusEvent): Unit = {
    logger.debug(s"Published event $event")
  }

}