package org.joda.time

object JodaAccessor {
  def localMillis(localDateTime: LocalDateTime) = 
    localDateTime.getLocalMillis
  def localMillis(localDate: LocalDate) = 
    localDate.getLocalMillis
}