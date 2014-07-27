package com.busymachines.commons.util

import scala.concurrent.ExecutionContext

object SimpleExecutionContext extends ExecutionContext {

  def execute(runnable: Runnable) {
    runnable.run()
  }
  
  def reportFailure(t: Throwable) {
    throw t
  }
}