package busymachines.duration

import scala.concurrent.{duration => d}
import java.util.concurrent.TimeUnit

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 09 Jan 2018
  *
  */
trait DurationDefinitions {
  type FiniteDuration = d.FiniteDuration
  @inline def FiniteDuration: d.FiniteDuration.type = d.FiniteDuration

  @inline def Nanos:   TimeUnit = TimeUnit.NANOSECONDS
  @inline def Micros:  TimeUnit = TimeUnit.MICROSECONDS
  @inline def Millis:  TimeUnit = TimeUnit.MILLISECONDS
  @inline def Seconds: TimeUnit = TimeUnit.SECONDS
  @inline def Minutes: TimeUnit = TimeUnit.MINUTES
  @inline def Hours:   TimeUnit = TimeUnit.HOURS
  @inline def Days:    TimeUnit = TimeUnit.DAYS

  @inline def nanos(s:   Long): FiniteDuration = d.FiniteDuration(s, Nanos)
  @inline def micros(s:  Long): FiniteDuration = d.FiniteDuration(s, Micros)
  @inline def millis(s:  Long): FiniteDuration = d.FiniteDuration(s, Millis)
  @inline def seconds(s: Long): FiniteDuration = d.FiniteDuration(s, Seconds)
  @inline def minutes(s: Long): FiniteDuration = d.FiniteDuration(s, Minutes)
  @inline def hours(s:   Long): FiniteDuration = d.FiniteDuration(s, Hours)
  @inline def days(s:    Long): FiniteDuration = d.FiniteDuration(s, Days)
}
