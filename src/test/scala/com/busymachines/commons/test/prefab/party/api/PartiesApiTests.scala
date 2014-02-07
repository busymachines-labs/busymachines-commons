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

/**
 * Created by alex on 2/7/14.
 */
class PartiesApiTests extends FlatSpec with AssemblyTestBase with PartyApiV1Directives with PartyFixture {

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
          "lastName": "Doe 2",
          "addresses": [{
            "street": "Street 2"
          }]
      }]
      }
                             """

  var authResponse: AuthenticationResponse = null;
  "PartiesApi" should "get all parties" in {
    Post("/users/authentication", HttpEntity(ContentTypes.`application/json`, userAuthRequestBodyJson)) ~> authenticationApiV1.route ~> check {
      assert(status === StatusCodes.OK)
      assert(body.toString.contains("authToken"))
      authResponse = JsonParser(body.asString).convertTo[AuthenticationResponse]
    }
    Get("/parties") ~> addHeader("Auth-Token", authResponse.authToken) ~> partiesApiV1.route ~> check {
     val parties = JsonParser(body.asString).convertTo[List[Party]]
      assert(parties.count(p => true) == 1)
      assert(parties(0).id.toString === "test-party-1")
    }
  }

  "it" should "create and retrieve a new party" in {
    Post("/users/authentication", HttpEntity(ContentTypes.`application/json`, userAuthRequestBodyJson)) ~> authenticationApiV1.route ~> check {
      assert(status === StatusCodes.OK)
      assert(body.toString.contains("authToken"))
      authResponse = JsonParser(body.asString).convertTo[AuthenticationResponse]
    }
    Post("/parties", HttpEntity(ContentTypes.`application/json`, partyRequestBodyJson)) ~> addHeader("Auth-Token", authResponse.authToken) ~> partiesApiV1.route ~> check {
      //assert(status === StatusCodes.OK)
      //println(body.toString)
    }
    pending
  }

  "it" should "get one party" is pending

  "it" should "delete one party" is pending

}
