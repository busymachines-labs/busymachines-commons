package com.busymachines.commons.dao

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.annotation.tailrec
import scala.util.Try
import scala.util.Failure
import scala.util.Success

object Versioned {
  implicit def toEntity[T](v: Versioned[T]) = v.entity
}

case class Versioned[T](entity: T, version: Long)

object RetryVersionConflictAsync {
  def apply[A](n : Int)(f : => Future[A])(implicit ec : ExecutionContext) : Future[A] = { 
    f.recoverWith {
      case t : VersionConflictException if n > 1 =>
        apply(n - 1)(f)
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