package busymachines.duration

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Feb 2018
  *
  */
object TimeUnits {
  @inline def Nanos:   TimeUnit = java.util.concurrent.TimeUnit.NANOSECONDS
  @inline def Micros:  TimeUnit = java.util.concurrent.TimeUnit.MICROSECONDS
  @inline def Millis:  TimeUnit = java.util.concurrent.TimeUnit.MILLISECONDS
  @inline def Seconds: TimeUnit = java.util.concurrent.TimeUnit.SECONDS
  @inline def Minutes: TimeUnit = java.util.concurrent.TimeUnit.MINUTES
  @inline def Hours:   TimeUnit = java.util.concurrent.TimeUnit.HOURS
  @inline def Days:    TimeUnit = java.util.concurrent.TimeUnit.DAYS
}
