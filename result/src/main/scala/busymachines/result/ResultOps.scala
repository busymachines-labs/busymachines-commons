package busymachines.result

import busymachines.core.Anomaly

import scala.concurrent.Future
import scala.util.Try

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 09 Jan 2018
  *
  */
final class ResultOps[T](private[this] val r: Result[T]) {

  def bimap[R](bad: Anomaly => Anomaly, good: T => R): Result[R] = {
    r.right.map(good).left.map(bad)
  }

  def morph[R](bad: Anomaly => R, good: T => R): R = r match {
    case Left(value)  => bad(value)
    case Right(value) => good(value)
  }

  def recoverWith[R >: T](pf: PartialFunction[Anomaly, Result[R]]): Result[R] = r match {
    case Left(a: Anomaly) if pf.isDefinedAt(a) => pf(a)
    case _ => r
  }

  def recover[R >: T](pf: PartialFunction[Anomaly, R]): Result[R] = r match {
    case Left(a: Anomaly) if pf.isDefinedAt(a) => Result.pure(pf(a))
    case _ => r
  }

  //===========================================================================
  //================== Result to standard scala (pseudo)monads ================
  //===========================================================================

  def unsafeAsOption: Option[T] = r match {
    case Left(value)  => throw value.asThrowable
    case Right(value) => Option(value)
  }

  def asTry: Try[T] = r match {
    case Left(value)  => scala.util.Failure(value.asThrowable)
    case Right(value) => scala.util.Success(value)
  }

  def asFuture: Future[T] = r match {
    case Left(value)  => Future.failed(value.asThrowable)
    case Right(value) => Future.successful(value)
  }

  def unsafeGet: T = r match {
    case Left(value)  => throw value.asThrowable
    case Right(value) => value
  }
}
