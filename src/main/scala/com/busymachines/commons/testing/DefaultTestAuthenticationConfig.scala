package com.busymachines.commons.testing

import com.busymachines.prefab.authentication.logic.AuthenticationConfig
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit

object DefaultTestAuthenticationConfig extends AuthenticationConfig("") {
  override def expiration = FiniteDuration(7, TimeUnit.DAYS)
  override def idleTime = FiniteDuration(2, TimeUnit.HOURS)
  override def maxCapacity = 100000
}