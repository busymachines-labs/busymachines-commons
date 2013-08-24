package com.busymachines.prefab.authentication.logic

import com.busymachines.commons.CommonConfig

class AuthenticatorCacheConfig(baseName : String) extends CommonConfig(baseName) {
  val expiration = duration("expiration")
  val idleTime = duration("idleTime")
  val maxCapacity = int("maxCapacity")
}
