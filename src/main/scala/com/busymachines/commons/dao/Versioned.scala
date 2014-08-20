package com.busymachines.commons.dao

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.annotation.tailrec
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import com.busymachines.commons.logging.Logging

object Versioned {
  implicit def toEntity[T](v: Versioned[T]) = v.entity
}

case class Versioned[T](entity: T, version: Long)

object RetryVersionConflictAsync extends Logging {
  def apply[A](maxRetries : Int = 3, n : Int = 1)(f : => Future[A])(implicit ec : ExecutionContext) : Future[A] = {
    f.recoverWith {
      case t : VersionConflictException if n <= maxRetries =>
        logger.debug(s"Version conflict attempt $n: ${t.getMessage}")
        apply(maxRetries, n + 1)(f)
    }
  }
}

object RetryVersionConflict {
  @tailrec
  def apply[A](n : Int)(f : => A) : A = { 
    Try (f) match {
      case Success(a) => a
      case Failure(e : VersionConflictException) if n > 1 => apply (n - 1)(f)
      case Failure(e) => throw e
    }
  }
}