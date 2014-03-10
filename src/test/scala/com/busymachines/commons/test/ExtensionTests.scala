package com.busymachines.commons.test


import com.busymachines.commons.implicits._
import org.scalatest.FlatSpec
import com.busymachines.commons.{ExtensionsImplicits, Extension, Extensions}
import spray.json._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

object ExtensionTestsFixture {
  case class Thing(name: String, extensions: Extensions[Thing] = Extensions.empty)
  case class Big(size: Int = 10, name: Option[String] = None)
  case class Colored(color: String = "red")

  implicit val thingFormat = format2(Thing)
  implicit val bigFormat = format2(Big)
  implicit val coloredFormat = format1(Colored)

  implicit val BigThingExtension = new Extension[Thing, Big](_.extensions, (a, b) => a.copy(extensions = b))
  implicit def toBigThing(thing: Thing) = thing.extensions[Big]
  BigThingExtension.register()

  implicit object ColoredThingExtension extends Extension[Thing, Colored](_.extensions, (a, b) => a.copy(extensions = b))
  implicit def toColoredThing(thing: Thing) = thing.extensions[Colored]
  ColoredThingExtension.register()
}

@RunWith(classOf[JUnitRunner])
class ExtensionTests extends FlatSpec {

  import ExtensionTestsFixture._

  "An extension" should "be converted to Extensions" in {
    val t = Thing("egg", Big(3))
    assert(t.size === 3)
  }

  "An extension" should "be concatenated to Extensions" in {
    val t = Thing("egg", Big(3) & Colored("yellow"))
    assert(t.size === 3)
    assert(t.color === "yellow")
  }

  it should "be merged into one json document" in {
    val t = Thing("egg", Big(3) & Colored("yellow"))
    val json = """{"name":"egg","size":3,"color":"yellow"}"""
    assert(t.toJson === json.asJson)
  }

  it should "be correctly deserialized" in {
    val json = """{"name":"egg","size":3,"color":"yellow"}"""
    val t = Thing("egg", Big(3) & Colored("yellow"))
    assert(json.asJson.convertTo[Thing] === t)
    assert(json.asJson.convertTo[Thing].name === "egg")
    assert(json.asJson.convertTo[Thing].size === 3)
    assert(json.asJson.convertTo[Thing].color === "yellow")
    assert(json.asJson.convertTo[Thing].extensions[Big] === Big(3))
  }

  it should "prioritize parent fields" in {
    val t = Thing("Parent name", Big(12, "Big name"))
    val json = """{"name":"Parent name","size":12}"""
    assert(t.toJson == json.asJson)
  }
}
