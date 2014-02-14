package com.busymachines.commons.test.time

import org.joda.time.DateTime
import org.scalatest.FlatSpec
import com.busymachines.commons.time.slicer.DaySlicer
import org.joda.time.Hours
import org.joda.time.Duration
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DaySlicerTest extends FlatSpec {

  val time = DateTime.parse("2013-01-15T00:00:00Z")

  "The day slicer" should "detect slice durations at day level" in {
    assert(DaySlicer.hasSlices(time, Hours.hours(23).toStandardDuration()) == false)
    assert(DaySlicer.hasSlices(time, Hours.hours(24).toStandardDuration()) == true)
    assert(DaySlicer.hasSlices(time, Hours.hours(48).toStandardDuration()) == true)
    assert(DaySlicer.hasSlices(time, Hours.hours(49).toStandardDuration()) == true)
  }

  "The day slicer" should "slice durations at day level" in {
    assert(DaySlicer.slice(time, Hours.hours(23).toStandardDuration()).size == 1)
    assert(DaySlicer.slice(time, Hours.hours(24).toStandardDuration()).size == 1)
    assert(DaySlicer.slice(time, Hours.hours(48).toStandardDuration()).size == 2)
    assert(DaySlicer.slice(time, Hours.hours(49).toStandardDuration()).size == 3)
  }

  "The day slicer" should "slice durations starting at the correct times" in {
    
    assert(Duration.standardHours(23).isEqual(DaySlicer.slice(time, Hours.hours(23).toStandardDuration())(0)) == true)
    assert(Duration.standardHours(24).isEqual(DaySlicer.slice(time, Hours.hours(24).toStandardDuration())(0)) == true)

    assert(Duration.standardDays(1).isEqual(DaySlicer.slice(time, Hours.hours(48).toStandardDuration())(0)) == true)
    assert(Duration.standardDays(1).isEqual(DaySlicer.slice(time, Hours.hours(48).toStandardDuration())(1)) == true)

    assert(Duration.standardDays(1).isEqual(DaySlicer.slice(time, Hours.hours(49).toStandardDuration())(0)) == true)
    assert(Duration.standardDays(1).isEqual(DaySlicer.slice(time, Hours.hours(49).toStandardDuration())(1)) == true)
    assert(Duration.standardHours(1).isEqual(DaySlicer.slice(time, Hours.hours(49).toStandardDuration())(2)) == true)

  }

}