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

  def bimap[R](good: T => R, bad: Anomaly => Anomaly): Result[R] = Result.bimap(r, good, bad)

  /**
    * Used to transform the underlying [[Result]] into a [[Correct]] one.
    * The functions should be pure, and not throw any exception.
    * @return
    *   A [[Correct]] [[Result]], where each branch is transformed as specified
    */
  def morph[R](good: T => R, bad: Anomaly => R): Result[R] = Result.morph(r, good, bad)

  def recover[R >: T](pf: PartialFunction[Anomaly, R]): Result[R] = Result.recover(r, pf)

  def recoverWith[R >: T](pf: PartialFunction[Anomaly, Result[R]]): Result[R] = Result.recoverWith(r, pf)

  //===========================================================================
  //===================== Result to various (pseudo)monads ====================
  //===========================================================================

  def unsafeAsOption: Option[T] = Result.unsafeAsOption(r)

  def unsafeAsList: List[T] = Result.unsafeAsList(r)

  def asTry: Try[T] = Result.asTry(r)

  def unsafeGet: T = Result.unsafeGet(r)

  /**
    * The "main" conversion is found in the ``future`` module,
    * but it is technically writeable in this module as well,
    * therefore we provide it, just in case it is needed.
    *
    * Unfortunately because [[Result]] is not an actual companion
    * object, we cannot use implicit resolution priorities to make
    * this conversion "lower priority" than the one in the ``future``
    * module.
    */
  def asFutureAlias: Future[T] = Result.asFuture(r)
}

