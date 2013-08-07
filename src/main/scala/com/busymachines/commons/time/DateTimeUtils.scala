package com.busymachines.commons.time

import org.joda.time.DateTime
import org.joda.time.LocalDateTime

object DateTimeUtils {
	def dateTimeAtHourOffset(timestamp:DateTime) = timestamp.withFields(new LocalDateTime(timestamp.getYear(),timestamp.getMonthOfYear(),timestamp.getDayOfMonth(),timestamp.getHourOfDay(),0,0,0))
	def dateTimeAtDayOffset(timestamp:DateTime) = timestamp.withFields(new LocalDateTime(timestamp.getYear(),timestamp.getMonthOfYear(),timestamp.getDayOfMonth(),0,0,0,0))
	def dateTimeAtWeekOffset(timestamp:DateTime) = dateTimeAtDayOffset(timestamp.minusDays(timestamp.getDayOfWeek()).plusDays(1))
	def dateTimeAtMonthOffset(timestamp:DateTime) = timestamp.withFields(new LocalDateTime(timestamp.getYear(),timestamp.getMonthOfYear(),1,0,0,0,0))
	def dateTimeAtYearOffset(timestamp:DateTime) = timestamp.withFields(new LocalDateTime(timestamp.getYear(),1,1,0,0,0))

	def dateTimeOpenIntervalCurrentDay(timestamp:DateTime):(DateTime,DateTime) = {
	  val startTime = dateTimeAtDayOffset(timestamp)
	  (startTime,startTime.plusDays(1).minusMillis(1))
	}

	def dateTimeOpenIntervalCurrentWeek(timestamp:DateTime):(DateTime,DateTime) = {
	  val startTime = dateTimeAtWeekOffset(timestamp)
	  (startTime,startTime.plusDays(7).minusMillis(1))
	}
	
	def dateTimeOpenIntervalCurrentMonth(timestamp:DateTime):(DateTime,DateTime) = {
	  val startTime = dateTimeAtMonthOffset(timestamp)
	  (startTime,startTime.plusMonths(1).minusMillis(1))
	}

	def dateTimeOpenIntervalCurrentYear(timestamp:DateTime):(DateTime,DateTime) = {
	  val startTime = dateTimeAtYearOffset(timestamp)
	  (startTime,startTime.plusYears(1).minusMillis(1))
	}
	
}