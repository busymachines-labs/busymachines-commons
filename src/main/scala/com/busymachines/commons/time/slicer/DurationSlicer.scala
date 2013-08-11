package com.busymachines.commons.time.slicer

import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.MutableDateTime
import scala.collection.mutable.ListBuffer

trait DurationSlicer {
  def slice(time: DateTime, duration: Duration, nextMarkFunc: DateTime => DateTime): Seq[Duration] = {
    val intervals = ListBuffer[Duration]()
    var offsetTime = new DateTime(time)
    var remainingDuration = duration

    while (remainingDuration.isLongerThan(Duration.ZERO)) {

      // we do this because each month has a different duration
      var nextTimeMark = nextMarkFunc(offsetTime)

      val nextOffsetTime = remainingDuration.isShorterThan(new Duration(nextTimeMark.getMillis() - offsetTime.getMillis())) match {
        case true =>
          val next = new MutableDateTime(offsetTime)
          next.add(remainingDuration.toPeriod())
          next
        case false => nextTimeMark
      }

      val intervalDuration: Duration = new Duration(offsetTime.getMillis(), nextOffsetTime.getMillis())
      intervals += intervalDuration
      remainingDuration = remainingDuration.minus(intervalDuration)
      offsetTime = nextOffsetTime.toDateTime()
    }

    intervals

  }
  def slice(time: DateTime, duration: Duration): Seq[Duration]
  def hasSlices(time: DateTime, duration: Duration): Boolean
}