package com.busymachines.commons.time.slicer

import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.MutableDateTime

object DaySlicer extends DurationSlicer {
	def hasSlices(time:DateTime,duration:Duration):Boolean = time.getDayOfYear() != time.plus(duration).getDayOfYear()  
	def slice(time:DateTime,duration:Duration):Seq[Duration] = slice(time,duration,nextMark)
	def nextMark(offsetTime:DateTime):DateTime = {
	  	    var next = new MutableDateTime(offsetTime)
		    next.addDays(1)
	        next.setHourOfDay(0)
		    next.setMinuteOfHour(0)
		    next.setSecondOfMinute(0)
		    next.setMillisOfSecond(0)
		    next.toDateTime()
	}
}

