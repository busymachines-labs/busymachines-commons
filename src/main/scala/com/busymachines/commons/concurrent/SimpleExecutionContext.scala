package com.busymachines.commons.concurrent

import scala.concurrent.ExecutionContext

object SimpleExecutionContext extends ExecutionContext {

  def execute(runnable: Runnable) {
    runnable.run
  }
  
  def reportFailure(t: Throwable) {
    throw t
  }
}