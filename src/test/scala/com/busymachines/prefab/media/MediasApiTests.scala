package com.busymachines.prefab.media

import akka.actor.ActorSystem
import com.busymachines.commons.event.LocalEventBus
import com.busymachines.commons.testing.{DefaultTestESConfig, EmptyESTestIndex}
import com.typesafe.config.ConfigFactory
import org.scalatest.FlatSpec
import spray.http._
import com.busymachines.prefab.media.api.v1.MediaApiV1Directives
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import spray.testkit.ScalatestRouteTest

/**
 * Created by Alexandru Matei on 2/6/14.
 */
@RunWith(classOf[JUnitRunner])
class MediasApiTests extends FlatSpec with MediaAssembly with MediaApiV1Directives with ScalatestRouteTest {

  lazy implicit val actorSystem: ActorSystem = ActorSystem("Commons",ConfigFactory.load("tests.conf"))
  lazy implicit val executionContext = actorSystem.dispatcher
  lazy val eventBus = new LocalEventBus(actorSystem)
  lazy val index = EmptyESTestIndex(getClass, DefaultTestESConfig, eventBus)

  val userAuthRequestBodyJson = """
    {
      "loginName": "user1@test.com",
      "password": "test"
    }
                                """
  /* The data payload is "hello world" encoded as base64. */
  val mediaInputRequestBodyJson = """
      {
      "id": "41a6d07c-9c09-44e9-a3f7-4ea3d533727e",
      "mimeType": "text/html",
      "name": "media1.text",
      "data": "data:text/plain;base64,aGVsbG8gd29ybGQ="
      }
                                  """

  "MediasApi" should "create & get raw & complete a media item based on its id" in {
    var mediaId: String = null
    Post(s"/medias", HttpEntity(ContentTypes.`application/json`, mediaInputRequestBodyJson)) ~> mediasApiV1.route ~> check {
      mediaId = body.asInstanceOf[HttpEntity].data.asString
    }
    // Can get the complete media object
    Get(s"/medias/$mediaId") ~> mediasApiV1.route ~> check {
      assert(body.toString.contains(mediaId))
    }
    // Can get the raw media object (ie. only it's data)
    Get(s"/medias/$mediaId?raw=true") ~> mediasApiV1.route ~> check {
      assert(body.asInstanceOf[HttpEntity].data.asString === "hello world")
    }

  }
// TODO test fails occasionally. Need to be investigated!
//  it should "delete a media item based on its id" in {
//    var authResponse: AuthenticationResponse = null
//    var mediaId: String = null
//
//    //authentificate first
//    Post("/users/authentication", HttpEntity(ContentTypes.`application/json`, userAuthRequestBodyJson)) ~> authenticationApiV1.route ~> check {
//      assert(status === StatusCodes.OK)
//      assert(body.toString.contains("authToken"))
//      authResponse = JsonParser(body.asString).convertTo[AuthenticationResponse]
//    }
//    // Can delete media object
//    Delete(s"/medias/$mediaId") ~> addHeader("Auth-Token", authResponse.authToken) ~> mediasApiV1.route ~> check {
//      assert(status === StatusCodes.OK)
//    }
//    // Should not get the complete media object
//    Get(s"/medias/$mediaId") ~> addHeader("Auth-Token", authResponse.authToken) ~> mediasApiV1.route ~> check {
//      assert(status === StatusCodes.InternalServerError)
//    }
//  }
}
