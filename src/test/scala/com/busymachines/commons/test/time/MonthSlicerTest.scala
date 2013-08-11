package com.busymachines.commons.test.time

import org.joda.time.DateTime
import org.scalatest.FlatSpec
import com.busymachines.commons.time.slicer.MonthSlicer
import org.joda.time.Months
import org.joda.time.Days
import org.joda.time.Duration

class MonthSlicerTest extends FlatSpec {

  // March has 31 days
  // April has 30 days
  val time = DateTime.parse("2013-03-01T00:00:00Z")

  "The month slicer" should "detect slice durations at month level" in {
    assert(MonthSlicer.hasSlices(time, Days.days(30).toStandardDuration()) == false)
    assert(MonthSlicer.hasSlices(time, Days.days(31).toStandardDuration()) == true)
    assert(MonthSlicer.hasSlices(time, Days.days(61).toStandardDuration()) == true)
    assert(MonthSlicer.hasSlices(time, Days.days(62).toStandardDuration()) == true)
  }

  "The month slicer" should "slice durations at month level" in {
    assert(MonthSlicer.slice(time, Days.days(30).toStandardDuration()).size == 1)
    assert(MonthSlicer.slice(time, Days.days(31).toStandardDuration()).size == 1)
    assert(MonthSlicer.slice(time, Days.days(61).toStandardDuration()).size == 2)
    assert(MonthSlicer.slice(time, Days.days(62).toStandardDuration()).size == 3)
  }

  "The month slicer" should "slice durations starting at the correct times" in {
    
    assert(Duration.standardDays(30).isEqual(MonthSlicer.slice(time, Days.days(30).toStandardDuration())(0)) == true)
    assert(Duration.standardDays(31).isEqual(MonthSlicer.slice(time, Days.days(31).toStandardDuration())(0)) == true)

    assert(Duration.standardDays(31).isEqual(MonthSlicer.slice(time, Days.days(61).toStandardDuration())(0)) == true)
    assert(Duration.standardDays(30).isEqual(MonthSlicer.slice(time, Days.days(61).toStandardDuration())(1)) == true)

    assert(Duration.standardDays(31).isEqual(MonthSlicer.slice(time, Days.days(62).toStandardDuration())(0)) == true)
    assert(Duration.standardDays(30).isEqual(MonthSlicer.slice(time, Days.days(62).toStandardDuration())(1)) == true)
    assert(Duration.standardDays(1).isEqual(MonthSlicer.slice(time, Days.days(62).toStandardDuration())(2)) == true)
  }

}