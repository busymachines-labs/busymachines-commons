package com.busymachines.commons.event

import akka.actor.ActorRef
import akka.actor.Actor
import scala.concurrent.Future
import com.busymachines.commons.domain.Id
import scala.reflect.ClassTag
import java.util.UUID

trait BusEvent

trait EventBus {
  def subscribe(f: PartialFunction[BusEvent, Any]): Unit
  def publish(event: BusEvent):Unit
}

