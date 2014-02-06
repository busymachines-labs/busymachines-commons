package com.busymachines.commons.test.prefab.party.api

import com.busymachines.commons.Logging
import com.busymachines.commons.test.AssemblyTestBase
import com.busymachines.prefab.party.api.v1.PartyApiV1Directives
import com.busymachines.prefab.party.api.v1.model.AuthenticationResponse
import org.scalatest.FlatSpec
import spray.http._
import spray.json.JsonParser

/**
 * Created by alex on 2/6/14.
 */
class UsersApiTests extends FlatSpec with AssemblyTestBase with PartyApiV1Directives {

  val userAuthRequestBodyJson = """
    {
      "loginName": "user1@test.com",
      "password": "test"
    }
                                """

  val userRequestBodyJson = """
    {
      "auth-Token":"eec70494-3390-48da-89cd-89e58d851be3"
      "id": "usr1",
      "credentials": "cred1",
      "firstname": "User",
      "middlename": "",
      "lastname": "1",
      "addresses": "",
      "phoneNumbers": "",
      "emailAddresses": "",
      "roles":""
    }"""

  "UsersApi" should "get a list of users" in{

    var authResponse:AuthenticationResponse=null;
    Post("/users/authentication", HttpEntity(ContentTypes.`application/json`,userAuthRequestBodyJson)) ~> authenticationApiV1.route ~>  check {
      assert(status === StatusCodes.OK)
      assert(body.toString.contains("authToken"))
      println(body.toString())
      authResponse = JsonParser(body.asString).convertTo[AuthenticationResponse]
    }

    Get(s"/users/${authResponse.authToken}") ~>usersApiV1.route ~>  check {
      //assert(status === StatusCodes.OK)

      println(body.asString)
    }

    /*
    Post("/users/usr1", HttpEntity(ContentTypes.`application/json`,userRequestBodyJson)) ~> usersApiV1.route ~>  check {
      //assert(status === StatusCodes.OK)
      //assert(body.toString.contains("authToken"))
    }
    */
  }
}
