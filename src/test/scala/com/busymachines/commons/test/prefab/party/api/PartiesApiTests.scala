package com.busymachines.commons.test.prefab.party.api

import org.scalatest.FlatSpec
import com.busymachines.commons.test.AssemblyTestBase
import com.busymachines.prefab.party.api.v1.PartyApiV1Directives
import com.busymachines.prefab.party.logic.PartyFixture
import com.busymachines.prefab.party.api.v1.model.AuthenticationResponse
import spray.http.{StatusCodes, ContentTypes, HttpEntity}
import spray.json.JsonParser
import com.busymachines.prefab.party.domain.Party
import com.busymachines.prefab.party.db
import com.busymachines.commons.elasticsearch
import com.busymachines.commons.elasticsearch.RichJsValue
import com.busymachines.prefab.party.db.PartyMapping
import com.busymachines.commons.domain.Id

/**
 * Created by alex on 2/7/14.
 */
class PartiesApiTests extends FlatSpec with AssemblyTestBase with PartyApiV1Directives with PartyFixture {

  case class PartyPostResponse(id:String)
  implicit val partyPostResponseFormat=jsonFormat1(PartyPostResponse)

  val userAuthRequestBodyJson = """
    {
      "loginName": "user1@test.com",
      "password": "test"
    }
                                """
  val partyRequestBodyJson = """
      {
      "id": "test-party-2",
      "tenant": "test-tenant-2",
      "company": {
          "name": "Test Company 2"
        },
      "addresses": [{
          "street": "Korenmolen 2",
          "houseNumber": "4",
          "postalCode": "1541RW",
          "city": "Koog aan de Zaan"
        }],
      "phoneNumbers": [],
      "emailAddresses": [],
      "phoneNumbers": [{
          "kind": "none",
          "phoneNumber": "0745535785"
      }],
      "relations": [],
      "users": [{
          "id": "test-user-2",
          "credentials": "test-user-1-credentials",
          "firstName": "John 2",
          "phoneNumbers":[],
          "emailAddresses":[],
          "lastName": "Doe 2",
          "addresses": [{
            "street": "Street 2"
          }],
          "roles":[]
      }],
      "userRoles": []
      }
                             """

  "PartiesApi" should "get a party" in {
    var authResponse: AuthenticationResponse = null
    //authenticate
    Post("/users/authentication", HttpEntity(ContentTypes.`application/json`, userAuthRequestBodyJson)) ~> authenticationApiV1.route ~> check {
      assert(status === StatusCodes.OK)
      assert(body.toString.contains("authToken"))
      authResponse = JsonParser(body.asString).convertTo[AuthenticationResponse]
    }
    Get(s"/parties/${testParty1Id}")~> addHeader("Auth-Token", authResponse.authToken) ~> partiesApiV1.route ~> check {
      assert(status === StatusCodes.OK)
      //println(body.toString)
      val party = JsonParser(body.asString).convertTo[Party]
      assert(party.id === Id[Party]("test-party-1"))
    }
  }

  it should "create and retrieve and delete a new party" in {
    var authResponse: AuthenticationResponse = null
    var testParty2:PartyPostResponse=null;

    //authenticate
    Post("/users/authentication", HttpEntity(ContentTypes.`application/json`, userAuthRequestBodyJson)) ~> authenticationApiV1.route ~> check {
      assert(status === StatusCodes.OK)
      assert(body.toString.contains("authToken"))
      authResponse = JsonParser(body.asString).convertTo[AuthenticationResponse]
    }
    //post a second party
    Post("/parties", HttpEntity(ContentTypes.`application/json`, partyRequestBodyJson)) ~> addHeader("Auth-Token", authResponse.authToken) ~> partiesApiV1.route ~> check {
      assert(status === StatusCodes.OK)
      testParty2=JsonParser(body.asString).convertTo[PartyPostResponse]
    }
    //retrieve it
    Get(s"/parties/${testParty2.id}")~> addHeader("Auth-Token", authResponse.authToken) ~> partiesApiV1.route ~> check {
      assert(status === StatusCodes.OK)
      val party = JsonParser(body.asString).convertTo[Party]
      assert(party.id === Id[Party]("test-party-2"))
    }
    //delete
    Delete(s"/parties/${testParty2.id}")~> addHeader("Auth-Token", authResponse.authToken) ~> partiesApiV1.route ~> check {
      assert(status === StatusCodes.OK)
    }
    //check it is deleted
    Get(s"/parties/${testParty2.id}")~> addHeader("Auth-Token", authResponse.authToken) ~> partiesApiV1.route ~> check {
      assert(status === StatusCodes.NotFound)
    }

  }

  it should "get all parties" in {
    var authResponse: AuthenticationResponse = null

    //authenticate
    Post("/users/authentication", HttpEntity(ContentTypes.`application/json`, userAuthRequestBodyJson)) ~> authenticationApiV1.route ~> check {
      assert(status === StatusCodes.OK)
      assert(body.toString.contains("authToken"))
      authResponse = JsonParser(body.asString).convertTo[AuthenticationResponse]
    }
    //get all parties (should be only one)
    Get("/parties") ~> addHeader("Auth-Token", authResponse.authToken) ~> partiesApiV1.route ~> check {
      val parties = JsonParser(body.asString).convertTo[List[Party]]
      assert(parties.count(p => true) == 1)
      assert(parties(0).id.toString === "test-party-1")
    }
  }

}
