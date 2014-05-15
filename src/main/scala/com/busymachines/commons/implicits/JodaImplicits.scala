package com.busymachines.commons.implicits

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.ISODateTimeFormat
import spray.json.JsString
import spray.json.JsValue
import spray.json.JsonFormat
import spray.json.deserializationError
import org.joda.time.ReadablePartial
import spray.json.RootJsonFormat
import org.mockito.internal.matchers.EndsWith

trait JodaImplicits { this: CommonJsonFormats =>
  implicit val jodaTimeZoneFormat = stringFormat("JodaTimeZone", s => DateTimeZone.forID(s))
  
  implicit val jodaLocalDateFormat = new RootJsonFormat[LocalDate] {
    val format = ISODateTimeFormat.date
    def write(value: LocalDate) = JsString(format.print(value))
    def read(value: JsValue): LocalDate = value match {
      case JsString(s) =>
        try LocalDate.parse(stripTimeZone(stripTime(s)))
        catch {
          case e: Throwable => deserializationError("Couldn't convert '" + s + "' to a local date: " + e.getMessage)
        }
      case s => deserializationError("Couldn't convert '" + s + "' to a local date")
    }
  }

  implicit val jodaLocalDateTimeFormat = new RootJsonFormat[LocalDateTime] {
    val format = ISODateTimeFormat.date
    def write(value: LocalDateTime) = JsString(format.print(value))
    def read(value: JsValue): LocalDateTime = value match {
      case JsString(s) =>
        try LocalDateTime.parse(stripTimeZone(s))
        catch {
          case e: Throwable => deserializationError("Couldn't convert '" + s + "' to a local date-time: " + e.getMessage)
        }
      case s => deserializationError("Couldn't convert '" + s + "' to a local date-time")
    }
  }

  private def stripTimeZone(s: String): String = 
    if (s.endsWith("Z")) s.dropRight(1)
    else s.lastIndexOf('+') match {
      case -1 => s
      case i => s.substring(0, i)
    }
  
  private def stripTime(s: String): String = 
    s.lastIndexOf('T') match {
      case -1 => s
      case i => s.substring(0, i)
    }
  
  val jodaDateTimeParser = ISODateTimeFormat.dateOptionalTimeParser.withZoneUTC
  val jodaDateTimeParserFormatter = ISODateTimeFormat.dateTime.withZoneUTC

  implicit val jodaDateTimeFormat = new RootJsonFormat[DateTime] {
    def write(value: DateTime) = JsString(jodaDateTimeParserFormatter.print(value))
    def read(value: JsValue): DateTime = value match {
      case JsString(s) =>
        try jodaDateTimeParser.parseDateTime(s)
        catch {
          case e: Throwable => deserializationError("Couldn't convert '" + s + "' to a date-time: " + e.getMessage)
        }
      case s => deserializationError("Couldn't convert '" + s + "' to a date-time")
    }
  }

  implicit val jodaDurationFormat = new RootJsonFormat[org.joda.time.Duration] {
    def write(value: org.joda.time.Duration) = JsString(value.getMillis + "")
    def read(value: JsValue): org.joda.time.Duration = value match {
      case JsString(s) =>
        try org.joda.time.Duration.parse(s)
        catch {
          case e: Throwable => deserializationError("Couldn't convert '" + s + "' to a duration: " + e.getMessage)
        }
      case s => deserializationError("Couldn't convert '" + s + "' to a duration")
    }
  }
  implicit def jodaOrdering[A <: ReadablePartial] = new Ordering[A] {
    def compare(x: A, y: A): Int = 
      x.compareTo(y)
  }
}