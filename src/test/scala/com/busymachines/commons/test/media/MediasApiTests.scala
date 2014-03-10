package com.busymachines.commons.test.media

import com.busymachines.commons.test.AssemblyTestBase
import com.busymachines.prefab.party.api.v1.PartyApiV1Directives
import com.busymachines.prefab.party.api.v1.model.AuthenticationResponse
import com.busymachines.prefab.party.implicits._
import com.busymachines.prefab.media.api.v1
import org.scalatest.FlatSpec
import spray.http._
import spray.json.JsonParser
import com.busymachines.prefab.media.api.v1.MediaApiV1Directives
import com.busymachines.prefab.party.api.v1.model.AuthenticationResponse
import com.busymachines.prefab.party.logic.PartyFixture
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * Created by alex on 2/6/14.
 */
@RunWith(classOf[JUnitRunner])
class MediasApiTests extends FlatSpec with AssemblyTestBase with MediaApiV1Directives with PartyApiV1Directives {

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
    var authResponse: AuthenticationResponse = null
    var mediaId: String = null

    //authentificate first
    Post("/users/authentication", HttpEntity(ContentTypes.`application/json`, userAuthRequestBodyJson)) ~> authenticationApiV1.route ~> check {
      assert(status === StatusCodes.OK)
      assert(body.toString.contains("authToken"))
      authResponse = JsonParser(body.asString).convertTo[AuthenticationResponse]
    }
    Post(s"/medias", HttpEntity(ContentTypes.`application/json`, mediaInputRequestBodyJson)) ~> addHeader("Auth-Token", authResponse.authToken) ~> mediasApiV1.route ~> check {
      mediaId = body.asInstanceOf[HttpEntity].data.asString
    }
    // Can get the complete media object
    Get(s"/medias/$mediaId") ~> addHeader("Auth-Token", authResponse.authToken) ~> mediasApiV1.route ~> check {
      assert(body.toString.contains(mediaId))
    }
    // Can get the raw media object (ie. only it's data)
    Get(s"/medias/$mediaId?raw=true") ~> addHeader("Auth-Token", authResponse.authToken) ~> mediasApiV1.route ~> check {
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
