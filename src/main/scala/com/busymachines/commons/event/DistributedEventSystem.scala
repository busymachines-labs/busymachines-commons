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

class DistributedEventBus[E <:BusEvent](actorSystem: ActorSystem, topic: String = "all") extends EventBus[E] {

  private val localEndpoints: scala.collection.mutable.Map[EventBusEndpoint[E], ActorRef] = scala.collection.mutable.Map[EventBusEndpoint[E], ActorRef]()

  private val subscriber = actorSystem.actorOf(Props(classOf[DistributedSubscriber[E]], topic, {
    event: BusEvent =>
      localEndpoints.map { case (endpoint, actor) => actor ! event }
  }))

  private val publisher = actorSystem.actorOf(Props(classOf[DistributedPublisher[E]], topic))

  def createEndpoint: EventBusEndpoint[E] = {
    val endPoint = new DefaultBusEndpoint(this)
    this.subscribe(endPoint)
    endPoint
  }

  def subscribe(endPoint: EventBusEndpoint[E]): ActorRef = {
    val actorRef = actorSystem.actorOf(Props(classOf[EventBusEndpointActor[E]], endPoint))
    localEndpoints.put(endPoint, actorRef)
    actorRef
  }

  def publish(event: E):Future[Unit] = {
    publisher ! event
    Future.successful()
  }
}

class DistributedSubscriber[E <: BusEvent](topic: String, onReceiveCompletion: E => Any) extends Actor with ActorLogging {
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
      onReceiveCompletion(e.asInstanceOf[E])
  }
}

class DistributedPublisher[E](topic: String) extends Actor {
  import DistributedPubSubMediator.Publish
  // activate the extension
  val mediator = DistributedPubSubExtension(context.system).mediator

  def receive = {
    case e: BusEvent =>
      mediator ! Publish(topic, e)
  }
}
