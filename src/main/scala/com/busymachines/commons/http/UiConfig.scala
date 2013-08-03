package com.busymachines.commons

object UiConfig extends HasConfiguration {
  val devmode = config.getBoolean("devmode")
}