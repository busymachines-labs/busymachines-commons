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
  final type FiniteDuration = d.FiniteDuration
  @inline final def FiniteDuration: d.FiniteDuration.type = d.FiniteDuration

  @inline final def Nanos:   TimeUnit = TimeUnit.NANOSECONDS
  @inline final def Micros:  TimeUnit = TimeUnit.MICROSECONDS
  @inline final def Millis:  TimeUnit = TimeUnit.MILLISECONDS
  @inline final def Seconds: TimeUnit = TimeUnit.SECONDS
  @inline final def Minutes: TimeUnit = TimeUnit.MINUTES
  @inline final def Hours:   TimeUnit = TimeUnit.HOURS
  @inline final def Days:    TimeUnit = TimeUnit.DAYS

  @inline final def nanos(s:   Long): FiniteDuration = d.FiniteDuration(s, Nanos)
  @inline final def micros(s:  Long): FiniteDuration = d.FiniteDuration(s, Micros)
  @inline final def millis(s:  Long): FiniteDuration = d.FiniteDuration(s, Millis)
  @inline final def seconds(s: Long): FiniteDuration = d.FiniteDuration(s, Seconds)
  @inline final def minutes(s: Long): FiniteDuration = d.FiniteDuration(s, Minutes)
  @inline final def hours(s:   Long): FiniteDuration = d.FiniteDuration(s, Hours)
  @inline final def days(s:    Long): FiniteDuration = d.FiniteDuration(s, Days)
}
