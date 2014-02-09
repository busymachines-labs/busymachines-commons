package com.busymachines.commons.test

import org.scalatest.FlatSpec
import com.busymachines.commons.implicits._
import com.busymachines.commons.domain.CommonJsonFormats._
import spray.json._
/**
 * Created by ruud on 09/02/14.
 */

object CommonProductformatsTests {
  case class Thing(name: String = "The Thing", map: Map[String, String] = Map.empty)
}
class CommonProductformatsTests extends FlatSpec {

  import CommonProductformatsTests._

  implicit val thingFormat = jsonFormat2(Thing, true)

  "JsonFormat" should "accepts parameters" in {
    val json = """{ "name" : "New Name" }""".asJson
    val thing = thingFormat.read(json)
    assert(thing.name === "New Name")
  }

  "JsonFormat" should "allow optional parameters" in {
    val json = "{}".asJson
    val thing = thingFormat.read(json)
    assert(thing.name === "The Thing")
  }
}
