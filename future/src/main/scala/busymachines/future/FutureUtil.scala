package busymachines.future

import busymachines.core.Anomaly
import busymachines.core.CatastrophicError
import busymachines.duration
import busymachines.duration.FiniteDuration
import busymachines.effects.result._

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable
import scala.util.control.NonFatal

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 20 Jan 2018
  *
  */
object FutureUtil {

  //===========================================================================
  //========================== Primary constructors ===========================
  //===========================================================================

  def pure[T](t: T): Future[T] = Future.successful(t)

  def fail[T](a: Anomaly): Future[T] = Future.failed(a.asThrowable)

  //===========================================================================
  //==================== Future from various (pseudo)monads ===================
  //===========================================================================

  def fromResult[R](r: Result[R]): Future[R] = Result.asFuture(r)

  def flattenResult[T](fres: Future[Result[T]])(implicit ec: ExecutionContext): Future[T] = fres flatMap {
    case Correct(c)   => FutureUtil.pure(c)
    case Incorrect(a) => FutureUtil.fail(a)
  }

  def fromEither[L, R](elr: Either[L, R])(implicit ev: L <:< Throwable): Future[R] = {
    elr match {
      case Left(left) =>
        ev(left) match {
          case a: Anomaly => FutureUtil.fail(a)
          case NonFatal(t) => FutureUtil.fail(CatastrophicError(t))
        }
      case Right(value) => FutureUtil.pure(value)
    }
  }

  def fromEither[L, R](elr: Either[L, R], transformLeft: L => Anomaly): Future[R] = {
    elr match {
      case Left(left)   => FutureUtil.fail(transformLeft(left))
      case Right(value) => FutureUtil.pure(value)
    }
  }

  def fromOption[T](opt: Option[T], ifNone: => Anomaly): Future[T] = {
    opt match {
      case None    => FutureUtil.fail(ifNone)
      case Some(v) => FutureUtil.pure(v)
    }
  }

  def fromOptionWeak[T](opt: Option[T], ifNone: => Throwable): Future[T] = {
    opt match {
      case None    => Future.failed(ifNone)
      case Some(v) => FutureUtil.pure(v)
    }
  }

  def flattenOption[T](fopt: Future[Option[T]], ifNone: => Anomaly)(implicit ec: ExecutionContext): Future[T] =
    fopt flatMap (opt => FutureUtil.fromOption(opt, ifNone))

  //===========================================================================
  //==================== Future from special cased Future =====================
  //===========================================================================

  def cond[T](test: Boolean, correct: => T, anomaly: => Anomaly): Future[T] =
    if (test) FutureUtil.pure(correct) else FutureUtil.fail(anomaly)

  def condWith[T](test: Boolean, correct: => Future[T], anomaly: => Anomaly): Future[T] =
    if (test) correct else FutureUtil.fail(anomaly)

  def failOnTrue(test: Boolean, anomaly: => Anomaly): Future[Unit] =
    if (test) FutureUtil.fail(anomaly) else Future.unit

  def failOnFalse(test: Boolean, anomaly: => Anomaly): Future[Unit] =
    if (!test) FutureUtil.fail(anomaly) else Future.unit

  def flatCond[T](test: Future[Boolean], correct: => T, anomaly: => Anomaly)(implicit ec: ExecutionContext): Future[T] =
    test flatMap (b => FutureUtil.cond(b, correct, anomaly))

  def flatCondWith[T](
    test:        Future[Boolean],
    correct:     => Future[T],
    anomaly:     => Anomaly
  )(implicit ec: ExecutionContext): Future[T] =
    test flatMap (b => FutureUtil.condWith(b, correct, anomaly))

  def flatFailOnTrue(test: Future[Boolean], anomaly: => Anomaly)(implicit ec: ExecutionContext): Future[Unit] =
    test flatMap (b => if (b) FutureUtil.fail(anomaly) else Future.unit)

  def flatFailOnFalse(test: Future[Boolean], anomaly: => Anomaly)(implicit ec: ExecutionContext): Future[Unit] =
    test flatMap (b => if (!b) FutureUtil.fail(anomaly) else Future.unit)

  def effectOnTrue[T](test: Boolean, eff: => Future[T])(implicit ec: ExecutionContext): Future[Unit] =
    if (test) FutureUtil.discardContent(eff) else Future.unit

  def effectOnFalse[T](test: Boolean, eff: => Future[T])(implicit ec: ExecutionContext): Future[Unit] =
    if (!test) FutureUtil.discardContent(eff) else Future.unit

  def flatEffectOnTrue[T](test: Future[Boolean], eff: => Future[T])(implicit ec: ExecutionContext): Future[Unit] =
    test flatMap (b => if (b) FutureUtil.discardContent(eff) else Future.unit)

  def flatEffectOnFalse[T](test: Future[Boolean], eff: => Future[T])(implicit ec: ExecutionContext): Future[Unit] =
    test flatMap (b => if (!b) FutureUtil.discardContent(eff) else Future.unit)

