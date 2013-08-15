package com.busymachines.commons.test

import org.scalatest.FlatSpec
import akka.actor.ActorSystem
import com.busymachines.commons.event.LocalEventBus
import com.busymachines.commons.event.BusEvent

class LocalEventSystemTests extends FlatSpec  {
  "LocalEventSystemTests" should "send and receive events" in {

    var enpoint1Received = false
    var enpoint2Received = false

    val actorSystem = ActorSystem("aurum")
    val localEventBus = new LocalEventBus(actorSystem)

    localEventBus subscribe {
      event: BusEvent =>
        enpoint1Received = true
    }


    localEventBus subscribe {
      event: BusEvent =>
        enpoint2Received = true

    }

    localEventBus.publish(new BusEvent {})

    Thread.sleep(1000)
    
    assert(enpoint1Received)
    assert(enpoint2Received)
    
  }
}