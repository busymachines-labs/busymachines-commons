package com.busymachines.commons.elasticsearch.tools

import java.io._
import java.util.zip.GZIPOutputStream

import ESToolHelper._
import org.scalastuff.esclient.ESClient
import org.scalastuff.json.spray.SprayJsonPrinter
import spray.json.{JsString, JsObject}

import scala.collection.immutable.ListMap

object ESExport {

  def exportJson(client: ESClient, index: String, file: File, f: (JsObject, Long, Long) => Unit): Unit =
    if (!file.getName.endsWith(".gz")) exportJson(client, index, new OutputStreamWriter(new FileOutputStream(file), "UTF-8"), f)
    else exportJson(client, index, new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file)), "UTF-8"), f)

  def exportJson(client: ESClient, index: String, writer: Writer, f: (JsObject, Long, Long) => Unit) {
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
