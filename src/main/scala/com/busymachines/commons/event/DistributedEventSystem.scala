package com.busymachines.commons.event

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.actorRef2Scala
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.Publish
import akka.contrib.pattern.DistributedPubSubMediator.Subscribe
import akka.contrib.pattern.DistributedPubSubMediator.SubscribeAck
import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.actor.Props
import akka.contrib.pattern.DistributedPubSubMediator
import scala.concurrent.Future

class DistributedEventBus(actorSystem: ActorSystem, topic: String = "all") extends EventBus {

  private val localEndpoints: scala.collection.mutable.Map[EventBusEndpoint, ActorRef] = scala.collection.mutable.Map[EventBusEndpoint, ActorRef]()

  private val subscriber = actorSystem.actorOf(Props(classOf[DistributedSubscriber], topic, {
    event: BusEvent =>
      localEndpoints.map { case (endpoint, actor) => actor ! event }
  }))

  private val publisher = actorSystem.actorOf(Props(classOf[DistributedPublisher], topic))

  def createEndpoint: EventBusEndpoint = {
    val endPoint = new DefaultBusEndpoint(this)
    this.subscribe(endPoint)
    endPoint
  }

  def subscribe(endPoint: EventBusEndpoint): ActorRef = {
    val actorRef = actorSystem.actorOf(Props(classOf[EventBusEndpointActor], endPoint))
    localEndpoints.put(endPoint, actorRef)
    actorRef
  }

  def publish(event: BusEvent):Future[Unit] = {
    publisher ! event
    Future.successful()
  }
}

class DistributedSubscriber(topic: String, onReceiveCompletion: BusEvent => Any) extends Actor with ActorLogging {
  import DistributedPubSubMediator.{ Subscribe, SubscribeAck }
  val mediator = DistributedPubSubExtension(context.system).mediator
  // subscribe to the topic  
  mediator ! Subscribe(topic, self)

  def receive = {
    case SubscribeAck(Subscribe(topic, `self`)) =>
      context become ready
  }

  def ready: Actor.Receive = {
    case e: BusEvent =>
      onReceiveCompletion(e.asInstanceOf[BusEvent])
  }
}

class DistributedPublisher(topic: String) extends Actor {
  import DistributedPubSubMediator.Publish
  // activate the extension
  val mediator = DistributedPubSubExtension(context.system).mediator

  def receive = {
    case e: BusEvent =>
      mediator ! Publish(topic, e)
  }
}
