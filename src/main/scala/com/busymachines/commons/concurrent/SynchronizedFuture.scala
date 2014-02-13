package com.busymachines.commons.concurrent

import scala.concurrent.{ExecutionContext, Future, Lock}

class SynchronizedFuture {
  val lock = new Lock
  def apply[A](f : => Future[A])(implicit ec: ExecutionContext): Future[A] = {
    lock.acquire()
    f.andThen {
      case _ =>
        lock.release()
    }
  }
}