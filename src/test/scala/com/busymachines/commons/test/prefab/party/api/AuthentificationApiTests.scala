package com.busymachines.commons.test.prefab.party.api

import org.scalatest.FlatSpec
import com.busymachines.commons.Logging
import com.busymachines.prefab.party.domain.{PhoneNumber, Address, User}
import com.busymachines.commons.domain.Id
import com.busymachines.commons.implicits._
import spray.http._
import spray.http.StatusCodes
import spray.json.JsonParser
import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.span
import spray.json.AdditionalFormats
import spray.json.JsonFormat
import spray.json.JsValue
import spray.json.JsObject
import spray.json.JsonParser
import spray.json.pimpString

import com.busymachines.commons.domain.Id
import com.busymachines.commons.implicits._

import spray.http.StatusCodes
import spray.json.JsonParser
import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.span
import spray.json.AdditionalFormats
import spray.json.JsonFormat
import spray.json.JsValue
import spray.json.JsObject
import spray.json.JsonParser
import spray.json.pimpString
import spray.testkit.ScalatestRouteTest
import spray.routing.HttpService

class AuthentificationApiTests extends FlatSpec with Logging  with ScalatestRouteTest{

  debug("*********--- Running AuthentificationApiV1Tests TESTS --- **********")

  "AuthentificationApi" should "authentificate" in {

    val user = User(
      id = Id.static[User]("user1"),
      lastName = Option("Tester"),
      firstName = Option("First"),
      addresses = Address(street = Option("Gen. Henri Berthelot")) :: Nil,
      phoneNumbers = PhoneNumber(phoneNumber="0745123412") :: Nil
    )
/*
    Post("/users/authentication", user) ~> authToken ~> route ~> check {
      assert(status === StatusCodes.OK)
      debug(body.toString)
      debug("Created user with ID: " + Id[User](body.asString))

  }
*/

    pending
}
}
