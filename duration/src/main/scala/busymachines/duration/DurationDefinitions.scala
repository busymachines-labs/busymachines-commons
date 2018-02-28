package busymachines.duration

import scala.concurrent.{duration => d}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 09 Jan 2018
  *
  */
trait DurationDefinitions {
  final type FiniteDuration = d.FiniteDuration
  final type Duration       = d.Duration

  @inline final def FiniteDuration: d.FiniteDuration.type = d.FiniteDuration
  @inline final def nanos(s:   Long): FiniteDuration = d.FiniteDuration(s, TimeUnits.Nanos)
  @inline final def micros(s:  Long): FiniteDuration = d.FiniteDuration(s, TimeUnits.Micros)
  @inline final def millis(s:  Long): FiniteDuration = d.FiniteDuration(s, TimeUnits.Millis)
  @inline final def seconds(s: Long): FiniteDuration = d.FiniteDuration(s, TimeUnits.Seconds)
  @inline final def minutes(s: Long): FiniteDuration = d.FiniteDuration(s, TimeUnits.Minutes)
  @inline final def hours(s:   Long): FiniteDuration = d.FiniteDuration(s, TimeUnits.Hours)
  @inline final def days(s:    Long): FiniteDuration = d.FiniteDuration(s, TimeUnits.Days)

  final type TimeUnit = java.util.concurrent.TimeUnit
}
