package com.busymachines.prefab.party.api

import akka.actor.ActorSystem
import com.busymachines.commons.Logging
import com.busymachines.commons.Implicits._
import com.busymachines.commons.event.LocalEventBus
import com.busymachines.commons.testing.{DefaultTestESConfig, EmptyESTestIndex}
import com.busymachines.prefab.party.PartyAssembly
import com.busymachines.prefab.party.api.v1.PartyApiV1Directives
import com.busymachines.prefab.party.api.v1.model.AuthenticationResponse
import com.busymachines.prefab.party.Implicits._
import com.busymachines.prefab.party.logic.PartyFixture
import com.typesafe.config.ConfigFactory
import org.scalatest.FlatSpec
import spray.http._
import spray.json.JsonParser
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import spray.testkit.{ScalatestRouteTest, RouteTest}

@RunWith(classOf[JUnitRunner])
class AuthentificationApiTests extends FlatSpec with PartyAssembly with PartyApiV1Directives with RouteTest with ScalatestRouteTest {

  // system setup
  lazy implicit val actorSystem: ActorSystem = ActorSystem("Commons",ConfigFactory.load("tests.conf"))
  lazy implicit val executionContext = actorSystem.dispatcher
  lazy val eventBus = new LocalEventBus(actorSystem)
  lazy val index = EmptyESTestIndex(getClass, DefaultTestESConfig, eventBus)
  def actorRefFactory = actorSystem
  PartyFixture.createDevMode(partyDao, credentialsDao)

  val userAuthRequestBodyJson = """
    {
      "loginName": "user1@test.com",
      "password": "test"
    }
    """

  val userAuthWithWrongPartyNameRequestBodyJson = """
    {
      "loginName": "user1@test.com",
      "password": "test",
      "party" : "Test,Company"
    }
                                """

  val userAuthWithCorrectPartyNameRequestBodyJson = """
    {
      "loginName": "user2@test.com",
      "password": "test",
      "party" : "Test 2 Company"
    }
                                                  """

  "AuthentificationApi" should "authentificate with username and password" in {
      Post("/users/authentication", HttpEntity(ContentTypes.`application/json`,userAuthRequestBodyJson)) ~> authenticationApiV1.route ~>  check {
        assert(status === StatusCodes.OK)
        assert(body.toString.contains("authToken"))
      }
  }

  it should "fail to authentificate with wrong party username and password" in {
    Post("/users/authentication", HttpEntity(ContentTypes.`application/json`,userAuthWithWrongPartyNameRequestBodyJson)) ~> authenticationApiV1.route ~>  check {
      assert(status === StatusCodes.Forbidden)
    }
  }

  it should "authentificate with correct party username and password" in {
    Post("/users/authentication", HttpEntity(ContentTypes.`application/json`,userAuthWithCorrectPartyNameRequestBodyJson)) ~> authenticationApiV1.route ~>  check {
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
 // TODO Fix test
/*
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
*/
}
