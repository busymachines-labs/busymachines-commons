//package com.busymachines.commons.test
//
//
//import com.busymachines.commons.domain.CommonJsonFormats._
//import org.scalatest.FlatSpec
//import com.busymachines.commons.{Extension, Extensions}
//import spray.json._
//import org.junit.runner.RunWith
//import org.scalatest.junit.JUnitRunner
//
//object ExtensionTestsFixture {
//  case class Thing(name: String, size: Int, extensions: Extensions = Extensions.empty)
//
//  case class PharmaThing(ziNumber: String = "")
//  case class OrderThing(orderNr: String = "", name: String = "")
//
//  implicit val thingFormat = jsonFormat2Ext(Thing)
//  implicit val pharmaThingFormat = jsonFormat1(PharmaThing, true)
//  implicit def toPharmaThing(thing: Thing) = thing.extensions(PharmaThingExtension)
//
//  implicit val orderThingFormat = jsonFormat2(OrderThing, true)
//  implicit def toOrderThing(thing: Thing) = thing.extensions(OrderThingExtension)
//  implicit class RichOrderThing(thing: Thing) {
//    def copyOrder(f: OrderThing => OrderThing) =
//      thing.copy(extensions = thing.extensions.copy(OrderThingExtension, f))
//  }
//
//  object PharmaThingExtension extends Extension[Thing, PharmaThing](PharmaThing())
//  object OrderThingExtension extends Extension[Thing, OrderThing](OrderThing())
//
//  OrderThingExtension.register()
//}
//
//@RunWith(classOf[JUnitRunner])
//class ExtensionTests extends FlatSpec {
//
//  import ExtensionTestsFixture._
//
//  "An extension" should "can be changed and read" in {
//    val t = Thing("hi", 3)
//    assert(t.orderNr === "")
//    val t2 = t.copyOrder(_.copy(orderNr = "12"))
//    assert(t2.orderNr === "12")
//  }
//
//  it should "be merged into one json document" in {
//    val t = Thing("hi", 3, Extensions(OrderThingExtension -> OrderThing("23")))
//    val json = thingFormat.write(t)
//    assert(json.isInstanceOf[JsObject])
//    assert(json.asInstanceOf[JsObject].fields("size").asInstanceOf[JsNumber].value === 3)
//    assert(json.asInstanceOf[JsObject].fields("orderNr").asInstanceOf[JsString].value === "23")
//  }
//
//  it should "be correctly deserialized" in {
//    val json = """{"name":"Test","size":5,"orderNr":"25"}"""
//    val t = thingFormat.read(json.asJson)
//    assert(t.name === "Test")
//    assert(t.size === 5)
//    assert(t.orderNr === "25")
//    assert(t.extensions(OrderThingExtension).orderNr === "25")
//    assert(t.extensions(OrderThingExtension).name === "Test")
//  }
//
//  it should "prioritize parent fields" in {
//    val t = Thing("Parent Name", 3, Extensions(OrderThingExtension -> OrderThing("23", "Order Name")))
//    val json = thingFormat.write(t)
//    assert(json.isInstanceOf[JsObject])
//    assert(json.asInstanceOf[JsObject].fields("name").asInstanceOf[JsString].value === "Parent Name")
//    assert(json.asInstanceOf[JsObject].fields("size").asInstanceOf[JsNumber].value === 3)
//    assert(json.asInstanceOf[JsObject].fields("orderNr").asInstanceOf[JsString].value === "23")
//  }
//}
