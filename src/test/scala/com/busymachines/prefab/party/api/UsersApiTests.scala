package com.busymachines.prefab.party.api

import akka.actor.ActorSystem
import com.busymachines.commons.event.LocalEventBus
import com.busymachines.commons.testing.{DefaultTestESConfig, EmptyESTestIndex}
import com.busymachines.prefab.party.PartyAssembly
import com.busymachines.prefab.party.api.v1.PartyApiV1Directives
import com.busymachines.prefab.party.api.v1.model.AuthenticationResponse
import com.busymachines.prefab.party.logic.PartyFixture
import com.typesafe.config.ConfigFactory
import org.scalatest.FlatSpec
import spray.http._
import spray.json.JsonParser
import com.busymachines.prefab.party.domain.User
import com.busymachines.commons.Implicits._
import com.busymachines.prefab.party.Implicits._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import spray.testkit.{ScalatestRouteTest, RouteTest}

/**
 * Created by alex on 2/6/14.
 */
@RunWith(classOf[JUnitRunner])
class UsersApiTests extends FlatSpec with PartyAssembly with PartyApiV1Directives with PartyFixture with RouteTest with ScalatestRouteTest {
  // system setup
  lazy implicit val actorSystem: ActorSystem = ActorSystem("Commons",ConfigFactory.load("tests.conf"))
  lazy implicit val executionContext = actorSystem.dispatcher
  lazy val eventBus = new LocalEventBus(actorSystem)
  lazy val index = new EmptyESTestIndex(getClass, DefaultTestESConfig, eventBus)
  PartyFixture.createDevMode(partyDao, credentialsDao)

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
      "firstName": "Test User Updated",
      "middleName": "",
      "lastName": "1",
      "addresses": [],
      "phoneNumbers": [],
      "emailAddresses": [],
      "roles":[]
    }"""



  "UsersApi" should "get one user" in {
    var authResponse:AuthenticationResponse=null;

    //authenticate
    Post("/users/authentication", HttpEntity(ContentTypes.`application/json`,userAuthRequestBodyJson)) ~> authenticationApiV1.route ~>  check {
      assert(status === StatusCodes.OK)
      assert(body.toString.contains("authToken"))
      authResponse = JsonParser(body.asString).convertTo[AuthenticationResponse]
    }
    //get user 1
    Get(s"/users/$testUser1Id") ~> addHeader("Auth-Token", authResponse.authToken) ~> usersApiV1.route ~> check {
      assert(status === StatusCodes.OK)
      assert(body.toString.contains("test-user-1"))
    }
  }

  it should "get all users" in {
    var authResponse:AuthenticationResponse=null;

    //authenticate
    Post("/users/authentication", HttpEntity(ContentTypes.`application/json`,userAuthRequestBodyJson)) ~> authenticationApiV1.route ~>  check {
        assert(status === StatusCodes.OK)
        assert(body.toString.contains("authToken"))
        authResponse = JsonParser(body.asString).convertTo[AuthenticationResponse]
      }
    Get("/users") ~> addHeader("Auth-Token", authResponse.authToken) ~> usersApiV1.route ~> check {
        assert(status === StatusCodes.OK)
        val response = JsonParser(body.asString).convertTo[List[User]]
        assert(response.count(p=>true) === 1)
      }
  }

  it should "update user" in {
    var authResponse:AuthenticationResponse=null;

    //authenticate
    Post("/users/authentication", HttpEntity(ContentTypes.`application/json`,userAuthRequestBodyJson)) ~> authenticationApiV1.route ~>  check {
      assert(status === StatusCodes.OK)
      assert(body.toString.contains("authToken"))
      authResponse = JsonParser(body.asString).convertTo[AuthenticationResponse]
    }
    //update
    Put(s"/users/$testUser1Id",HttpEntity(ContentTypes.`application/json`,userRequestBodyJson)) ~> addHeader("Auth-Token", authResponse.authToken) ~> usersApiV1.route ~> check {
      assert(status === StatusCodes.OK)
    }
    //get updated user
    Get(s"/users/$testUser1Id") ~> addHeader("Auth-Token", authResponse.authToken) ~> usersApiV1.route ~> check {
      assert(status === StatusCodes.OK)
      assert(body.toString.contains("Test User Updated"))
    }
  }
}
