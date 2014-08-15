package com.busymachines.commons.logger.consumer

import akka.actor.Actor
import com.busymachines.commons.elasticsearch.{ESCollection, ESIndex}
import com.busymachines.commons.event.DoNothingEventSystem
import com.busymachines.commons.logger.domain.{LoggerESTypes, LogMessage}
import com.busymachines.commons.testing.DefaultTestESConfig
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Alexandru Matei on 15.08.2014.
 */

class MessageActorConsumer (clusterName:String, hostNames:String, port:String, indexNamePrefix:String, indexNameDateFormat:String,
                             indexDocumentType:String) extends Actor {
  lazy val actualIndexName = s"${indexNamePrefix}-${DateTimeFormat.forPattern(indexNameDateFormat).print(DateTime.now)}"

  lazy val collection = new ESCollection[LogMessage](new ESIndex(DefaultTestESConfig, actualIndexName, DoNothingEventSystem), LoggerESTypes.LogMessage)


  def receive={
        case message: LogMessage =>
          try{
            collection.create(message, false, None)
          }catch{
            case ex:Exception=>println(ex)
          }
        case _ =>
      }
}
