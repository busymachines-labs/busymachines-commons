package com.busymachines.commons.implicits

import java.util.Currency
import java.util.Locale
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import spray.json._
import com.busymachines.commons.dao.SearchResult
import com.busymachines.commons.dao.Versioned
import scala.concurrent.duration.FiniteDuration
import org.joda.time.LocalDate
import com.busymachines.commons.dao.TermFacetValue
import com.busymachines.commons.dao.HistogramFacetValue
import com.busymachines.commons.spray.ProductFormatsInstances
import com.busymachines.commons.Enum
import com.busymachines.commons.EnumValue
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import com.busymachines.commons.localisation.Country
import com.busymachines.commons.localisation.Language

trait CommonJsonFormats
  extends BasicFormats
  with StandardFormats
  with CollectionFormats
  with ProductFormats
  with ProductFormatsInstances
  with AdditionalFormats {
  implicit object currencyJsonFormat extends RootJsonFormat[Currency] {
    def write(c: Currency) =
      JsString(c.toString)

    def read(value: JsValue) = value match {
      case JsString(x) => Currency.getInstance(x)
      case _ => deserializationError("Currency expected")
    }
  }

  def enumFormat[E <: Enumeration](enum: E) = new JsonFormat[E#Value] {
    def write(value: E#Value) = JsString(value.toString)
    def read(value: JsValue): E#Value = value match {
      case JsString(s) =>
        try enum.withName(s).asInstanceOf[E#Value]
        catch {
          case e: Throwable => deserializationError(s"Expected any of ${enum.values.mkString(",")}, but got " + s)
        }
      case x => deserializationError(s"Expected any of ${enum.values.mkString(",")}, but got " + x)
    }
  }

  def enumFormat[V <: EnumValue[V]](enum: Enum[V]) = new JsonFormat[V] {
    def write(value: V) = JsString(value.toString)
    def read(value: JsValue): V = value match {
      case JsString(s) =>
        try enum.withName(s).asInstanceOf[V]
        catch {
          case e: Throwable => deserializationError(s"Expected any of ${enum.values.mkString(",")}, but got " + s)
        }
      case x => deserializationError(s"Expected any of ${enum.values.mkString(",")}, but got " + x)
    }
  }

  def singletonFormat[A](instance: A) = new JsonFormat[A] {
    def write(value: A) = JsObject()
    def read(value: JsValue) = instance
  }

  /**
   * String format for case classes that wrap a single string.
   */
  def stringWrapperFormat[A <: Product](construct: String => A): JsonFormat[A] =
    stringFormat(construct.getClass.getSimpleName, construct, _.productElement(0).toString)

  def stringFormat[A](type_ : String, fromStr: String => A, toStr: A => String = (a: A) => a.toString) = new JsonFormat[A] {
    def write(value: A) = JsString(toStr(value))
    def read(value: JsValue): A = value match {
      case JsString(s) =>
        try fromStr(s)
        catch {
          case e: NoSuchElementException if e.getMessage == "None.get" => deserializationError("Couldn't find a " + type_ + " for value '" + s + "'")
          case e: Throwable => deserializationError("Couldn't convert '" + s + "' to a " + type_ + ": " + e.getMessage)
        }
      case x => deserializationError("Expected " + type_ + " :: String, but got " + x)
    }
  }

  implicit val stringMapFormat = new JsonFormat[Map[String, String]] {
    def write(value: Map[String, String]) = JsObject(value.mapValues(JsString(_)).toList)
    def read(value: JsValue): Map[String, String] = value match {
      case JsObject(fields) => fields.map(t => (t._1, t._2.toString)).toMap
      case s => deserializationError("Couldn't convert '" + s + "' to a string map")
    }
  }

  implicit val localeJsonFormat = stringFormat[Locale]("Locale", {
    case "" => Locale.ROOT
    case tag => Locale.forLanguageTag(tag)
  }, {
    case Locale.ROOT => ""
    case locale => locale.getLanguage
  })

  implicit val durationFormat = new JsonFormat[FiniteDuration] {
    def write(value: FiniteDuration) = JsObject(
      "length" -> JsNumber(value.length),
      "unit" -> JsString(value.unit.name))
    def read(value: JsValue): FiniteDuration = value match {
      case JsObject(o) =>
        FiniteDuration(
          length = o.getOrElse("length", deserializationError(s"FiniteDuration lacks length field")).toString.toLong,
          unit = o.getOrElse("unit", deserializationError(s"FiniteDuration lacks unit field")).toString)
      case s => deserializationError("Couldn't convert '" + s + "' to a geo point")
    }
  }

  implicit val countryJsonFormat = enumFormat(Country)
  
  implicit val languageJsonFormat = enumFormat(Language)
}