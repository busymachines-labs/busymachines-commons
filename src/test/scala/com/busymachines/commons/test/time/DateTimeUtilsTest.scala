package com.busymachines.commons.test.time
import org.scalatest.FlatSpec
import com.busymachines.commons.time.DateTimeUtils
import org.joda.time.DateTime

class DateTimeUtilsTest extends FlatSpec {
  //  DateTime.parse("2013-02-15T10:35:10Z") is Friday

  "The datetime utils" should "function correctly" in {
    assert(DateTimeUtils.dateTimeAtHourOffset(DateTime.parse("2013-02-15T10:35:10Z")) === DateTime.parse("2013-02-15T10:00:00Z"))
    assert(DateTimeUtils.dateTimeAtDayOffset(DateTime.parse("2013-02-15T10:35:10Z")) === DateTime.parse("2013-02-15T00:00:00Z"))
    assert(DateTimeUtils.dateTimeAtWeekOffset(DateTime.parse("2013-02-15T10:35:10Z")) === DateTime.parse("2013-02-11T00:00:00Z")) // Monday
    assert(DateTimeUtils.dateTimeAtMonthOffset(DateTime.parse("2013-02-15T10:35:10Z")) === DateTime.parse("2013-02-01T00:00:00Z"))
    assert(DateTimeUtils.dateTimeAtYearOffset(DateTime.parse("2013-02-15T10:35:10Z")) === DateTime.parse("2013-01-01T00:00:00Z"))

    assert(DateTimeUtils.dateTimeOpenIntervalCurrentDay(DateTime.parse("2013-02-11T11:22:33Z")) === (DateTime.parse("2013-02-11T00:00:00Z"), DateTime.parse("2013-02-11T23:59:59.999Z")))
    assert(DateTimeUtils.dateTimeOpenIntervalCurrentWeek(DateTime.parse("2013-02-15T10:35:10Z")) === (DateTime.parse("2013-02-11T00:00:00Z"), DateTime.parse("2013-02-17T23:59:59.999Z")))
    assert(DateTimeUtils.dateTimeOpenIntervalCurrentMonth(DateTime.parse("2013-02-15T10:35:10Z")) === (DateTime.parse("2013-02-01T00:00:00Z"), DateTime.parse("2013-02-28T23:59:59.999Z")))
  }

}