package com.busymachines.commons.elasticsearch

import java.io.{Reader, Writer}
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.common.unit.TimeValue
import java.util.concurrent.TimeUnit
import spray.json._
import com.busymachines.commons.util.JsonParser
import scala.Some
import scala.collection.immutable.ListMap

private[elasticsearch] object ESIndexDump {

  def exportJsonDump(index: ESIndex, writer: Writer, f: JsObject => Unit) {
    writer.write("[\n")
    iterateAll(index, (typeName, obj) => {
      val objWithType = JsObject(ListMap("_type" -> JsString(typeName)) ++ obj.fields)
      writer.write("  ")
      write(objWithType, 2, writer)
      writer.write(",\n")
      f(objWithType)
    })
    writer.write("]\n")
    writer.close()
  }

  def importJsonDump(index: ESIndex, reader: Reader, f: JsObject => Unit) {

  }

  private def write(value: JsValue, indent: Int, writer: Writer): Unit =
    value match {
      case JsObject(fields) =>
        writer.append("{\n")
        var first = true
        for (field <- fields) {
          if (first) first = false else writer.append(",\n")
          for (i <- 1 to indent + 2) writer.append(' ')
          writer.append('\"').append(field._1).append("\" : ")
          write(field._2, indent + 2, writer)
        }
        if (!first) writer.append("\n")
        for (i <- 1 to indent) writer.append(' ')
        writer.append("}")
      case JsArray(values) =>
        writer.append("[\n")
        var first = true
        for (value <- values) {
          if (first) first = false else writer.append(",\n")
          for (i <- 1 to indent + 2) writer.append(' ')
          write(value, indent + 2, writer)
        }
        if (!first) writer.append("\n")
        for (i <- 1 to indent) writer.append(' ')
        writer.append("]")
      case JsString(s) =>
        writer.append('\"').append(s).append('\"')
      case JsNumber(n) =>
        writer.append(String.valueOf(n))
      case JsNull =>
        writer.append("null")
      case JsTrue =>
        writer.append("true")
      case JsFalse =>
        writer.append("false")
    }

  private def iterateAll(index: ESIndex, f: (String, JsObject) => Unit) = {

    val client = index.client.javaClient

    var scrollId: Option[String] =
      Some(client.prepareSearch(index.name)
        .setSearchType(SearchType.SCAN)
        .setScroll(new TimeValue(5, TimeUnit.MINUTES))
        .setSize(100)
        .execute.actionGet.getScrollId)

    while (scrollId.isDefined) {
      val response =
        client.prepareSearchScroll(scrollId.get)
          .setScroll(new TimeValue(5, TimeUnit.MINUTES))
          .execute().actionGet()

      for (hit <- response.getHits.hits) {
        JsonParser.parse(hit.sourceAsString) match {
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
