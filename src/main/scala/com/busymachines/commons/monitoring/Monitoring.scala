package com.busymachines.commons.monitoring

import org.joda.time.DateTime
import spray.json.JsonWriter
import com.busymachines.commons.spray.json.ProductFormat
import com.codahale.metrics.Meter
import com.codahale.metrics.Gauge
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Counter
import com.codahale.metrics.Timer
import com.codahale.metrics.Histogram

trait MonitoringEvent 

object DiskFull extends MonitoringEvent

trait Monitoring {
  def meter(name: String): Meter
  def counter(name: String): Counter
  def gauge[A](name: String)(f: => A): Gauge[A]
  def sendEvent[A <: MonitoringEvent](obj: A)(implicit fmt: ProductFormat[A])
}

class MonitoringImpl extends Monitoring {
  private val metrics = new MetricRegistry
  def meter(name: String): Meter = 
    metrics.meter(name)

  def counter(name: String): Counter = 
    metrics.counter(name)

  def timer(name: String): Timer = 
    metrics.timer(name)

  def histogram(name: String): Histogram = 
    metrics.histogram(name)

  def gauge[A](name: String)(f: => A): Gauge[A] = {
    new Gauge[A] {
      def getValue = f
      override def toString = name
    }  
  }
  
  def sendEvent[A <: MonitoringEvent](obj: A)(implicit fmt: ProductFormat[A]) {
  }
  
}