package com.busymachines.commons.domain

import java.util.Currency
import java.util.Locale
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import spray.json.DefaultJsonProtocol
import spray.json.JsObject
import spray.json.JsString
import spray.json.JsValue
import spray.json.JsonFormat
import spray.json.RootJsonFormat
import spray.json.deserializationError
import spray.json.JsObject
import spray.json.JsNumber
import spray.json.JsonWriter
import com.busymachines.commons.dao.FacetField
import com.busymachines.commons.dao.SearchResult
import com.busymachines.commons.dao.FacetField
import spray.json.JsonWriter
import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.dao.Page
import com.busymachines.commons.dao.FacetFieldValue
import spray.json.JsArray
import spray.json.RootJsonWriter
import scala.concurrent.duration.{Duration, FiniteDuration, Deadline}

object CommonJsonFormats extends CommonJsonFormats

trait CommonJsonFormats extends DefaultJsonProtocol {

  implicit object currencyJsonFormat extends RootJsonFormat[Currency] {
    def write(c: Currency) =
      JsString(c.toString())

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

  def singletonFormat[A](instance: A) = new JsonFormat[A] {
    def write(value: A) = JsObject()
    def read(value: JsValue) = instance
  }

  def stringJsonFormat[A](type_ : String, fromStr: String => A, toStr: A => String = (a: A) => a.toString) = new JsonFormat[A] {
    def write(value: A) = JsString(toStr(value))
    def read(value: JsValue): A = value match {
      case JsString(s) =>
        try fromStr(s)
        catch {
          case e: NoSuchElementException if e.getMessage == "None.get" => deserializationError("Couldn't find a " + type_ + " for value '" + s + "'")
          case e: Throwable => deserializationError("Couldn't convert '" + s + "' to a " + type_ + ": " + e.getMessage)
        }
      case x => deserializationError("Expected " + type_ + " as String, but got " + x)
    }
  }

  implicit val dateTimeFormat = new JsonFormat[DateTime] {
    val format = ISODateTimeFormat.dateTime.withZoneUTC
    def write(value: DateTime) = JsString(format.print(value))
    def read(value: JsValue): DateTime = value match {
      case JsString(s) =>
        try format.parseDateTime(s)
        catch {
          case e: Throwable => deserializationError("Couldn't convert '" + s + "' to a date-time: " + e.getMessage)
        }
      case s => deserializationError("Couldn't convert '" + s + "' to a date-time")
    }
  }

  implicit val geoPointFormat = new JsonFormat[GeoPoint] {
    def write(value: GeoPoint) = JsObject(
      "lat" -> JsNumber(value.lat),
      "lon" -> JsNumber(value.lon))
    def read(value: JsValue): GeoPoint = value match {
      case JsObject(o) =>
        GeoPoint(
          lat = o.getOrElse("lat", deserializationError(s"Geopoint lacks lat field")).toString.toDouble,
          lon = o.getOrElse("lon", deserializationError(s"Geopoint lacks lon field")).toString.toDouble)
      case s => deserializationError("Couldn't convert '" + s + "' to a geo point")
    }
  }

  implicit val stringMapFormat = new JsonFormat[Map[String, String]] {
    def write(value: Map[String, String]) = JsObject(value.mapValues(JsString(_)).toList)
    def read(value: JsValue): Map[String, String] = value match {
      case JsObject(fields) => fields.map(t => (t._1, t._2.toString)).toMap
      case s => deserializationError("Couldn't convert '" + s + "' to a string map")
    }
  }

  implicit val unitOfMeasureFormat = stringJsonFormat("UnitOfMeasure", s => UnitOfMeasure(Nil))
  
  implicit val localeJsonFormat = stringJsonFormat[Locale]("Locale", _ match {
    case "und" => Locale.ROOT
    case tag => Locale.forLanguageTag(tag)
  }, _.toLanguageTag)

  //  implicit def localeMapJsonFormat[T: JsonFormat] = new JsonFormat[Map[Locale, T]] {
  //    def write(map: Map[Locale, T]) = map.toList match {
  //      case Nil =>
  //        JsArray()
  //      case (locale, value) :: Nil =>
  //        if (locale.getCountry.isEmpty && locale.getLanguage.isEmpty && locale.getVariant.isEmpty)
  //          value.toJson
  //        else
  //          localeJsonFormat.write(locale, Some("value2", value.toJson))
  //      case entries =>
  //        JsArray(for ((locale, value) <- entries)
  //          yield localeJsonFormat.write(locale, Some("value2", value.toJson)))
  //    }
  //    def read(value: JsValue) = value match {
  //      case JsString(s) => Map.empty
  //      case _ => Map.empty
  //    }
  //  }
  //  
    
  implicit def idFormat[A] = stringJsonFormat[Id[A]]("Id", Id(_))
  implicit val mediaFormat = jsonFormat4(Media)
  
  implicit val facetFieldFormat = new JsonWriter[FacetField] {
    def write(c: FacetField) = JsString(c.toString)
  }
  implicit val facetFieldValueFormat = jsonFormat2(FacetFieldValue)
  
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

  implicit def versionedFormat[T <: HasId[T]](implicit tFormat : JsonFormat[T]) = jsonFormat2(Versioned[T])
  
  class SearchResultFormat[T <: HasId[T]](fieldName : String)(implicit tFormat : JsonFormat[T]) extends RootJsonWriter[SearchResult[T]] {
    def write(result: SearchResult[T]) = {
      val jsonResult = JsArray(result.result.map(_.entity).map(tFormat.write(_)))
      result.totalCount match {
        case Some(totalCount) => JsObject(fieldName -> jsonResult, "totalCount" -> JsNumber(totalCount))
        case None => JsObject(fieldName -> jsonResult)
      }
    } 
  }
}
