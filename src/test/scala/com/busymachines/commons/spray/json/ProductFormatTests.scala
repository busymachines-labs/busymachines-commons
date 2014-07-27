package com.busymachines.commons.spray.json

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import spray.json._
import com.busymachines.commons.spray

object ProductFormatTestsFixture extends DefaultJsonProtocol with spray.json.ProductFormatsInstances {

  case class Box(size: Double, things: List[Thing])
  case class Thing(name: String, properties: Map[String, String] = Map.empty)
}

@RunWith(classOf[JUnitRunner])
class ProductFormatTests extends FlatSpec {

  import com.busymachines.commons.spray.json.ProductFormatTestsFixture._

  {
    implicit val thingFormat = format2(Thing)
    implicit val boxFormat = format2(Box)

    "A case class" should "be correctly serialized to json" in {
      val thing = Thing("egg", Map("color" -> "red"))
      val json = """{"name" : "egg", "properties" : {"color" : "red"}}"""
      assert(thing.toJson === json.parseJson)
      assert(json.parseJson.convertTo[Thing] === thing)
    }

    "Default values" should "be correctly deserialized from json" in {
      val json = """{"name" : "egg"}"""
      val thing = Thing("egg")
      assert(json.parseJson.convertTo[Thing] === thing)
      assert(json.parseJson.convertTo[Thing].properties == Map.empty)
    }

    "Empty collections with a default value" should "not be serialized to json" in {
      val thing = Thing("egg")
      val json = """{"name" : "egg"}"""
      assert(thing.toJson === json.parseJson)
    }

    "Empty collections without a default value" should "be serialized to json" in {
      val thing = Box(10, Nil)
      val json = """{"size" : 10, "things" : [] }"""
      assert(thing.toJson === json.parseJson)
    }
  }

  {
    implicit val thingFormat = format2(Thing).withJsonNames("name" -> "description")
    implicit val boxFormat = format2(Box)

    "A renamed field" should "be correctly serialized to json" in {
      val thing = Thing("egg", Map("color" -> "red"))
      val json = """{"description" : "egg", "properties" : {"color" : "red"}}"""
      assert(thing.toJson === json.parseJson)
      assert(json.parseJson.convertTo[Thing] === thing)
    }

    it should "be correctly serialized in a nested object" in {
      val box = Box(10, Thing("egg", Map("color" -> "red")) :: Nil)
      val json = """{"size":10, "things":[{"description" : "egg", "properties" : {"color" : "red"}}]}"""
      assert(box.toJson === json.parseJson)
      assert(json.parseJson.convertTo[Box] === box)
    }
  }

  {
    implicit val thingFormat = format2(Thing)
      .excludeFields("properties")

    "An excluded field" should "not be serialized to json" in {
      val thing = Thing("egg", Map("color" -> "red"))
      val json = """{"name":"egg"}"""
      assert(thing.toJson === json.parseJson)
      assert(json.parseJson.convertTo[Thing] === Thing("egg"))
    }
    it should "not be deserialized from json" in {
      val json = """{"name":"egg", "properties" : {"color" : "red"}}"""
      val thing = Thing("egg")
      assert(json.parseJson.convertTo[Thing] === thing)
    }
  }

  {
    implicit val thingFormat = format2(Thing)
      .excludeFields("name")

    "An excluded field without a default" should "not be deserialized from json" in {
      val thing = Thing("egg", Map("color" -> "red"))
      val json = """{"properties" : {"color" : "red"}}"""
      assert(thing.toJson === json.parseJson)
      intercept[IllegalStateException] {
        assert(json.parseJson.convertTo[Thing] === thing)
      }
    }
  }

  {
    implicit val thingFormat = format2(Thing)
      .excludeFields("name")
      .withDefaults("name" -> (() => "ball"))

    "An excluded field with an explicit default" should "not be serialized to json" in {
      val thing = Thing("ball", Map("color" -> "red"))
      val json = """{"properties" : {"color" : "red"}}"""
      assert(thing.toJson === json.parseJson)
      assert(json.parseJson.convertTo[Thing] === thing)
    }
  }
}
