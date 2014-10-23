package com.busymachines.commons.domain

import com.busymachines.commons.{EnumValue, Enum}
import org.joda.time.DateTimeZone

trait TimeZone extends EnumValue[TimeZone]

object TimeZone extends Enum[TimeZone] {

  case class Value(name: String, gmtOffsetInMinutes: Int) extends Val with NextId with TimeZone {
    def dateTimeZone:DateTimeZone = DateTimeZone.forID(name)
  }

  val Europe_Amsterdam = Value("Europe/Amsterdam", 120)
  val Europe_Berlin = Value("Europe/Berlin", 120)
  val Europe_Bucharest = Value("Europe/Bucharest", 180)
  val Europe_London = Value("Europe/London", 60)
  val Europe_Paris = Value("Europe/Paris", 120)

}
