package com.busymachines.commons.elasticsearch.tools

import java.io.File

import akka.actor.Scheduler
import com.busymachines.commons.Logging
import com.busymachines.commons.elasticsearch.{ESMapping, ESConfig, ESIndex}
import spray.json.JsObject

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class ESAutoImport(config: ESConfig, index: String, files: Seq[File], mappings: String => Option[ESMapping[_]])(implicit ec: ExecutionContext, scheduler: Scheduler) extends Logging {

  private val lastModifiedMap = new mutable.HashMap[File, Long]

  scheduler.schedule(0.seconds, 1.seconds) {
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
