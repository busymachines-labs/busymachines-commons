package com.busymachines.commons.test.prefab.party.api

import com.busymachines.commons.test.AssemblyTestBase
import com.busymachines.prefab.party.api.v1.PartyApiV1Directives
import com.busymachines.prefab.party.api.v1.model.AuthenticationResponse
import com.busymachines.prefab.party.logic.PartyFixture
import org.scalatest.FlatSpec
import spray.http._
import spray.json.JsonParser
import com.busymachines.prefab.party.domain.User

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
      "id": "test-user-1",
      "credentials": "test-user-1-credentials",
      "firstName": "User",
      "middleName": "",
      "lastName": "1",
      "addresses": [],
      "phoneNumbers": [],
      "emailAddresses": [],
      "roles":[]
    }"""

  var authResponse:AuthenticationResponse=null;

  "UsersApi" should "get one user" in {
    Post("/users/authentication", HttpEntity(ContentTypes.`application/json`,userAuthRequestBodyJson)) ~> authenticationApiV1.route ~>  check {
      assert(status === StatusCodes.OK)
      assert(body.toString.contains("authToken"))
      authResponse = JsonParser(body.asString).convertTo[AuthenticationResponse]
    }
    Get(s"/users/$testUser1Id") ~> addHeader("Auth-Token", authResponse.authToken) ~> usersApiV1.route ~> check {
      assert(status === StatusCodes.OK)
      assert(body.toString.contains("test-user-1"))
      //println(body.asString)
    }
  }

  it should "get all users" in {
      Post("/users/authentication", HttpEntity(ContentTypes.`application/json`,userAuthRequestBodyJson)) ~> authenticationApiV1.route ~>  check {
        assert(status === StatusCodes.OK)
        assert(body.toString.contains("authToken"))
        authResponse = JsonParser(body.asString).convertTo[AuthenticationResponse]
      }
      Get("/users") ~> addHeader("Auth-Token", authResponse.authToken) ~> usersApiV1.route ~> check {
        assert(status === StatusCodes.OK)
        //assert(body.toString.contains("test-user-1"))
        val response = JsonParser(body.asString).convertTo[List[User]]
        assert(response.count(p=>true) === 1)
      }
  }

  //TODO FIX update - current implementation of the API doesn't update a user
  it should "update user" in {
    /*
    Post("/users/authentication", HttpEntity(ContentTypes.`application/json`,userAuthRequestBodyJson)) ~> authenticationApiV1.route ~>  check {
      assert(status === StatusCodes.OK)
      assert(body.toString.contains("authToken"))
      authResponse = JsonParser(body.asString).convertTo[AuthenticationResponse]
    }
    Post(s"/users/{$testUser1Id}",HttpEntity(ContentTypes.`application/json`,userRequestBodyJson)) ~> addHeader("Auth-Token", authResponse.authToken) ~> usersApiV1.route ~> check {
      assert(status === StatusCodes.OK)
      assert(body.toString.contains("test-user-1"))
      println(body.asString)
    }
    */
    pending
  }
}
