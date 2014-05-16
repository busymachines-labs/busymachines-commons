package com.busymachines.commons

import com.busymachines.commons.Implicits._
import org.scalatest.FlatSpec
import _root_.spray.json._
import org.joda.time.LocalDate
import _root_.spray.json.DefaultJsonProtocol._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.LocalDateTime
import java.util.Date

@RunWith(classOf[JUnitRunner])
class DateTimeTests extends FlatSpec {

  "Time-zone information" should "be stripped from date-time string" in {
    val dateString = "1971-12-11T00:00:00+02:00"
    val date = DateTime.parse(dateString)
    val localDate = date.toLocalDate
    println(date)
    println(localDate)
    assert("1971-12-11" === localDate.toString)
  }
  
  "Time-zone and time information" should "be optional on a date-time string" in {
    assert("1971-12-11T00:00:00.000+02:00" === DateTime.parse("1971-12-11T00:00:00+02:00").toString())
    assert("1971-12-11T00:00:00.000+02:00" === DateTime.parse("1971-12-11T00:00+02:00").toString())
    assert("1971-12-11T00:00:00.000" === DateTime.parse("1971-12-11").toLocalDateTime.toString())
    assert("1971-12-11T00:00:00.000" === DateTime.parse("1971-12-11T00:00").toLocalDateTime.toString())
    assert("1971-12-11T00:00:00.000" === DateTime.parse("1971-12-11T00:00+02:00").toLocalDateTime.toString())
    assert("1971-12-11" === DateTime.parse("1971-12-11").toLocalDate.toString())
    assert("1971-12-11" === DateTime.parse("1971-12-11T00:00").toLocalDate.toString())
    assert("1971-12-11" === DateTime.parse("1971-12-11T00:00+02:00").toLocalDate.toString())
    assert("1971-12-11T00:00:00.000+02:00" === DateTime.parse("1971-12-11T00:00:00+02:00").toString())
    assert(DateTime.parse("1971-12-11T00:00:00Z").toLocalDate === DateTime.parse("1971-12-11T00:00:00+02:00").toLocalDate)
  }
  
  "Local date-time" should "be correctly converted to Date" in {
    assert("1971-12-11T00:00:00.000Z" === LocalDateTime.parse("1971-12-11T00:00").toDateTime(DateTimeZone.UTC).toString())
    assert("1971-12-11T00:00:00.000" === new DateTime((DateTime.parse("1971-12-11T00:00:00.000Z").toDate)).withZone(DateTimeZone.UTC).toLocalDateTime.toString())
    assert("1971-12-11T00:00:00.000" === new DateTime((DateTime.parse("1971-12-11T00:00:00.000Z").toDate.getTime)).withZone(DateTimeZone.UTC).toLocalDateTime.toString())
    assert("1971-12-11T00:00:00.000" === new DateTime((DateTime.parse("1971-12-11T00:00:00.000Z").toDate.getTime)).withZone(DateTimeZone.UTC).toLocalDateTime.toString())
    val date = DateTime.parse("1971-12-11T00:00:00.000Z").toDate
    val localDateTime = DateTime.parse("1971-12-11T00:00:00.000").toLocalDateTime
    assert(localDateTime === toLocalDateTime(date))
    assert(date === toDate(localDateTime))
    println(toLocalDateTime(date))
  }
  
  private def toDate(local: LocalDateTime): Date = 
    local.toDateTime(DateTimeZone.UTC).toDate

  private def toLocalDateTime(date: Date): LocalDateTime = 
    new DateTime(date.getTime).withZone(DateTimeZone.UTC).toLocalDateTime

}