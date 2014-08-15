package com.busymachines.commons.logger.domain

import com.busymachines.commons.elasticsearch.ESTypes

/**
 * Created by alex on 15.08.2014.
 */
object LoggerESTypes extends ESTypes {
  val LogMessage = esType("log", LogMessageESMappings)
}
