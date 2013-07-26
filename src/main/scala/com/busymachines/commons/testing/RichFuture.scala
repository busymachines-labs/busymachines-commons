package com.busymachines.commons.testing

import scala.language.postfixOps
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._

class RichFuture[A](future: Future[A]) {
  def await[A](duration:Duration) = Await.result(future, duration)
  def await[A] = Await.result(future, 1 minute)
} 
