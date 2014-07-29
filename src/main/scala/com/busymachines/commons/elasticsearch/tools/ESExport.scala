package com.busymachines.commons.elasticsearch.tools

import java.io._
import java.util.zip.GZIPOutputStream
import ESToolHelper._
import org.scalastuff.json.spray.SprayJsonPrinter
import spray.json.{JsString, JsObject}
import scala.collection.immutable.ListMap
import com.busymachines.commons.elasticsearch.ESClient
import com.busymachines.commons.elasticsearch.ESConfig

object ESExport {

  def exportJson(config: ESConfig, index: String, file: File, f: (JsObject, Long, Long) => Unit): Unit =
    if (!file.getName.endsWith(".gz")) exportJson(config, index, new OutputStreamWriter(new FileOutputStream(file), "UTF-8"), f)
    else exportJson(config, index, new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file)), "UTF-8"), f)

  def exportJson(config: ESConfig, index: String, writer: Writer, f: (JsObject, Long, Long) => Unit) {
  	val client = ESClient(config)
    val printer = new SprayJsonPrinter(writer, 2)
    printer.handler.startArray()
    iterateAll(client, index, (typeName, obj, hit, totalHits) => {
      val objWithType = JsObject(ListMap("_type" -> JsString(typeName)) ++ obj.fields)
      printer(objWithType)
      f(objWithType, hit, totalHits)
    })
    printer.handler.endArray()
    writer.close()
  }
}
