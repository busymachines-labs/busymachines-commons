package com.busymachines.commons.domain

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
import com.busymachines.commons.AbstractEnum
import com.busymachines.commons.CommonEnum
import com.busymachines.commons.EnumValue

object CommonJsonFormats extends CommonJsonFormats

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

  def enumFormat[V <: EnumValue[V]](enum: AbstractEnum[V]) = new JsonFormat[V] {
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

  implicit val jodaLocalDateFormat = new JsonFormat[LocalDate] {
    val format = ISODateTimeFormat.date
    def write(value: LocalDate) = JsString(format.print(value))
    def read(value: JsValue): LocalDate = value match {
      case JsString(s) =>
        try format.parseLocalDate(s)
        catch {
          case e: Throwable => deserializationError("Couldn't convert '" + s + "' to a date-time: " + e.getMessage)
        }
      case s => deserializationError("Couldn't convert '" + s + "' to a date-time")
    }
  }

  val jodaDateTimeParser = ISODateTimeFormat.dateOptionalTimeParser.withZoneUTC
  val jodaDateTimeParserFormatter = ISODateTimeFormat.dateTime.withZoneUTC

  implicit val jodaDateTimeFormat = new JsonFormat[DateTime] {
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

  implicit val jodaDurationFormat = new JsonFormat[org.joda.time.Duration] {
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

  implicit val geoPointFormat = format3(GeoPoint)

  implicit val stringMapFormat = new JsonFormat[Map[String, String]] {
    def write(value: Map[String, String]) = JsObject(value.mapValues(JsString(_)).toList)
    def read(value: JsValue): Map[String, String] = value match {
      case JsObject(fields) => fields.map(t => (t._1, t._2.toString)).toMap
      case s => deserializationError("Couldn't convert '" + s + "' to a string map")
    }
  }

  implicit val unitOfMeasureFormat = stringFormat("UnitOfMeasure", s => UnitOfMeasure(s))

  implicit val localeJsonFormat = stringFormat[Locale]("Locale", {
    case "" => Locale.ROOT
    case tag => Locale.forLanguageTag(tag)
  }, {
    case Locale.ROOT => ""
    case locale => locale.getLanguage
  })

  implicit def idFormat[A] = stringWrapperFormat(Id[A])
  implicit def mimeTypeFormat = stringWrapperFormat(MimeType)
  implicit val mediaFormat = format4(Media)
  implicit val moneyFormat = format2(Money)

  implicit val termFacetValueFormat = format2(TermFacetValue)
  implicit val histogramValueFormat = format7(HistogramFacetValue)

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

  implicit def versionedFormat[T <: HasId[T]](implicit tFormat: JsonFormat[T]) = format2(Versioned[T])

  class SearchResultFormat[T <: HasId[T]](fieldName: String)(implicit tFormat: JsonFormat[T]) extends RootJsonWriter[SearchResult[T]] {
    def write(result: SearchResult[T]) = {
      val jsonResult = JsArray(result.result.map(_.entity).map(tFormat.write))
      val jsonFacetResult = JsObject(result.facets.map(facet => facet._1 -> JsArray(facet._2.map {
        case v: HistogramFacetValue => histogramValueFormat.write(v)
        case v: TermFacetValue => termFacetValueFormat.write(v)
        })))
      result.totalCount match {
        case Some(totalCount) => JsObject(fieldName -> jsonResult, "totalCount" -> JsNumber(totalCount), "facets" -> jsonFacetResult)
        case None => JsObject(fieldName -> jsonResult)
      }
    }
  }

  implicit val sequenceFormat = format2(Sequence)
}
