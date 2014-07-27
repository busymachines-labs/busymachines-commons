package com.busymachines.commons.event

import akka.actor.ActorSystem
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LocalEventSystemTests extends FlatSpec  {
  "LocalEventSystemTests" should "send and receive events" in {

    var enpoint1Received = false
    var enpoint2Received = false

    val actorSystem = ActorSystem("aurum")
    val localEventBus = new LocalEventBus(actorSystem)

    localEventBus subscribe {
      case event: BusEvent =>
        enpoint1Received = true
    }


    localEventBus subscribe {
      case event: BusEvent =>
        enpoint2Received = true

    }

    localEventBus.publish(new BusEvent {})

    Thread.sleep(1000)

    assert(enpoint1Received)
    assert(enpoint2Received)
    
  }
}