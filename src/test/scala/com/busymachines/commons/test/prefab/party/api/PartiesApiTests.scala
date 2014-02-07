package com.busymachines.commons.test.prefab.party.api

import org.scalatest.FlatSpec
import com.busymachines.commons.test.AssemblyTestBase
import com.busymachines.prefab.party.api.v1.PartyApiV1Directives
import com.busymachines.prefab.party.logic.PartyFixture
import com.busymachines.prefab.party.api.v1.model.AuthenticationResponse
import spray.http.{StatusCodes, ContentTypes, HttpEntity}
import spray.json.JsonParser
import com.busymachines.prefab.party.domain.Party

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
  var authResponse: AuthenticationResponse = null;
  "PartiesApi" should "get all parties" in {
    Post("/users/authentication", HttpEntity(ContentTypes.`application/json`, userAuthRequestBodyJson)) ~> authenticationApiV1.route ~> check {
      assert(status === StatusCodes.OK)
      assert(body.toString.contains("authToken"))
      authResponse = JsonParser(body.asString).convertTo[AuthenticationResponse]
    }
    Get("/parties")~>addHeader("Auth-Token", authResponse.authToken) ~> partiesApiV1.route ~> check{
      val parties=JsonParser(body.asString).convertTo[List[Party]]
      assert(parties.count(p=>true) == 1)
      assert(parties(0).id.toString === "test-party-1")
    }
  }

  "it" should "create a new party" is pending

  "it" should "get one party" is pending

  "it" should "delete one party" is pending

}
