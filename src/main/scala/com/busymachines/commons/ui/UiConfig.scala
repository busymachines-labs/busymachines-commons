package com.busymachines.commons.ui
import com.busymachines.commons.HasConfiguration

object UiConfig extends HasConfiguration {
  val devmode = config.getBoolean("devmode")
}