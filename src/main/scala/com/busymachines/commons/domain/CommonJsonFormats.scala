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
import com.busymachines.commons.domain.Media

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
  
  def singletonFormat[A](instance : A) = new JsonFormat[A] {
    def write(value : A) = JsObject()
    def read(value : JsValue) = instance
  }

  def stringJsonFormat[A](type_ : String, fromStr: String => A, toStr: A => String = (a : A) => a.toString) = new JsonFormat[A] {
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

  implicit val stringMapFormat = new JsonFormat[Map[String, String]] {
    def write(value: Map[String, String]) = JsObject(value.mapValues(JsString(_)).toList)
    def read(value: JsValue): Map[String, String] = value match {
      case JsObject(fields) => fields.map(t => (t._1, t._2.toString)).toMap
      case s => deserializationError("Couldn't convert '" + s + "' to a string map")
    }
  }
/*
  implicit val localeJsonFormat = stringJsonFormat[Locale]("Locale", _ match {
    case "und" => Locale.ROOT 
    case tag => Locale.forLanguageTag(tag)
  }, _.toLanguageTag)
*/
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
 }