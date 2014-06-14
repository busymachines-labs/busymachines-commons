package com.busymachines.commons.elasticsearch

import java.io.{Reader, Writer}
import java.util.concurrent.TimeUnit

import org.elasticsearch.action.search.SearchType
import org.elasticsearch.common.unit.TimeValue
import org.scalastuff.json.JsonParser
import org.scalastuff.json.spray.{SprayJsonBuilder, SprayJsonParser, SprayJsonPrinter}
import spray.json._

import scala.collection.concurrent.TrieMap
import scala.collection.immutable.ListMap

private[elasticsearch] object ESIndexDump {

  def exportJson(index: ESIndex, writer: Writer, f: JsObject => Unit) {
    val printer = new SprayJsonPrinter(writer, 2)
    printer.handler.startArray()
    iterateAll(index, (typeName, obj) => {
      val objWithType = JsObject(ListMap("_type" -> JsString(typeName)) ++ obj.fields)
      printer(objWithType)
      f(objWithType)
    })
    printer.handler.endArray()
    writer.close()
  }

  def importJson(index: ESIndex, reader: Reader, mapping: PartialFunction[String, ESMapping[_]], f: JsObject => Unit) {

    val collections = new TrieMap[String, ESCollection[_]]

    val parser = new JsonParser(new SprayJsonBuilder() {
      var level = 0
      override def startObject() {
        if (level == 0)
          reset()
        level += 1
        super.startObject()
      }

      override def endObject() {
        super.endObject()
        level -= 1
        if (level == 0) {
          println(result)
        }
      }
      override def startArray() {
        if (level > 0)
          super.startArray()
      }
      override def endArray() {
        if (level > 0)
          super.endArray()
      }
    })

    def process(obj: JsObject) = {
      val t = obj.fields.get("_type") match {
        case Some(JsString(s)) => s
        case _ => ""
      }
//      val collection = collections.getOrElseUpdate(t, {
//        if (mapping.isDefinedAt(t)) {
//          new ESCollection[_](index, t, mapping(t))
//        }
//        else throw new Exception(s"Unknown type '$t', import aborted.")
//      })
    }
    parser.parse(reader)
  }

  private def iterateAll(index: ESIndex, f: (String, JsObject) => Unit) = {

    val client = index.client.javaClient

    var scrollId: Option[String] =
      Some(client.prepareSearch(index.name)
        .setSearchType(SearchType.SCAN)
        .setScroll(new TimeValue(5, TimeUnit.MINUTES))
        .setSize(100)
        .execute.actionGet.getScrollId)

    val jsonParser = new SprayJsonParser

    while (scrollId.isDefined) {
      val response =
        client.prepareSearchScroll(scrollId.get)
          .setScroll(new TimeValue(5, TimeUnit.MINUTES))
          .execute().actionGet()

      for (hit <- response.getHits.hits) {
        jsonParser.parse(hit.sourceAsString) match {
          case obj: JsObject => f(hit.`type`, obj)
          case _ =>
        }
      }
      scrollId =
        if (response.getHits.getHits.length == 0) None
        else Some(response.getScrollId)
    }
  }
}
