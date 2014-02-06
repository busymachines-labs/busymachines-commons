package com.busymachines.commons.test.prefab.party.api

import com.busymachines.commons.test.AssemblyTestBase
import com.busymachines.prefab.party.api.v1.PartyApiV1Directives
import com.busymachines.prefab.party.api.v1.model.AuthenticationResponse
import com.busymachines.prefab.party.logic.PartyFixture
import org.scalatest.FlatSpec
import spray.http._
import spray.json.JsonParser

/**
 * Created by alex on 2/6/14.
 */
class UsersApiTests extends FlatSpec with AssemblyTestBase with PartyApiV1Directives with PartyFixture {

  val userAuthRequestBodyJson = """
    {
      "loginName": "user1@test.com",
      "password": "test"
    }
                                """

  val userRequestBodyJson = """
    {
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

  "UsersApi" should "get one user" in {

    var authResponse:AuthenticationResponse=null;
    Post("/users/authentication", HttpEntity(ContentTypes.`application/json`,userAuthRequestBodyJson)) ~> authenticationApiV1.route ~>  check {
      assert(status === StatusCodes.OK)
      assert(body.toString.contains("authToken"))
      authResponse = JsonParser(body.asString).convertTo[AuthenticationResponse]
    }

    Get(s"/users/$testUser1Id") ~> addHeader("Auth-Token", authResponse.authToken) ~> usersApiV1.route ~> check {
      assert(status === StatusCodes.OK)
      assert(body.toString.contains("test-user-1"))
      println(body.asString)
    }
  }
}
