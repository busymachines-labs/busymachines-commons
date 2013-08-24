package com.kentivo.mdm.domain

import com.busymachines.commons.domain.Id
import java.util.Locale
import org.joda.time.DateTime
import com.busymachines.commons.domain.Unit

case class ItemRule(
  id: Id[ItemRule])

case class PropertyRule(
  id: Id[ItemRule],
  rangeCheck: Id[RangeCheck])

case class RangeCheck(
  min: Option[Double],
  max: Option[Double],
  unit: Option[Unit])

case class Schedule(
  time: DateTime,
  repeat: Option[ScheduleRepeat.Value] = None)

object ScheduleRepeat extends Enumeration {
  val Daily = Value("daily")
  val Weekly = Value("weekly")
  val Monthly = Value("monthly")
  val Yearly = Value("yearly")
}