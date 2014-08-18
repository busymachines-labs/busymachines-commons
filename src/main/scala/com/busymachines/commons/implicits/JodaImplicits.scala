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
import org.joda.time.JodaAccessor

class RichLocalDateTime(val localDateTime: LocalDateTime) extends AnyVal {
  def localMillis: Long = JodaAccessor.localMillis(localDateTime)
}
class RichLocalDate(val localDate: LocalDate) extends AnyVal {
  def localMillis: Long = JodaAccessor.localMillis(localDate)
}

trait JodaImplicits { this: CommonJsonFormats =>

  implicit def toLocalDateTime(localDateTime: LocalDateTime) = new RichLocalDateTime(localDateTime)
  implicit def toLocalDate(localDate: LocalDate) = new RichLocalDate(localDate)

  implicit val jodaTimeZoneFormat = stringFormat("JodaTimeZone", s => DateTimeZone.forID(s))

  implicit val jodaLocalDateFormat = new RootJsonFormat[LocalDate] {
    def write(value: LocalDate) = JsString(value.toString)
    def read(value: JsValue): LocalDate = value match {
      case JsString(s) =>
        try DateTime.parse(s).toLocalDate
        catch {
          case e: Throwable => deserializationError("Couldn't convert '" + s + "' to a local date: " + e.getMessage)
        }
      case s => deserializationError("Couldn't convert '" + s + "' to a local date")
    }
  }

  implicit val jodaLocalDateTimeFormat = new RootJsonFormat[LocalDateTime] {
    def write(value: LocalDateTime) = JsString(value.toString)
    def read(value: JsValue): LocalDateTime = value match {
      case JsString(s) =>
        try DateTime.parse(s).toLocalDateTime
        catch {
          case e: Throwable => deserializationError("Couldn't convert '" + s + "' to a local date-time: " + e.getMessage)
        }
      case s => deserializationError("Couldn't convert '" + s + "' to a local date-time")
    }
  }

  implicit val jodaDateTimeFormat = new RootJsonFormat[DateTime] {
    def write(value: DateTime) = JsString(value.toString())
    def read(value: JsValue): DateTime = value match {
      case JsString(s) =>
        try DateTime.parse(s)
        catch {
          case e: Throwable => deserializationError("Couldn't convert '" + s + "' to a date-time: " + e.getMessage)
        }
      case s => deserializationError("Couldn't convert '" + s + "' to a date-time")
    }
  }

  //we use the representation in this standard: http://en.wikipedia.org/wiki/ISO_8601
  implicit val jodaDurationFormat = new RootJsonFormat[org.joda.time.Duration] {
    def write(value: org.joda.time.Duration) = JsString(value.toString)
    def read(value: JsValue): org.joda.time.Duration = value match {
      case JsString(s) =>
        try {
          org.joda.time.Duration.parse(s)
        } catch {
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