package com.busymachines.commons.test

import org.scalatest.FlatSpec
import com.busymachines.commons.implicits._
import spray.json._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * Created by ruud on 09/02/14.
 */

object CommonProductformatsTests {
  case class Thing(name: String = "The Thing", map: Map[String, String] = Map.empty)
}
@RunWith(classOf[JUnitRunner])
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
