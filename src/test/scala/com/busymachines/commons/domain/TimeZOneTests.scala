package com.busymachines.commons.domain

import org.joda.time.DateTimeZone
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TimeZoneTests extends FlatSpec {

	"TimeZone" should "generate a DateTimeZone based on name" in {

	  assert(TimeZone.Europe_Bucharest.dateTimeZone === DateTimeZone.forID("Europe/Bucharest"))
	}
}