package com.busymachines.commons.test

import org.scalatest.FlatSpec
import com.busymachines.commons.implicits._
import spray.json._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.busymachines.commons.{Extension, Extensions}

/**
* Created by ruud on 09/02/14.
*/

object ProductJsonFormatTests {

  case class Thing1(name: String = "The Thing", map: Map[String, String] = Map.empty)
  case class Thing2(name: String, map: Map[String, String])
  case class Thing3(name: String, ext: Extensions[Thing3])
  case class Colored(color: String = "")
  case class Big(size: Int)

  implicit val thing1Format = format2(Thing1)
  implicit val thing2Format = format2(Thing2)
  implicit val thing3Format = format2(Thing3)
  implicit val coloredFormat = format1(Colored)
  implicit val bigFormat = format1(Big)

  implicit object Thing3ColoredExtension extends Extension[Thing3, Colored](_.ext, (a, b) => a.copy(ext = b))
  implicit object BigExtension extends Extension[Thing3, Big](_.ext, (a, b) => a.copy(ext = b))

  implicit def toColored[A](a: A)(implicit ext: Extension[A, Colored]): Colored = ext(a)
  implicit def toBig[A](a: A)(implicit ext: Extension[A, Big]) = ext(a)
}

@RunWith(classOf[JUnitRunner])
class ProductJsonFormatTests extends FlatSpec {

  import ProductJsonFormatTests._

  Thing3ColoredExtension.register()
  BigExtension.register()

  "Default field value" should "be used when missing in json" in {
    val json = "{}".parseJson
    val thing = json.convertTo[Thing1]
    assert(thing.name === "The Thing")
  }

  it should "be overridden by json value" in {
    val json = """{ "name" : "New Name" }""".parseJson
    val thing = json.convertTo[Thing1]
    assert(thing.name === "New Name")
  }

  "Empty collections with default values" should "be excluded" in {
    assert(Thing1().toJson === """{"name":"The Thing"}""".parseJson)
  }

  "Empty collections without default values" should "not be excluded" in {
    assert(Thing2("The Thing", Map.empty).toJson === """{"name":"The Thing", "map":{}}""".parseJson)
  }

//  "Multiple extensions " should "be instantiated" in {
//    val json = """{"name":"Colored Thing","size" : 12, "color":"red"}""".asJson
//    val thing = json.convertTo[Thing3]
//    assert(thing.ext.map.size === 2)
//    assert(thing.color === "red")
//    assert(thing.size === 12)
//  }
}
