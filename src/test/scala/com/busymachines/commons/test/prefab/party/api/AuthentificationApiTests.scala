package com.busymachines.commons.test.prefab.party.api

import com.busymachines.commons.Logging
import com.busymachines.commons.test.AssemblyTestBase
import com.busymachines.prefab.party.api.v1.PartyApiV1Directives
import com.busymachines.prefab.party.api.v1.model.AuthenticationResponse
import org.scalatest.FlatSpec
import spray.http._
import spray.json.JsonParser

class AuthentificationApiTests extends FlatSpec with Logging with AssemblyTestBase with PartyApiV1Directives {

  val userAuthRequestBodyJson = """
    {
      "loginName": "user1@test.com",
      "password": "test"
    }
    """

  "AuthentificationApi" should "authentificate with username and password" in {
      Post("/users/authentication", HttpEntity(ContentTypes.`application/json`,userAuthRequestBodyJson)) ~> authenticationApiV1.route ~>  check {
        assert(status === StatusCodes.OK)
        assert(body.toString.contains("authToken"))
      }
  }

  it should "inform whether an authentication token is valid or not" in {
    var authResponse:AuthenticationResponse = null
    Post("/users/authentication", HttpEntity(ContentTypes.`application/json`,userAuthRequestBodyJson)) ~> authenticationApiV1.route ~>  check {
      assert(status === StatusCodes.OK)
      authResponse = JsonParser(body.asString).convertTo[AuthenticationResponse]
    }

    Get(s"/users/authentication/${authResponse.authToken}") ~> authenticationApiV1.route ~>  check {
      assert(status === StatusCodes.OK)
    }

    Get(s"/users/authentication/justARandomText") ~> authenticationApiV1.route ~>  check {
      assert(status === StatusCodes.NotFound)
    }
  }

    it should "invalidate the auth token on logout" in {
      var authResponse:AuthenticationResponse = null
      Post("/users/authentication", HttpEntity(ContentTypes.`application/json`,userAuthRequestBodyJson)) ~> authenticationApiV1.route ~>  check {
        assert(status === StatusCodes.OK)
        authResponse = JsonParser(body.asString).convertTo[AuthenticationResponse]
      }

      Get(s"/users/authentication/${authResponse.authToken}") ~> authenticationApiV1.route ~>  check {
        assert(status === StatusCodes.OK)
      }

      Delete(s"/users/authentication/${authResponse.authToken}") ~> authenticationApiV1.route ~>  check {
        assert(status === StatusCodes.OK)
      }

      Get(s"/users/authentication/${authResponse.authToken}") ~> authenticationApiV1.route ~>  check {
        assert(status === StatusCodes.NotFound)
      }
  }

}
