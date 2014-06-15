package com.busymachines.commons.elasticsearch.tools

import java.io._
import java.util.concurrent.TimeUnit
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import com.busymachines.commons.elasticsearch.{ESCollection, ESIndex, ESMapping}
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.common.unit.TimeValue
import org.scalastuff.esclient.ESClient
import org.scalastuff.json.JsonParser
import org.scalastuff.json.spray.{SprayJsonBuilder, SprayJsonParser, SprayJsonPrinter}
import spray.json._

import scala.collection.concurrent.TrieMap
import scala.collection.immutable.ListMap

object ESImport {

  def importJson(client: ESClient, index: String, file: File, force: Boolean, mappings: PartialFunction[String, ESMapping[_]], f: JsObject => Unit): Unit =
    if (!file.getName.endsWith(".gz")) importJson(client, index, new InputStreamReader(new FileInputStream(file), "UTF-8"), force, mappings, f)
    else importJson(client, index, new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), "UTF-8"), force, mappings, f)

  def importJson(client: ESClient, index: String, reader: Reader, force: Boolean, mappings: PartialFunction[String, ESMapping[_]], f: JsObject => Unit) {

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

}
