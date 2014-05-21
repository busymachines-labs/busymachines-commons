package com.busymachines.commons.monitoring

import org.joda.time.DateTime
import spray.json.JsonWriter
import com.busymachines.commons.spray.ProductFormat
import com.codahale.metrics.Meter
import com.codahale.metrics.Gauge

trait MonitoringEvent {
  def topic: Option[MonitoringEventTopic] = None
}
abstract class MonitoringEventTopic(name: String)

object DiskFull extends MonitoringEvent

trait Monitoring {
  def meter(name: String): Meter
  def gauge[A](name: String)(f: => A): Gauge[A]
  def sendEvent[A <: MonitoringEvent](obj: A)(implicit fmt: ProductFormat[A])
  
  def oneMinuteRatio()
}