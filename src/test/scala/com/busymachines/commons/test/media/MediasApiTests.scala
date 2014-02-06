package com.busymachines.commons.test.media

import com.busymachines.commons.test.AssemblyTestBase
import com.busymachines.prefab.party.api.v1.PartyApiV1Directives
import com.busymachines.prefab.party.api.v1.model.AuthenticationResponse
import com.busymachines.prefab.media.api.v1
import org.scalatest.FlatSpec
import spray.http._
import spray.json.JsonParser
import com.busymachines.prefab.media.api.v1.MediaApiV1Directives
import com.busymachines.prefab.party.api.v1.model.AuthenticationResponse
import com.busymachines.prefab.party.logic.PartyFixture

/**
 * Created by alex on 2/6/14.
 */
class MediasApiTests extends FlatSpec with AssemblyTestBase with MediaApiV1Directives with PartyApiV1Directives{

  val userAuthRequestBodyJson = """
    {
      "loginName": "user1@test.com",
      "password": "test"
    }
                                """

  val mediaInputRequestBodyJson="""
      {
      "id": "41a6d07c-9c09-44e9-a3f7-4ea3d533727e",
      "mimeType": "text/html",
      "name": "media1.text",
      "data": "data:;base64,"
      }
                           """
  
  var authResponse:AuthenticationResponse=null;
  var mediaId:String=null
"MediasApi" should "post a new media item" in{

  //authentificate first
  Post("/users/authentication", HttpEntity(ContentTypes.`application/json`,userAuthRequestBodyJson)) ~> authenticationApiV1.route ~>  check {
    assert(status === StatusCodes.OK)
    assert(body.toString.contains("authToken"))
    authResponse = JsonParser(body.asString).convertTo[AuthenticationResponse]
  }
  Post(s"/medias",HttpEntity(ContentTypes.`application/json`,mediaInputRequestBodyJson))~>addHeader("Auth-Token", authResponse.authToken)~>mediasApiV1.route~>check{
    println(body.toString)
    mediaId=body.asInstanceOf[HttpEntity].data.asString
  }

}
  "it" should "get a media item based on its id" in{
    //authentificate first
    Post("/users/authentication", HttpEntity(ContentTypes.`application/json`,userAuthRequestBodyJson)) ~> authenticationApiV1.route ~>  check {
      assert(status === StatusCodes.OK)
      assert(body.toString.contains("authToken"))
      authResponse = JsonParser(body.asString).convertTo[AuthenticationResponse]
    }
    Get(s"/medias/$mediaId/?raw='true'")~>addHeader("Auth-Token", authResponse.authToken)~>mediasApiV1.route~>check{
      println(body.toString)
    }

    "it" should "delete a media item based on its id" is pending

  }


}
