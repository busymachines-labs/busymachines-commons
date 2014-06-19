package com.busymachines.commons.elasticsearch.tools

import java.io._
import java.util.concurrent.TimeUnit
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.elasticsearch.{ESConfig, ESCollection, ESIndex, ESMapping}
import com.busymachines.commons.event.{DoNothingEventSystem, LocalEventBus}
import org.elasticsearch.action.index.{IndexAction, IndexRequest}
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.common.unit.TimeValue
import org.scalastuff.esclient.ESClient
import org.scalastuff.json.JsonParser
import org.scalastuff.json.spray.{SprayJsonBuilder, SprayJsonParser, SprayJsonPrinter}
import spray.json._

import scala.collection.concurrent.TrieMap
import scala.collection.immutable.ListMap
import scala.concurrent.ExecutionContext.Implicits.global

object ESImport {

  def importJson(config: ESConfig, index: String, file: File, force: Boolean, mappings: PartialFunction[String, ESMapping[_]], f: JsObject => Unit): Unit =
    if (!file.getName.endsWith(".gz")) importJson(config, index, new InputStreamReader(new FileInputStream(file), "UTF-8"), force, mappings, f)
    else importJson(config, index, new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), "UTF-8"), force, mappings, f)

  def importJson(config: ESConfig, indexName: String, reader: Reader, force: Boolean, mappings: PartialFunction[String, ESMapping[_]], f: JsObject => Unit) {

    lazy val index = new ESIndex(config, indexName, DoNothingEventSystem)

    def process(obj: JsObject) = {
      f(obj)
      obj.fields.get("_type") match {
        case Some(JsString(t)) =>
          val mapping =
            if (mappings.isDefinedAt(t)) {
              new ESCollection[Any](index, t, mappings(t).asInstanceOf[ESMapping[Any]])
            }
            else throw new Exception(s"Unknown type '$t', import aborted.")
          val objWithoutType = JsObject(obj.fields.filter(_._1 != "_type"))

          obj.fields.get("_id") match {
            case Some(JsString(id)) =>

              val request = new IndexRequest(indexName, t)
                .id(id)
                .create(true)
                .source(objWithoutType.toString)
                .refresh(false)

              val response = index.client.javaClient.execute(IndexAction.INSTANCE, request).get
              if (!response.isCreated)
                println("Couldn't create document: " + obj)
            case _ =>
              println("Skipping document without id: " + obj)
          }
        case _ =>
          println("Skipping document without type: " + obj)
      }
    }

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
          process(result.asJsObject)
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

    parser.parse(reader)
  }

}
