package com.busymachines.commons.util

import com.busymachines.commons.logging.Logging

object ProfilingUtils extends Logging {

  def time[R](message: String, stdout: Boolean = false)(block: => R): R = {
    if (logger.isDebugEnabled || stdout) {
      val t0 = System.nanoTime()
      val result = block // call-by-name
      val t1 = System.nanoTime()
      val msec = (t1 - t0) / 1000000.0;
      val m = 
        if (msec >= 1000)
          s"${message} (${msec / 1000.0} sec)"
        else
          s"${message} ($msec msec)"
      if (stdout) {
        println(m)
      }
      logger.debug(m)
      result
    } else {
      block
    }
  }
}