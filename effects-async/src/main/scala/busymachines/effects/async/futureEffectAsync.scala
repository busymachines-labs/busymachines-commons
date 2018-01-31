package busymachines.effects.async

import busymachines.core._
import busymachines.effects.sync._
import busymachines.duration, duration.FiniteDuration

import scala.collection.generic.CanBuildFrom
import scala.util.control.NonFatal
import scala.{concurrent => sc}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 29 Jan 2018
  *
  */
trait FutureTypeDefinitions {
  type Future[T] = sc.Future[T]
  val Future: sc.Future.type = sc.Future

  type ExecutionContext = sc.ExecutionContext
  val ExecutionContext: sc.ExecutionContext.type = sc.ExecutionContext

  val Await: sc.Await.type = sc.Await

  def blocking[T](body: => T): T = sc.blocking(body)

}

object FutureSyntax {

  /**
    *
    */
  trait Implicits {
    implicit def bmcFutureCompanionObjectOps(obj: Future.type): CompanionObjectOps =
      new CompanionObjectOps(obj)

    implicit def bmcFutureReferenceOps[T](value: Future[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit def bmcFutureNestedOptionOps[T](nopt: Future[Option[T]]): NestedOptionOps[T] =
      new NestedOptionOps(nopt)

    implicit def bmcFutureNestedResultOps[T](result: Future[Result[T]]): NestedResultOps[T] =
      new NestedResultOps(result)

    implicit def bmcFutureBooleanOps(test: Boolean): BooleanOps =
      new BooleanOps(test)

    implicit def bmcFutureNestedBooleanOps(test: Future[Boolean]): NestedBooleanOps =
      new NestedBooleanOps(test)
  }

  /**
    *
    */
  final class CompanionObjectOps(val obj: Future.type) {

    def pure[T](value: T): Future[T] =
      FutureOps.pure(value)

    def fail[T](bad: Anomaly): Future[T] =
      FutureOps.fail(bad)

    def failWeak[T](bad: Throwable): Future[T] =
      FutureOps.failWeak(bad)

    // —— def unit: Future[Unit] —— already defined on Future object

    def fromOption[T](opt: Option[T], ifNone: => Anomaly): Future[T] =
      FutureOps.fromOption(opt, ifNone)

    def suspendOption[T](opt:    => Option[T], ifNone: => Anomaly)(
      implicit executionContext: ExecutionContext
    ): Future[T] =
      FutureOps.suspendOption(opt, ifNone)

    def fromOptionWeak[T](opt: Option[T], ifNone: => Throwable): Future[T] =
      FutureOps.fromOptionWeak(opt, ifNone)

    def suspendOptionWeak[T](opt: => Option[T], ifNone: => Throwable)(
      implicit executionContext:  ExecutionContext
    ): Future[T] = FutureOps.suspendOptionWeak(opt, ifNone)

    // —— def fromTry —— already defined on Future object

    def suspendTry[T](tr: => Try[T])(implicit executionContext: ExecutionContext): Future[T] =
      FutureOps.suspendTry(tr)

    def fromEither[L, R](either: Either[L, R], transformLeft: L => Anomaly): Future[R] =
      FutureOps.fromEither(either, transformLeft)

    def suspendEither[L, R](either: => Either[L, R], transformLeft: L => Anomaly)(
      implicit ec: ExecutionContext
    ): Future[R] = FutureOps.suspendEither(either, transformLeft)

    def fromEitherWeak[L, R](either: Either[L, R])(implicit ev: L <:< Throwable): Future[R] =
      FutureOps.fromEitherWeak(either)(ev)

    def suspendEitherWeak[L, R](either: => Either[L, R])(
      implicit
      ev: L <:< Throwable,
      ec: ExecutionContext
    ): Future[R] = FutureOps.suspendEitherWeak(either)(ev, ec)

    def fromEitherWeak[L, R](either: Either[L, R], transformLeft: L => Throwable): Future[R] =
      FutureOps.fromEitherWeak(either, transformLeft)

    def suspendEitherWeak[L, R](either: => Either[L, R], transformLeft: L => Throwable)(
      implicit ec: ExecutionContext
    ): Future[R] = FutureOps.suspendEitherWeak(either, transformLeft)

    def fromResult[T](result: Result[T]): Future[T] =
      FutureOps.fromResult(result)

    def suspendResult[T](result: => Result[T])(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendResult(result)

    def cond[T](test: Boolean, good: => T, bad: => Anomaly): Future[T] =
      FutureOps.cond(test, good, bad)

    def condWeak[T](test: Boolean, good: => T, bad: => Throwable): Future[T] =
      FutureOps.condWeak(test, good, bad)

    def condWith[T](test: Boolean, good: => Future[T], bad: => Anomaly): Future[T] =
      FutureOps.condWith(test, good, bad)

    def condWithWeak[T](test: Boolean, good: => Future[T], bad: => Throwable): Future[T] =
      FutureOps.condWithWeak(test, good, bad)

    def flatCond[T](test: Future[Boolean], good: => T, bad: => Anomaly)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.flatCond(test, good, bad)

    def flatCondWeak[T](test: Future[Boolean], good: => T, bad: => Throwable)(
      implicit ec:            ExecutionContext
    ): Future[T] =
      FutureOps.flatCondWeak(test, good, bad)

    def flatCondWith[T](test: Future[Boolean], good: => Future[T], bad: => Anomaly)(
      implicit ec:            ExecutionContext
    ): Future[T] =
      FutureOps.flatCondWith(test, good, bad)

    def flatCondWithWeak[T](
      test: Future[Boolean],
      good: => Future[T],
      bad:  => Throwable
    )(
      implicit ec: ExecutionContext
    ): Future[T] =
      FutureOps.flatCondWithWeak(test, good, bad)

    def failOnTrue(test: Boolean, bad: => Anomaly): Future[Unit] =
      FutureOps.failOnTrue(test, bad)

    def failOnTrueWeak(test: Boolean, bad: => Throwable): Future[Unit] =
      FutureOps.failOnTrueWeak(test, bad)

    def failOnFalse(test: Boolean, bad: => Anomaly): Future[Unit] =
      FutureOps.failOnFalse(test, bad)

    def failOnFalseWeak(test: Boolean, bad: => Throwable): Future[Unit] =
      FutureOps.failOnFalseWeak(test, bad)

    def flatFailOnTrue(test: Future[Boolean], bad: => Anomaly)(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatFailOnTrue(test, bad)

    def flatFailOnTrueWeak(test: Future[Boolean], bad: => Throwable)(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatFailOnTrueWeak(test, bad)

    def flatFailOnFalse(test: Future[Boolean], bad: => Anomaly)(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatFailOnFalse(test, bad)

    def flatFailOnFalseWeak(test: Future[Boolean], bad: => Throwable)(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatFailOnFalseWeak(test, bad)

    def flattenOption[T](nopt: Future[Option[T]], ifNone: => Anomaly)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.flattenOption(nopt, ifNone)

    def flattenOptionWeak[T](nopt: Future[Option[T]], ifNone: => Throwable)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.flattenOptionWeak(nopt, ifNone)

    def flattenResult[T](value: Future[Result[T]])(implicit ec: ExecutionContext): Future[T] =
      FutureOps.flattenResult(value)

    def attemptResult[T](value: Future[T])(implicit ec: ExecutionContext): Future[Result[T]] =
      FutureOps.attemptResult(value)

    def asIO[T](value: Future[T])(implicit ec: ExecutionContext): IO[T] =
      FutureOps.asIO(value)

    def asTask[T](value: Future[T]): Task[T] =
      FutureOps.asTask(value)

    def suspendInIO[T](value: => Future[T])(implicit ec: ExecutionContext): IO[T] =
      FutureOps.suspendInIO(value)

    def suspendInTask[T](value: => Future[T]): Task[T] =
      FutureOps.suspendInTask(value)

    def unsafeSyncGet[T](value: Future[T], atMost: FiniteDuration = FutureOps.defaultDuration): T =
      FutureOps.unsafeSyncGet(value, atMost)

    //=========================================================================
    //================= Run side-effects in varrying scenarios ================
    //=========================================================================

    def effectOnTrue[_](test: Boolean, effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.effectOnTrue(test, effect)

    def flatEffectOnTrue[_](test: Future[Boolean], effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatEffectOnTrue(test, effect)

    def effectOnFalse[_](test: Boolean, effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.effectOnFalse(test, effect)

    def flatEffectOnFalse[_](test: Future[Boolean], effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatEffectOnFalse(test, effect)

    def effectOnEmpty[T, _](value: Option[T], effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.effectOnEmpty(value, effect)

    def flatEffectOnEmpty[T, _](value: Future[Option[T]], effect: => Future[_])(
      implicit ec: ExecutionContext
    ): Future[Unit] =
      FutureOps.flatEffectOnEmpty(value, effect)

    def effectOnSome[T, _](value: Option[T], effect: T => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.effectOnSome(value, effect)

    def flatEffectOnSome[T, _](value: Future[Option[T]], effect: T => Future[_])(
      implicit ec: ExecutionContext
    ): Future[Unit] =
      FutureOps.flatEffectOnSome(value, effect)

    def effectOnIncorrect[T, _](value: Result[T], effect: Anomaly => Future[_])(
      implicit ec: ExecutionContext
    ): Future[Unit] =
      FutureOps.effectOnIncorrect(value, effect)

    def flatEffectOnIncorrect[T, _](value: Future[Result[T]], effect: Anomaly => Future[_])(
      implicit ec: ExecutionContext
    ): Future[Unit] =
      FutureOps.flatEffectOnIncorrect(value, effect)

    def flatEffectOnCorrect[T, _](value: Future[Result[T]], effect: T => Future[_])(
      implicit ec: ExecutionContext
    ): Future[Unit] =
      FutureOps.flatEffectOnCorrect(value, effect)

    def effectOnCorrect[T, _](value: Result[T], effect: T => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.effectOnCorrect(value, effect)

    //=========================================================================
    //============================== Transformers =============================
    //=========================================================================

    def bimap[T, R](value: Future[T], good: T => R, bad: Throwable => Anomaly)(
      implicit ec: ExecutionContext
    ): Future[R] =
      FutureOps.bimap(value, good, bad)

    def bimap[T, R](value: Future[T], result: Result[T] => Result[R])(implicit ec: ExecutionContext): Future[R] =
      FutureOps.bimap(value, result)

    def bimapWeak[T, R](value: Future[T], good: T => R, bad: Throwable => Throwable)(
      implicit ec: ExecutionContext
    ): Future[R] =
      FutureOps.bimapWeak(value, good, bad)

    def morph[T, R](value: Future[T], good: T => R, bad: Throwable => R)(implicit ec: ExecutionContext): Future[R] =
      FutureOps.morph(value, good, bad)

    def morph[T, R](value: Future[T], result: Result[T] => R)(implicit ec: ExecutionContext): Future[R] =
      FutureOps.morph(value, result)

    def discardContent[_](value: Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.discardContent(value)

    //=========================================================================
    //=============================== Traversals ==============================
    //=========================================================================

    def serialize[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => Future[B])(
      implicit
      cbf: CanBuildFrom[C[A], B, C[B]],
      ec:  ExecutionContext
    ): Future[C[B]] = FutureOps.serialize(col)(fn)
  }

  /**
    *
    */
  final class ReferenceOps[T](val value: Future[T]) extends AnyVal {

    def attempResult(implicit ec: ExecutionContext): Future[Result[T]] =
      FutureOps.attemptResult(value)

    def asIO(implicit ec: ExecutionContext): IO[T] =
      FutureOps.asIO(value)

    def asTask: Task[T] =
      FutureOps.asTask(value)

    def unsafeSyncGet(atMost: FiniteDuration = FutureOps.defaultDuration): T =
      FutureOps.unsafeSyncGet(value, atMost)

    def bimap[R](good: T => R, bad: Throwable => Anomaly)(implicit ec: ExecutionContext): Future[R] =
      FutureOps.bimap(value, good, bad)

    def bimap[R](result: Result[T] => Result[R])(implicit ec: ExecutionContext): Future[R] =
      FutureOps.bimap(value, result)

    def bimapWeak[R](good: T => R, bad: Throwable => Throwable)(implicit ec: ExecutionContext): Future[R] =
      FutureOps.bimapWeak(value, good, bad)

    def morph[R](good: T => R, bad: Throwable => R)(implicit ec: ExecutionContext): Future[R] =
      FutureOps.morph(value, good, bad)

    def morph[R](result: Result[T] => R)(implicit ec: ExecutionContext): Future[R] =
      FutureOps.morph(value, result)

    def discardContent(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.discardContent(value)
  }

  final class SafeReferenceOps[T](value: => Future[T]) {

    def suspendInIO(implicit ec: ExecutionContext): IO[T] =
      FutureOps.suspendInIO(value)

    def suspendInTask: Task[T] =
      FutureOps.suspendInTask(value)

  }

  /**
    *
    *
    */
  final class NestedOptionOps[T](private[this] val nopt: Future[Option[T]]) {

    def flattenOption(ifNone: => Anomaly)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.flattenOption(nopt, ifNone)

    def flattenOptionWeak(ifNone: => Throwable)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.flattenOptionWeak(nopt, ifNone)

    def effectOnEmpty[_](effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatEffectOnEmpty(nopt, effect)

    def effectOnSome[_](effect: T => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatEffectOnSome(nopt, effect)

  }

  /**
    *
    */
  final class NestedResultOps[T](private[this] val result: Future[Result[T]]) {

    def flattenResult(implicit ec: ExecutionContext): Future[T] =
      FutureOps.flattenResult(result)

    def effectOnIncorrect[_](effect: Anomaly => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatEffectOnIncorrect(result, effect)

    def effectOnCorrect[_](effect: T => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatEffectOnCorrect(result, effect)
  }

  /**
    *
    *
    */
  final class BooleanOps(private[this] val test: Boolean) {

    def condFuture[T](good: => T, bad: => Anomaly): Future[T] =
      FutureOps.cond(test, good, bad)

    def condFutureWeak[T](good: => T, bad: => Throwable): Future[T] =
      FutureOps.condWeak(test, good, bad)

    def condWithFuture[T](good: => Future[T], bad: => Anomaly): Future[T] =
      FutureOps.condWith(test, good, bad)

    def condWithFutureWeak[T](good: => Future[T], bad: => Throwable): Future[T] =
      FutureOps.condWithWeak(test, good, bad)

    def failOnTrueFuture(bad: => Anomaly): Future[Unit] =
      FutureOps.failOnTrue(test, bad)

    def failOnTrueFutureWeak(bad: => Throwable): Future[Unit] =
      FutureOps.failOnTrueWeak(test, bad)

    def failOnFalseFuture(bad: => Anomaly): Future[Unit] =
      FutureOps.failOnFalse(test, bad)

    def failOnFalseFutureWeak(bad: => Throwable): Future[Unit] =
      FutureOps.failOnFalseWeak(test, bad)

    def effectOnFalseFuture[_](effect: => Future[_])(implicit ec: ExecutionContext): Future[_] =
      FutureOps.effectOnFalse(test, effect)

    def effectOnTrueFuture[_](effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.effectOnTrue(test, effect)

  }

  /**
    *
    *
    */
  final class NestedBooleanOps(private[this] val test: Future[Boolean]) {

    def cond[T](good: => T, bad: => Anomaly)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.flatCond(test, good, bad)

    def condWeak[T](good: => T, bad: => Throwable)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.flatCondWeak(test, good, bad)

    def condWith[T](good: => Future[T], bad: => Anomaly)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.flatCondWith(test, good, bad)

    def condWithWeak[T](good: => Future[T], bad: => Throwable)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.flatCondWithWeak(test, good, bad)

    def failOnTrue(bad: => Anomaly)(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatFailOnTrue(test, bad)

    def failOnTrueWeak(bad: => Throwable)(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatFailOnTrueWeak(test, bad)

    def failOnFalse(bad: => Anomaly)(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatFailOnFalse(test, bad)

    def failOnFalseWeak(bad: => Throwable)(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatFailOnFalseWeak(test, bad)

    def effectOnFalse[_](effect: => Future[_])(implicit ec: ExecutionContext): Future[_] =
      FutureOps.flatEffectOnFalse(test, effect)

    def effectOnTrue[_](effect: => Future[_])(implicit ec: ExecutionContext): Future[_] =
      FutureOps.flatEffectOnTrue(test, effect)

  }
}

/**
  *
  */
object FutureOps {

  def pure[T](value: T): Future[T] =
    Future.successful(value)

  def fail[T](bad: Anomaly): Future[T] =
    Future.failed(bad.asThrowable)

  def failWeak[T](bad: Throwable): Future[T] =
    Future.failed(bad)

  // —— def unit: Future[Unit] —— already defined on Future object

  def fromOption[T](opt: Option[T], ifNone: => Anomaly): Future[T] = opt match {
    case None        => FutureOps.fail(ifNone)
    case Some(value) => FutureOps.pure(value)
  }

  def suspendOption[T](opt: => Option[T], ifNone: => Anomaly)(implicit executionContext: ExecutionContext): Future[T] =
    Future(opt).flatMap(o => FutureOps.fromOption(o, ifNone))

  def fromOptionWeak[T](opt: Option[T], ifNone: => Throwable): Future[T] = opt match {
    case None        => FutureOps.failWeak(ifNone)
    case Some(value) => FutureOps.pure(value)
  }

  def suspendOptionWeak[T](opt: => Option[T], ifNone: => Throwable)(
    implicit executionContext:  ExecutionContext
  ): Future[T] =
    Future(opt).flatMap(o => FutureOps.fromOptionWeak(o, ifNone))

  // —— def fromTry[T](tr: Try[T]): Future[T] —— already exists on Future

  def suspendTry[T](tr: => Try[T])(implicit executionContext: ExecutionContext): Future[T] =
    Future(tr).flatMap(Future.fromTry)

  def fromEither[L, R](either: Either[L, R], transformLeft: L => Anomaly): Future[R] = either match {
    case Left(value)  => FutureOps.fail(transformLeft(value))
    case Right(value) => FutureOps.pure(value)
  }

  def suspendEither[L, R](either: => Either[L, R], transformLeft: L => Anomaly)(
    implicit ec: ExecutionContext
  ): Future[R] =
    Future(either).flatMap(eit => FutureOps.fromEither(eit, transformLeft))

  def fromEitherWeak[L, R](either: Either[L, R])(implicit ev: L <:< Throwable): Future[R] = either match {
    case Left(value)  => FutureOps.failWeak(ev(value))
    case Right(value) => FutureOps.pure(value)
  }

  def suspendEitherWeak[L, R](either: => Either[L, R])(implicit ev: L <:< Throwable, ec: ExecutionContext): Future[R] =
    Future(either).flatMap(eit => FutureOps.fromEitherWeak(eit)(ev))

  def fromEitherWeak[L, R](either: Either[L, R], transformLeft: L => Throwable): Future[R] = either match {
    case Left(value)  => FutureOps.failWeak(transformLeft(value))
    case Right(value) => FutureOps.pure(value)
  }

  def suspendEitherWeak[L, R](either: => Either[L, R], transformLeft: L => Throwable)(
    implicit ec: ExecutionContext
  ): Future[R] = Future(either).flatMap(eit => FutureOps.fromEitherWeak(eit, transformLeft))

  def fromResult[T](result: Result[T]): Future[T] = result match {
    case Left(value)  => FutureOps.fail(value)
    case Right(value) => FutureOps.pure(value)
  }

  def suspendResult[T](result: => Result[T])(implicit ec: ExecutionContext): Future[T] =
    Future(result).flatMap(FutureOps.fromResult)

  def cond[T](test: Boolean, good: => T, bad: => Anomaly): Future[T] =
    if (test) FutureOps.pure(good) else FutureOps.fail(bad)

  def condWeak[T](test: Boolean, good: => T, bad: => Throwable): Future[T] =
    if (test) FutureOps.pure(good) else FutureOps.failWeak(bad)

  def condWith[T](test: Boolean, good: => Future[T], bad: => Anomaly): Future[T] =
    if (test) good else FutureOps.fail(bad)

  def condWithWeak[T](test: Boolean, good: => Future[T], bad: => Throwable): Future[T] =
    if (test) good else FutureOps.failWeak(bad)

  def flatCond[T](test: Future[Boolean], good: => T, bad: => Anomaly)(implicit ec: ExecutionContext): Future[T] =
    test.flatMap(t => FutureOps.cond(t, good, bad))

  def flatCondWeak[T](test: Future[Boolean], good: => T, bad: => Throwable)(implicit ec: ExecutionContext): Future[T] =
    test.flatMap(t => FutureOps.condWeak(t, good, bad))

  def flatCondWith[T](test: Future[Boolean], good: => Future[T], bad: => Anomaly)(
    implicit ec:            ExecutionContext
  ): Future[T] =
    test.flatMap(t => FutureOps.condWith(t, good, bad))

  def flatCondWithWeak[T](test: Future[Boolean], good: => Future[T], bad: => Throwable)(
    implicit ec:                ExecutionContext
  ): Future[T] =
    test.flatMap(t => FutureOps.condWithWeak(t, good, bad))

  def failOnTrue(test: Boolean, bad: => Anomaly): Future[Unit] =
    if (test) FutureOps.fail(bad) else Future.unit

  def failOnTrueWeak(test: Boolean, bad: => Throwable): Future[Unit] =
    if (test) FutureOps.failWeak(bad) else Future.unit

  def failOnFalse(test: Boolean, bad: => Anomaly): Future[Unit] =
    if (!test) FutureOps.fail(bad) else Future.unit

  def failOnFalseWeak(test: Boolean, bad: => Throwable): Future[Unit] =
    if (!test) FutureOps.failWeak(bad) else Future.unit

  def flatFailOnTrue(test: Future[Boolean], bad: => Anomaly)(implicit ec: ExecutionContext): Future[Unit] =
    test.flatMap(t => FutureOps.failOnTrue(t, bad))

  def flatFailOnTrueWeak(test: Future[Boolean], bad: => Throwable)(implicit ec: ExecutionContext): Future[Unit] =
    test.flatMap(t => FutureOps.failOnTrueWeak(t, bad))

  def flatFailOnFalse(test: Future[Boolean], bad: => Anomaly)(implicit ec: ExecutionContext): Future[Unit] =
    test.flatMap(t => FutureOps.failOnFalse(t, bad))

  def flatFailOnFalseWeak(test: Future[Boolean], bad: => Throwable)(implicit ec: ExecutionContext): Future[Unit] =
    test.flatMap(t => FutureOps.failOnFalseWeak(t, bad))

  def flattenOption[T](nopt: Future[Option[T]], ifNone: => Anomaly)(implicit ec: ExecutionContext): Future[T] =
    nopt.flatMap {
      case None    => FutureOps.fail(ifNone)
      case Some(v) => FutureOps.pure(v)
    }

  def flattenOptionWeak[T](nopt: Future[Option[T]], ifNone: => Throwable)(implicit ec: ExecutionContext): Future[T] =
    nopt.flatMap {
      case None    => FutureOps.failWeak(ifNone)
      case Some(v) => FutureOps.pure(v)
    }

  def flattenResult[T](value: Future[Result[T]])(implicit ec: ExecutionContext): Future[T] = value.flatMap {
    case Left(a)  => FutureOps.fail(a)
    case Right(a) => FutureOps.pure(a)
  }

  def attemptResult[T](value: Future[T])(implicit ec: ExecutionContext): Future[Result[T]] =
    value.map(Result.pure).recover {
      case NonFatal(t) => Result.failWeak(t)
    }

  def asIO[T](value: Future[T])(implicit ec: ExecutionContext): IO[T] =
    IOOps.fromFuture(value)

  def asTask[T](value: Future[T]): Task[T] =
    TaskOps.fromFuture(value)

  def suspendInIO[T](value: => Future[T])(implicit ec: ExecutionContext): IO[T] =
    IOOps.suspendFuture(value)

  def suspendInTask[T](value: => Future[T]): Task[T] =
    TaskOps.suspendFuture(value)

  def unsafeSyncGet[T](value: Future[T], atMost: FiniteDuration = FutureOps.defaultDuration): T =
    Await.result(value, atMost)

  //=========================================================================
  //================= Run side-effects in varrying scenarios ================
  //=========================================================================

  def effectOnTrue[_](test: Boolean, effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
    if (test) FutureOps.discardContent(effect) else Future.unit

  def flatEffectOnTrue[_](test: Future[Boolean], effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
    test.flatMap(t => FutureOps.effectOnTrue(t, effect))

  def effectOnFalse[_](test: Boolean, effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
    if (!test) FutureOps.discardContent(effect) else Future.unit

  def flatEffectOnFalse[_](test: Future[Boolean], effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
    test.flatMap(t => FutureOps.effectOnFalse(t, effect))

  def effectOnEmpty[T, _](value: Option[T], effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
    if (value.isEmpty) FutureOps.discardContent(effect) else Future.unit

  def flatEffectOnEmpty[T, _](value: Future[Option[T]], effect: => Future[_])(
    implicit ec: ExecutionContext
  ): Future[Unit] =
    value.flatMap(opt => FutureOps.effectOnEmpty(opt, effect))

  def effectOnSome[T, _](value: Option[T], effect: T => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
    value match {
      case None    => Future.unit
      case Some(v) => FutureOps.discardContent(effect(v))

    }

  def flatEffectOnSome[T, _](value: Future[Option[T]], effect: T => Future[_])(
    implicit ec: ExecutionContext
  ): Future[Unit] =
    value.flatMap(opt => FutureOps.effectOnSome(opt, effect))

  def effectOnIncorrect[T, _](value: Result[T], effect: Anomaly => Future[_])(
    implicit ec: ExecutionContext
  ): Future[Unit] = value match {
    case Correct(_)         => Future.unit
    case Incorrect(anomaly) => FutureOps.discardContent(effect(anomaly))
  }

  def flatEffectOnIncorrect[T, _](value: Future[Result[T]], effect: Anomaly => Future[_])(
    implicit ec: ExecutionContext
  ): Future[Unit] =
    value.flatMap(result => FutureOps.effectOnIncorrect(result, effect))

  def effectOnCorrect[T, _](value: Result[T], effect: T => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
    value match {
      case Incorrect(_) => Future.unit
      case Correct(v)   => FutureOps.discardContent(effect(v))
    }

  def flatEffectOnCorrect[T, _](value: Future[Result[T]], effect: T => Future[_])(
    implicit ec: ExecutionContext
  ): Future[Unit] =
    value.flatMap(result => FutureOps.effectOnCorrect(result, effect))

  //=========================================================================
  //============================== Transformers =============================
  //=========================================================================

  def bimap[T, R](value: Future[T], good: T => R, bad: Throwable => Anomaly)(implicit ec: ExecutionContext): Future[R] =
    value.transform(tr => tr.bimap(good, bad))

  def bimap[T, R](value: Future[T], result: Result[T] => Result[R])(implicit ec: ExecutionContext): Future[R] =
    FutureOps.attemptResult(value).map(result).flatMap {
      case Correct(v)   => FutureOps.pure(v)
      case Incorrect(v) => FutureOps.fail(v)
    }

  def bimapWeak[T, R](value: Future[T], good: T => R, bad: Throwable => Throwable)(
    implicit ec: ExecutionContext
  ): Future[R] =
    value.transform(tr => tr.bimapWeak(good, bad))

  def morph[T, R](value: Future[T], good: T => R, bad: Throwable => R)(implicit ec: ExecutionContext): Future[R] =
    value.map(good).recover {
      case NonFatal(t) => bad(t)
    }

  def morph[T, R](value: Future[T], result: Result[T] => R)(implicit ec: ExecutionContext): Future[R] =
    FutureOps.attemptResult(value).map(result)

  private val UnitFunction: Any => Unit = _ => ()

  def discardContent[_](value: Future[_])(implicit ec: ExecutionContext): Future[Unit] =
    value.map(UnitFunction)

  //=========================================================================
  //=============================== Traversals ==============================
  //=========================================================================

  /**
    *
    * Syntactically inspired from [[Future.traverse]], but it differs semantically
    * insofar as this method does not attempt to run any futures in parallel. "M" stands
    * for "monadic", as opposed to "applicative" which is the foundation for the formal definition
    * of "traverse" (even though in Scala it is by accident-ish)
    *
    * For the vast majority of cases you should prefer this method over [[Future.sequence]]
    * and [[Future.traverse]], since even small collections can easily wind up queuing so many
    * [[Future]]s that you blow your execution context.
    *
    * Usage:
    * {{{
    *   import busymachines.effects.async._
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
    import scala.collection.mutable
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

  //=========================================================================
  //=============================== Constants ===============================
  //=========================================================================

  private[async] val defaultDuration: FiniteDuration = duration.minutes(1)
}