  private val UnitFunction: Any => Unit = _ => ()
  def discardContent[T](f: Future[T])(implicit ec: ExecutionContext): Future[Unit] = f.map(UnitFunction)

  //===========================================================================
  //===================== Future to various (pseudo)monads ====================
  //===========================================================================

  /**
    * Captures the failure of this Future in a [[Result]].
    * Preserves the underlying [[Anomaly]] type, if the failed Future is
    * because of a non-Anomaly throwable, then it will be wrapped in a [[CatastrophicError]]
    */
  def asResult[T](f: Future[T])(implicit ec: ExecutionContext): Future[Result[T]] = {
    FutureUtil.morph[T, Result[T]](
      f = f,
      bad = (t: Throwable) =>
        t match {
          case a: Anomaly => Result.fail(a)
          case NonFatal(e) => Result.fail(CatastrophicError(e))
      },
      good = (v: T) => Result.pure(v)
    )
  }

  /**
    * Using this is highly discouraged
    *
    * This is here more as a convenience method for testing
    */
  def syncUnsafeGet[T](f: Future[T], timeout: FiniteDuration = duration.minutes(1)): T =
    Await.result(f, timeout)

  /**
    * Using this is highly discouraged
    *
    * This is here more as a convenience method for testing
    */
  def syncAwaitReady[T](f: Future[T], timeout: FiniteDuration = duration.minutes(1)): Future[T] =
    Await.ready(f, timeout)

  /**
    * Using this is highly discouraged
    *
    * This is here more as a convenience method for testing
    */
  def syncAsResult[T](f: Future[T], timeout: FiniteDuration = duration.minutes(1)): Result[T] =
    Result(syncUnsafeGet(f, timeout))

  //===========================================================================
  //============================== Transformers ===============================
  //===========================================================================

  def bimap[T, R](f: Future[T], good: T => R, bad: Throwable => Anomaly)(implicit ec: ExecutionContext): Future[R] =
    f.transform {
      case scala.util.Success(g) => scala.util.Success(good(g))
      case scala.util.Failure(g) => scala.util.Failure(bad(g).asThrowable)
    }

  def bimapWeak[T, R](f: Future[T], good: T => R, bad: Throwable => Throwable)(
    implicit ec: ExecutionContext
  ): Future[R] = f.transform(good, bad)

  /**
    * Used to transform the underlying [[Future]] into a successful one.
    * The functions should be pure, and not throw any exception.
    * @return
    *   A Future that is successfull
    */
  def morph[T, R](f: Future[T], bad: Throwable => R, good: T => R)(implicit ec: ExecutionContext): Future[R] =
    f.transform {
      case scala.util.Success(v) => scala.util.Success(good(v))
      case scala.util.Failure(t) => scala.util.Success(bad(t))
    }

  def morph[T, R](f: Future[T], morphism: Result[T] => Result[R])(implicit ec: ExecutionContext): Future[R] =
    FutureUtil.flattenResult(FutureUtil.asResult(f).map(morphism))

  //===========================================================================
  //================================== Utils ==================================
  //===========================================================================

  /**
    *
    * Syntactically inspired from [[Future.traverse]], but it differs semantically
    * insofar as this method does not attempt to run any futures in parallel.
    *
    * For the vast majority of cases you should prefer this method over [[Future.sequence]]
    * and [[Future.traverse]], since even small collections can easily wind up queuing so many
    * [[Future]]s that you blow your execution context.
    *
    * Usage:
    * {{{
    *   import busymachines.future._
    *   val patches: Seq[Patch] = //...
    *
    *   //this ensures that no two changes will be applied in parallel.
    *   val allPatches: Future[Seq[Patch]] = Future.serialize(patches){ patch: Patch =>
    *     Future {
    *       //apply patch
    *     }
    *   }
    *   //... and so on, and so on!
    * }}}
    *
    *
    */
  def serialize[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => Future[B])(
    implicit
    cbf: CanBuildFrom[C[A], B, C[B]],
    ec:  ExecutionContext
  ): Future[C[B]] = {
    if (col.isEmpty) {
      Future.successful(cbf.apply().result())
    }
    else {
      val seq  = col.toSeq
      val head = seq.head
      val tail = seq.tail
      val builder: mutable.Builder[B, C[B]] = cbf.apply()
      val firstBuilder = fn(head) map { z =>
        builder.+=(z)
      }
      val eventualBuilder: Future[mutable.Builder[B, C[B]]] = tail.foldLeft(firstBuilder) {
        (serializedBuilder: Future[mutable.Builder[B, C[B]]], element: A) =>
          serializedBuilder flatMap [mutable.Builder[B, C[B]]] { (result: mutable.Builder[B, C[B]]) =>
            val f: Future[mutable.Builder[B, C[B]]] = fn(element) map { newElement =>
              result.+=(newElement)
            }
            f
          }
      }
      eventualBuilder map { b =>
        b.result()
      }
    }
  }

}
