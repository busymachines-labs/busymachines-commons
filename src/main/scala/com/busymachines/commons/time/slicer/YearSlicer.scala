package com.busymachines.commons.time.slicer

import org.joda.time.MutableDateTime
import org.joda.time.DateTime
import org.joda.time.Duration

object YearSlicer extends DurationSlicer {
	def hasSlices(time:DateTime,duration:Duration):Boolean = time.getYear() != time.plus(duration).getYear()
	def slice(time:DateTime,duration:Duration):Seq[Duration] = slice(time,duration,nextMark)
	def nextMark(offsetTime:DateTime):DateTime = {
	  	    var next = new MutableDateTime(offsetTime)
		    next.addYears(1)
	  	    next.setMonthOfYear(1)
		    next.setDayOfMonth(1)
	        next.setHourOfDay(0)
		    next.setMinuteOfHour(0)
		    next.setSecondOfMinute(0)
		    next.setMillisOfSecond(0)
		    next.toDateTime()
	}
}