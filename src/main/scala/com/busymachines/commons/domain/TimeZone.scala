package com.busymachines.commons.domain

import com.busymachines.commons.{Enum, EnumValue}
import org.joda.time.DateTimeZone

trait TimeZone extends EnumValue[TimeZone]

object TimeZone extends Enum[TimeZone] {

  case class Value(name: String) extends Val with NextId with TimeZone {
    def dateTimeZone:DateTimeZone = DateTimeZone.forID(name)
  }

  val Europe_Amsterdam = Value("Europe/Amsterdam")
  val Europe_Berlin = Value("Europe/Berlin")
  val Europe_Bucharest = Value("Europe/Bucharest")
  val Europe_London = Value("Europe/London")
  val Europe_Paris = Value("Europe/Paris")

}
