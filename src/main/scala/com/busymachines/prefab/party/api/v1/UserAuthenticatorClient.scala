package com.busymachines.prefab.party.api.v1

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import com.busymachines.commons.Logging
import com.busymachines.prefab.authentication.spray.AuthenticationDirectives
import com.busymachines.prefab.party.api.v1.model.AuthenticationRequest
import com.busymachines.prefab.party.api.v1.model.AuthenticationResponse
import com.busymachines.prefab.party.api.v1.model.PartyApiV1JsonFormats._
import akka.actor.ActorSystem
import spray.client.UnsuccessfulResponseException
import spray.client.pipelining._
import spray.http.HttpRequest
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller
import com.busymachines.commons.concurrent.SerializedFutures
import com.busymachines.commons.cache.SyncRef

class UserAuthenticatorClient(url : String, user : String, password : String)(implicit actorSystem: ActorSystem, ec: ExecutionContext) extends Logging {

  private val callAuthenticate: HttpRequest => Future[AuthenticationResponse] = sendReceive ~> unmarshal[AuthenticationResponse]
  private val authenticateFuture = new SyncRef[Future[String]]

  def apply[A](pipeline : HttpRequest => Future[A], request : HttpRequest) : Future[A] = 
    authenticate(pipeline, request, true)
    
  private def authenticate[A](pipeline : HttpRequest => Future[A], request : HttpRequest, shouldRetry : Boolean) : Future[A] = {
    val future = authenticateFuture.getOrElseUpdate {
      callAuthenticate(Post(url, AuthenticationRequest(user, password))).recover {
        case t : Throwable => 
          error(s"Authentication failure for user $user at $url: ${t.getMessage}")
          authenticateFuture.clear
          throw t
      }.map { response =>
        info(s"Authenticated user $user at $url")
        response.authToken
      }
    }
    future.flatMap { token =>
      pipeline(addHeader(AuthenticationDirectives.tokenKey, token)(request))
    }.recoverWith {
      case e : UnsuccessfulResponseException if e.responseStatus == StatusCodes.Unauthorized =>
        authenticateFuture.clear
        if (shouldRetry) authenticate(pipeline, request, false)
        else throw new Exception(s"Authentication failure at $url, did you specify the correct URL?")
      case t : Throwable =>
        error(s"Error calling ${request.method} ${request.uri}: ${t.getMessage}")
        throw t
    }
  }
}