package com.busymachines.commons.event

import spray.json.JsonWriter
import org.joda.time.DateTime
import com.busymachines.prefab.media.domain.Media


trait BusEvent {
  def topic: Option[String] = None
}

trait EventBus {
  def subscribe(f: PartialFunction[BusEvent, Any]): Unit
  def publish(event: BusEvent):Unit
}

//trait MonitoredEvent { this: BusEvent => }
//class SensuEventAdapter(eventBus: EventBus) {
//  eventBus.subscribe {
//    case s: MonitoredEvent => sendToSensu(s)
//  }
//}
//
//
//trait Logging2 {
//  type KeyValue[A] = (String, A, JsonWriter[A])
//  implicit def toKeyValue[A](nameValue: (String, A))(implicit writer: JsonWriter[A]) = 
//    (nameValue._1, nameValue._2, writer)
//  def info(message: String, keyValues: KeyValue[_]*)
//  def info[A](message: String, obj: A)(implicit writer: ProductFormat[A])
//}
//
//object App {
//  trait ErrorEvent extends MonitoredEvent { this: BusEvent => }
//  trait WarningEvent extends MonitoredEvent { this: BusEvent => }
//  class TimeoutEvent(timeout: Int) extends BusEvent
//  trait UserTopic  { this: BusEvent =>
//    override val topic = Some("user")
//  }  
//  
//}
//
//
//class App(eventBus: EventBus) {
//  import App._  
//  if (false) {
//    eventBus.publish(new TimeoutEvent(200) with UserTopic with ErrorEvent)
//  }
//}
//
//class CustomerManager(eventBus: EventBus) extends Logging2 {
//  import App._
//  import com.busymachines.commons.Implicits._
//  import com.busymachines.prefab.media.Implicits._
//  val logger: Logging2 = new EventLogger(eventBus)
//  
//  val date: DateTime
//  val media: Media
//  logger.info("something happened", "date" -> date, "media" -> media)
//  logger.info("something happened", media)
//  eventBus.publish(new TimeoutEvent(200) with UserTopic with ErrorLevel with MonitoredEvent with LogEvent)
//  
//}
//
