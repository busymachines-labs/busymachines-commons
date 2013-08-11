package com.busymachines.commons.test.time

import org.joda.time.DateTime
import org.scalatest.FlatSpec
import com.busymachines.commons.time.slicer.YearSlicer
import org.joda.time.Days
import org.joda.time.Duration

class YearSlicerTest extends FlatSpec {

  // 2012 has 366 days (leap year)
  // 2013 has 365 days
  val time = DateTime.parse("2012-01-01T00:00:00Z")

  "The year slicer" should "detect slice durations at year level" in {
    assert(YearSlicer.hasSlices(time, Days.days(365).toStandardDuration()) == false)
    assert(YearSlicer.hasSlices(time, Days.days(366).toStandardDuration()) == true)
    assert(YearSlicer.hasSlices(time, Days.days(365+366).toStandardDuration()) == true)
    assert(YearSlicer.hasSlices(time, Days.days(365+366+1).toStandardDuration()) == true)
  }

  "The year slicer" should "slice durations at year level" in {
    assert(YearSlicer.slice(time, Days.days(365).toStandardDuration()).size == 1)
    assert(YearSlicer.slice(time, Days.days(366).toStandardDuration()).size == 1)
    assert(YearSlicer.slice(time, Days.days(365+366).toStandardDuration()).size == 2)
    assert(YearSlicer.slice(time, Days.days(365+366+1).toStandardDuration()).size == 3)
  }

  "The year slicer" should "slice durations starting at the correct times" in {
    
    assert(Duration.standardDays(365).isEqual(YearSlicer.slice(time, Days.days(365).toStandardDuration())(0)) == true)
    assert(Duration.standardDays(366).isEqual(YearSlicer.slice(time, Days.days(366).toStandardDuration())(0)) == true)

    assert(Duration.standardDays(366).isEqual(YearSlicer.slice(time, Days.days(365+366).toStandardDuration())(0)) == true)
    assert(Duration.standardDays(365).isEqual(YearSlicer.slice(time, Days.days(365+366).toStandardDuration())(1)) == true)

    assert(Duration.standardDays(366).isEqual(YearSlicer.slice(time, Days.days(365+366+1).toStandardDuration())(0)) == true)
    assert(Duration.standardDays(365).isEqual(YearSlicer.slice(time, Days.days(365+366+1).toStandardDuration())(1)) == true)
    assert(Duration.standardDays(1).isEqual(YearSlicer.slice(time, Days.days(365+366+1).toStandardDuration())(2)) == true)
  }

}