package com.busymachines.commons.test

import org.scalatest.FlatSpec
import akka.actor.ActorSystem
import com.busymachines.commons.event.LocalEventBus
import com.busymachines.commons.event.BusEvent

class EventBusEndpointTests extends FlatSpec  {
  "EventBusEndpoint" should "be able to accept multiple registrations & complete them all" in {

    var enpointReceived1 = false
    var enpointReceived2 = false

    val actorSystem = ActorSystem("aurum")
    val localEventBus = new LocalEventBus[BusEvent](actorSystem)

    val endpoint = localEventBus.createEndpoint

    endpoint onReceive {
      event: BusEvent =>
        enpointReceived1 = true
    }

    endpoint onReceive {
      event: BusEvent =>
        enpointReceived2 = true
    }

    val endpoint2 = localEventBus.createEndpoint

    endpoint2.publish(new BusEvent {})

    Thread.sleep(1000)
    
    assert(enpointReceived1)
    assert(enpointReceived2)
    
  }
}