package com.busymachines.commons.test.time

import org.joda.time.DateTime
import org.scalatest.FlatSpec
import com.busymachines.commons.time.slicer.HourSlicer
import org.joda.time.Minutes
import org.joda.time.Duration

class HourSlicerTest extends FlatSpec {

  val time = DateTime.parse("2013-01-15T10:00:00Z")

  "The hour slicer" should "detect slice durations at hour level" in {
    assert(HourSlicer.hasSlices(time, Minutes.minutes(59).toStandardDuration()) == false)
    assert(HourSlicer.hasSlices(time, Minutes.minutes(60).toStandardDuration()) == true)
    assert(HourSlicer.hasSlices(time, Minutes.minutes(120).toStandardDuration()) == true)
    assert(HourSlicer.hasSlices(time, Minutes.minutes(121).toStandardDuration()) == true)
  }

  "The hour slicer" should "slice durations at hour level" in {
    assert(HourSlicer.slice(time, Minutes.minutes(59).toStandardDuration()).size == 1)
    assert(HourSlicer.slice(time, Minutes.minutes(60).toStandardDuration()).size == 1)
    assert(HourSlicer.slice(time, Minutes.minutes(120).toStandardDuration()).size == 2)
    assert(HourSlicer.slice(time, Minutes.minutes(121).toStandardDuration()).size == 3)
  }

  "The hour slicer" should "slice durations starting at the correct times" in {
    
    assert(Duration.standardMinutes(59).isEqual(HourSlicer.slice(time, Minutes.minutes(59).toStandardDuration())(0)) == true)
    assert(Duration.standardHours(1).isEqual(HourSlicer.slice(time, Minutes.minutes(60).toStandardDuration())(0)) == true)

    assert(Duration.standardHours(1).isEqual(HourSlicer.slice(time, Minutes.minutes(120).toStandardDuration())(0)) == true)
    assert(Duration.standardHours(1).isEqual(HourSlicer.slice(time, Minutes.minutes(120).toStandardDuration())(1)) == true)

    assert(Duration.standardHours(1).isEqual(HourSlicer.slice(time, Minutes.minutes(121).toStandardDuration())(0)) == true)
    assert(Duration.standardHours(1).isEqual(HourSlicer.slice(time, Minutes.minutes(121).toStandardDuration())(1)) == true)
    assert(Duration.standardMinutes(1).isEqual(HourSlicer.slice(time, Minutes.minutes(121).toStandardDuration())(2)) == true)

  }

}