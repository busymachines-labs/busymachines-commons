package com.busymachines.commons

import com.busymachines.commons.Implicits._
import org.scalatest.FlatSpec
import _root_.spray.json._
import org.joda.time.LocalDate
import _root_.spray.json.DefaultJsonProtocol._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DateTimeTests extends FlatSpec {

  "Time-zone information" should "be stripped from date-time string" in {
    val dateString = "1971-12-11T00:00:00+02:00"
    val localDate = jodaLocalDateFormat.read(JsString(dateString))
    println(localDate)
  }
  
}