package com.busymachines.commons.event


trait BusEvent

trait EventBus {
  def subscribe(f: PartialFunction[BusEvent, Any]): Unit
  def publish(event: BusEvent):Unit
}

