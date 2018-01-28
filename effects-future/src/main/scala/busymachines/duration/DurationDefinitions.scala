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
  val FiniteDuration: d.FiniteDuration.type = d.FiniteDuration

  val Nanos:   TimeUnit = TimeUnit.NANOSECONDS
  val Micros:  TimeUnit = TimeUnit.MICROSECONDS
  val Millis:  TimeUnit = TimeUnit.MILLISECONDS
  val Seconds: TimeUnit = TimeUnit.SECONDS
  val Minutes: TimeUnit = TimeUnit.MINUTES
  val Hours:   TimeUnit = TimeUnit.HOURS
  val Days:    TimeUnit = TimeUnit.DAYS

  def nanos(s:   Long): FiniteDuration = d.FiniteDuration(s, Nanos)
  def micros(s:  Long): FiniteDuration = d.FiniteDuration(s, Micros)
  def millis(s:  Long): FiniteDuration = d.FiniteDuration(s, Millis)
  def seconds(s: Long): FiniteDuration = d.FiniteDuration(s, Seconds)
  def minutes(s: Long): FiniteDuration = d.FiniteDuration(s, Minutes)
  def hours(s:   Long): FiniteDuration = d.FiniteDuration(s, Hours)
  def days(s:    Long): FiniteDuration = d.FiniteDuration(s, Days)
}
