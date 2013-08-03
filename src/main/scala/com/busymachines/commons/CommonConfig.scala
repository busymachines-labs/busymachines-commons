package com.busymachines.commons

object CommonConfig extends HasConfiguration {
  val devmode = config.getBoolean("devmode")
}