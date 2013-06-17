package com.kentivo.mdm.api

import com.busymachines.commons.Logging
import com.busymachines.commons.implicits._
import com.typesafe.config.ConfigFactory
import akka.actor.Actor
import akka.actor.Props
import spray.can.server.SprayCanHttpServerApp
import spray.routing.HttpServiceActor
import com.kentivo.mdm.logic.Authentication
import com.kentivo.mdm.SystemAssembly
import akka.actor.ActorSystem
import spray.routing.RequestContext
import akka.actor.ActorContext

/**
 * Create a server for API project using spray-can.
 */
class ApiServer(actorSystem: ActorSystem)(f : ActorContext => RequestContext => Unit) extends SprayCanHttpServerApp with Logging {

  override lazy val system = actorSystem

  lazy val actor = system.actorOf(Props(new Actor with HttpServiceActor { actor =>
    def receive = runRoute(f(context))
  }), "api-service")

  def start = {

    val config = ConfigFactory.load
    val interface = config.getString("com.kentivo.mdm.api.interface")
    val port = config.getString("com.kentivo.mdm.api.port").toInt
    
    newHttpServer(actor) ! Bind(interface = interface, port = port)
  }

}
