package com.busymachines.commons

import grizzled.slf4j.Logger

object ProfilingUtils {

  def time[R](message: String, stdout : Boolean = false)(block: => R)(implicit logger : Logger): R = {
    if (logger.isDebugEnabled || stdout) {
      val t0 = System.nanoTime()
      val result = block // call-by-name
      val t1 = System.nanoTime()
      val seconds = (t1 - t0) / 1000000;
      if (stdout) {
        println(s"${message} (${seconds / 1000.0}s)")
      }
      logger.debug(s"${message} (${seconds / 1000.0}s)")
      result
    } else {
      block
    }
  }
}