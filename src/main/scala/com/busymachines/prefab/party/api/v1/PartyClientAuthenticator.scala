package com.busymachines.prefab.party.api.v1

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import com.busymachines.commons.Logging
import com.busymachines.commons.implicits.richStringSeq
import com.busymachines.prefab.authentication.spray.AuthenticationDirectives
import com.busymachines.prefab.party.api.v1.model.AuthenticationRequest
import com.busymachines.prefab.party.api.v1.model.AuthenticationResponse
import com.busymachines.prefab.party.api.v1.model.PartyApiJsonFormats._
import akka.actor.ActorSystem
import spray.client.UnsuccessfulResponseException
import spray.client.pipelining._
import spray.http.HttpRequest
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller

class PartyClientAuthenticator(url : String, user : String, password : String)(implicit actorSystem: ActorSystem, ec: ExecutionContext) extends Logging {

  private val callAuthenticate: HttpRequest => Future[AuthenticationResponse] = sendReceive ~> unmarshal[AuthenticationResponse]
  private var authenticationToken : Option[String] = None

  def apply[A](pipeline : HttpRequest => Future[A], request : HttpRequest) : Future[A] = 
    authenticate(pipeline, request, true)
    
  private def authenticate[A](pipeline : HttpRequest => Future[A], request : HttpRequest, shouldRetry : Boolean) : Future[A] = { 
    authenticationToken match {
      case Some(token) => 
        val s : HttpRequest => HttpRequest= addHeader(AuthenticationDirectives.tokenKey, token)
        pipeline(s(request)).recoverWith {
          case e : UnsuccessfulResponseException if e.responseStatus == StatusCodes.Unauthorized =>
            authenticationToken = None
            if (shouldRetry) authenticate(pipeline, request, false)
            else throw new Exception(s"Authentication failure at $url, did you specify the correct URL?")
          case t : Throwable =>
            throw t
        }
      case None => 
        callAuthenticate(Post(url, AuthenticationRequest(user, password))).recover {
          case t : Throwable => 
            error(s"Authentication failure for user $user at $url: ${t.getMessage}")
            throw t
          }.flatMap { response =>
            authenticationToken = Some(response.authToken)
            authenticate(pipeline, request, false)
          }
    }
  }
}