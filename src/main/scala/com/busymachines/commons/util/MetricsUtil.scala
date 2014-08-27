package com.busymachines.commons.util

import com.netaporter.salad.metrics.spray.metrics.MetricsDirectiveFactory

object MetricsUtil {

  def apiCounter(mf: MetricsDirectiveFactory, label: String) =
    mf.counter(label).all.count & mf.timer(label).time

  def crudApiCounters(mf: MetricsDirectiveFactory, labelPrefix: String) =
    (apiCounter(mf, s"$labelPrefix.create"),
      apiCounter(mf, s"$labelPrefix.delete"),
      apiCounter(mf, s"$labelPrefix.update"),
      apiCounter(mf, s"$labelPrefix.retrieve"))

  def extendedCrudApiCounters(mf: MetricsDirectiveFactory, labelPrefix: String) =
    (apiCounter(mf, s"$labelPrefix.create"),
      apiCounter(mf, s"$labelPrefix.delete"),
      apiCounter(mf, s"$labelPrefix.update"),
      apiCounter(mf, s"$labelPrefix.retrieve"),
      apiCounter(mf, s"$labelPrefix.search"))

}
