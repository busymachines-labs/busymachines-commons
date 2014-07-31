package com.busymachines.commons.elasticsearch.tools

import java.io.File

import com.busymachines.commons.Logging
import com.busymachines.commons.elasticsearch.{ESMapping, ESConfig, ESIndex}
import spray.json.JsObject

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class ESAutoImport(config: ESConfig, index: String, files: Seq[File], mappings: String => Option[ESMapping[_]])(implicit ec: ExecutionContext) extends Logging {

  private val lastModifiedMap = new mutable.HashMap[File, Long]

  val future = Future {
    while (true) {
      for (file <- files) {
        val lastModified = if (file.exists) file.lastModified else -1
        if (lastModifiedMap.put(file, lastModified) != Some(lastModified)) {
          if (lastModified != -1) {
            info(s"Importing file $file")
            try {
              ESImport.importJson(config, index, file, overwrite = true, dryRun = false, mappings, (obj: JsObject) => {})
            } catch {
              case t: Throwable => logger.error(t)
            }
          }
        }
      }
    }
  }
}
