package com.busymachines.commons.logger.appender

import com.busymachines.commons.CommonException
import com.busymachines.commons.elasticsearch.{ESCollection, ESIndex}
import com.busymachines.commons.event.DoNothingEventSystem
import com.busymachines.commons.logger.domain._
import com.busymachines.commons.logger.layout.ElasticLayout
import com.busymachines.commons.testing.DefaultTestESConfig
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.plugins.{Plugin, PluginAttribute, PluginElement, PluginFactory}
import org.apache.logging.log4j.core.impl.Log4jLogEvent
import org.apache.logging.log4j.core.{Filter, Layout, LogEvent}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Created by Alexandru Matei on 14.08.2014.
 */

object ElasticAppender {
  @PluginFactory
  def createAppender(@PluginAttribute("name") name: String,
    @PluginAttribute("ignoreExceptions") ignoreExceptions: Boolean,
    @PluginAttribute("hostNames") hosts: String,
    @PluginAttribute("port") port: String,
    @PluginAttribute("clusterName") clusterName: String,
    @PluginAttribute("indexNamePrefix") indexNamePrefix: String,
    @PluginAttribute("indexNameDateFormat") indexNameDateFormat: String,
    @PluginAttribute("indexDocumentType") indexDocumentType: String,
    @PluginElement("Layout") layout: Layout[_ <: Serializable],
    @PluginElement("Filters") filter: Filter): ElasticAppender = new ElasticAppender(name, layout, filter, ignoreExceptions, hosts, port, clusterName, indexNamePrefix, indexNameDateFormat, indexDocumentType);
}


//TODO Check extra document creation 12k+ vs 10K
@Plugin(name = "Elastic", category = "Core", elementType = "appender", printObject = true)
class ElasticAppender(name: String, layout: Layout[_ <: Serializable], filter: Filter, ignoreExceptions: Boolean, hosts: String, port: String, clusterName: String, indexNamePrefix: String, indexNameDateFormat: String, indexDocumentType: String) extends AbstractAppender(name, filter, layout, ignoreExceptions) {

  lazy val collection = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val index = new ESIndex(DefaultTestESConfig, actualIndexName, DoNothingEventSystem)
    val collection = new ESCollection[LogMessage](index, LoggerESTypes.LogMessage)
    collection
  }

  lazy val actualIndexName =s"$indexNamePrefix.${DateTimeFormat.forPattern(indexNameDateFormat).print(DateTime.now)}"

  override def append(event: LogEvent): Unit = {
    if (event.getSource().getClassName().contains("grizzled.slf4j.Logger"))
      return ;

    if(!(event.isInstanceOf[Log4jLogEvent]))
      return ;

      send(event)
  }

  def send(event: LogEvent) {
    getLayout match{
      case e:ElasticLayout=>
        collection.create(e.toSerializable(event),false,None)
      case _ => println(s"Unsupported layout! $getLayout")
    }
  }
}
