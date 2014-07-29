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
import org.scalastuff.json.JsonParser
import org.scalastuff.json.spray.{SprayJsonBuilder, SprayJsonParser, SprayJsonPrinter}
import spray.json._
import scala.collection.concurrent.TrieMap
import scala.collection.immutable.ListMap
import scala.concurrent.ExecutionContext.Implicits.global
import com.busymachines.commons.Logging
import scala.collection.mutable.HashSet
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import scala.concurrent.duration.DurationInt
import scala.concurrent.Await
import com.busymachines.commons.elasticsearch.ESClient

object ESImport extends Logging {

  def importJson(config: ESConfig, index: String, file: File, overwrite: Boolean, dryRun: Boolean, mappings: String => Option[ESMapping[_]], f: JsObject => Unit): Boolean =
    if (!file.getName.endsWith(".gz")) importJson(config, index, new InputStreamReader(new FileInputStream(file), "UTF-8"), overwrite, dryRun, mappings, f)
    else importJson(config, index, new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), "UTF-8"), overwrite, dryRun, mappings, f)

  def importJson(config: ESConfig, indexName: String, reader: Reader, overwrite: Boolean, dryRun: Boolean, mappings: String => Option[ESMapping[_]], f: JsObject => Unit): Boolean = {

    val client = ESClient(config)
    val typesEncountered = new HashSet[String]
		var hasErrors: Boolean = false
    
    def process(obj: JsObject) = {
      f(obj)
      obj.fields.get("_type") match {
        case Some(JsString(t)) =>
          
          // Create a mapping for this type, but only the first time it is encountered
          val hasMapping = 
            if (typesEncountered.add(t)) {
              mappings(t) match {
                case Some(mapping) =>
                  if (!dryRun)
                    client.addMapping(indexName, t, mapping)
                  true
                case None =>
                  error(s"Unknown type '$t', import aborted.")
                  hasErrors = true
                  false
              }
            } else true

          // import the object
          if (hasMapping) {
            val objectBare = JsObject(obj.fields.filter(f => f._1 != "_type" && f._1 != "_ttl"))
  
            val id = 
            obj.fields.get("_id") match {
              case Some(JsString(id)) => id
              case None => null
            }

              if (!dryRun) {
                val request = new IndexRequest(indexName, t)
                  .id(id)
                  .create(true)
                  .source(objectBare.toString)
                  .refresh(false)
  
                val response = client.javaClient.execute(IndexAction.INSTANCE, request).get
                if (!response.isCreated) {
                  error(s"Couldn't create document: $obj")
                  hasErrors = true
                }
//          case _ =>
//              error(s"Skipping document without id: $obj")
//              hasErrors = true
            }
          }
        case _ =>
          error(s"Skipping document without type: $obj")
          hasErrors = true
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
    
    if (client.indexExists(indexName)) {
      if (overwrite) logger.info(s"Overwriting existing index: $indexName")
      else if (dryRun) logger.warn(s"Index already exists: $indexName")
      else {
        logger.error(s"Index already exists: $indexName")
        hasErrors = true
      }
    } else {
      if (!dryRun)
        client.createIndex(indexName)
    }

    // Stop processing if index existed
    if (dryRun || !hasErrors)
      parser.parse(reader)
    !hasErrors
  }

}
