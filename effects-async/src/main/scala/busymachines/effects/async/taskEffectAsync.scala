package busymachines.effects.async

import busymachines.core._
import busymachines.duration, duration.FiniteDuration
import busymachines.effects.sync._

import scala.collection.generic.CanBuildFrom
import scala.util.control.NonFatal

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jan 2018
  *
  */
trait TaskTypeDefinitions {
  import monix.{execution => mex}
  import monix.{eval      => mev}

  type CancellableFuture[T] = mex.CancelableFuture[T]

  /**
    * N.B.
    * that Scheduler is also a [[scala.concurrent.ExecutionContext]],
    * which makes this type the only implicit in context necessary to do
    * interop between [[Task]] and [[scala.concurrent.Future]]
    */
  type Scheduler = mex.Scheduler
  type Task[T]   = mev.Task[T]

  val Scheduler: mex.Scheduler.type = mex.Scheduler
  val Task:      mev.Task.type      = mev.Task

}

object TaskSyntax {

  /**
    *
    */
  trait Implicits {
    implicit def bmcTaskCompanionObjectOps(obj: Task.type): CompanionObjectOps =
      new CompanionObjectOps(obj)

    implicit def bmcTaskReferenceOps[T](value: Task[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit def bmcTaskNestedOptionOps[T](nopt: Task[Option[T]]): NestedOptionOps[T] =
      new NestedOptionOps(nopt)

    implicit def bmcTaskNestedResultOps[T](result: Task[Result[T]]): NestedResultOps[T] =
      new NestedResultOps(result)

    implicit def bmcTaskBooleanOps(test: Boolean): BooleanOps =
      new BooleanOps(test)

    implicit def bmcTaskNestedBooleanOps(test: Task[Boolean]): NestedBooleanOps =
      new NestedBooleanOps(test)
  }

  /**
    *
    */
  final class CompanionObjectOps(val obj: Task.type) {

    // —— def pure[T](value: T): Task[T] —— already defined on companion object

    def fail[T](bad: Anomaly): Task[T] =
      TaskOps.fail(bad)

    def failThr[T](bad: Throwable): Task[T] =
      TaskOps.failThr(bad)

    // —— def unit: Task[Unit] —— already defined on Task object

    def fromOption[T](opt: Option[T], ifNone: => Anomaly): Task[T] =
      TaskOps.fromOption(opt, ifNone)

    def suspendOption[T](opt: => Option[T], ifNone: => Anomaly): Task[T] =
      TaskOps.suspendOption(opt, ifNone)

    def fromOptionThr[T](opt: Option[T], ifNone: => Throwable): Task[T] =
      TaskOps.fromOptionThr(opt, ifNone)

    def suspendOptionThr[T](opt: => Option[T], ifNone: => Throwable): Task[T] =
      TaskOps.suspendOptionThr(opt, ifNone)

    // def fromTry[T](tr: Try[T]): Task[T] —— already defined on Task object

    def suspendTry[T](tr: => Try[T]): Task[T] =
      TaskOps.suspendTry(tr)

    def fromEither[L, R](either: Either[L, R], transformLeft: L => Anomaly): Task[R] =
      TaskOps.fromEither(either, transformLeft)

    def suspendEither[L, R](either: => Either[L, R], transformLeft: L => Anomaly): Task[R] =
      TaskOps.suspendEither(either, transformLeft)

    def fromEitherThr[L, R](either: Either[L, R])(implicit ev: L <:< Throwable): Task[R] =
      TaskOps.fromEitherThr(either)(ev)

    def suspendEitherThr[L, R](either: => Either[L, R])(implicit ev: L <:< Throwable): Task[R] =
      TaskOps.suspendEitherThr(either)(ev)

    def fromEitherThr[L, R](either: Either[L, R], transformLeft: L => Throwable): Task[R] =
      TaskOps.fromEitherThr(either, transformLeft)

    def suspendEitherThr[L, R](either: => Either[L, R], transformLeft: L => Throwable): Task[R] =
      TaskOps.suspendEitherThr(either, transformLeft)

    def fromResult[T](result: Result[T]): Task[T] =
      TaskOps.fromResult(result)

    def suspendResult[T](result: => Result[T]): Task[T] =
      TaskOps.suspendResult(result)

    def fromFuturePure[T](value: Future[T]): Task[T] =
      Task.fromFuture(value)

    def suspendFuture[T](result: => Future[T]): Task[T] =
      TaskOps.suspendFuture(result)

    def cond[T](test: Boolean, good: => T, bad: => Anomaly): Task[T] =
      TaskOps.cond(test, good, bad)

    def condThr[T](test: Boolean, good: => T, bad: => Throwable): Task[T] =
      TaskOps.condThr(test, good, bad)

    def condWith[T](test: Boolean, good: => Task[T], bad: => Anomaly): Task[T] =
      TaskOps.condWith(test, good, bad)

    def condWithThr[T](test: Boolean, good: => Task[T], bad: => Throwable): Task[T] =
      TaskOps.condWithThr(test, good, bad)

    def flatCond[T](test: Task[Boolean], good: => T, bad: => Anomaly): Task[T] =
      TaskOps.flatCond(test, good, bad)

    def flatCondThr[T](test: Task[Boolean], good: => T, bad: => Throwable): Task[T] =
      TaskOps.flatCondThr(test, good, bad)

    def flatCondWith[T](test: Task[Boolean], good: => Task[T], bad: => Anomaly): Task[T] =
      TaskOps.flatCondWith(test, good, bad)

    def flatCondWithThr[T](test: Task[Boolean], good: => Task[T], bad: => Throwable): Task[T] =
      TaskOps.flatCondWithThr(test, good, bad)

    def failOnTrue(test: Boolean, bad: => Anomaly): Task[Unit] =
      TaskOps.failOnTrue(test, bad)

    def failOnTrueThr(test: Boolean, bad: => Throwable): Task[Unit] =
      TaskOps.failOnTrueThr(test, bad)

    def failOnFalse(test: Boolean, bad: => Anomaly): Task[Unit] =
      TaskOps.failOnFalse(test, bad)

    def failOnFalseThr(test: Boolean, bad: => Throwable): Task[Unit] =
      TaskOps.failOnFalseThr(test, bad)

    def flatFailOnTrue(test: Task[Boolean], bad: => Anomaly): Task[Unit] =
      TaskOps.flatFailOnTrue(test, bad)

    def flatFailOnTrueThr(test: Task[Boolean], bad: => Throwable): Task[Unit] =
      TaskOps.flatFailOnTrueThr(test, bad)

    def flatFailOnFalse(test: Task[Boolean], bad: => Anomaly): Task[Unit] =
      TaskOps.flatFailOnFalse(test, bad)

    def flatFailOnFalseThr(test: Task[Boolean], bad: => Throwable): Task[Unit] =
      TaskOps.flatFailOnFalseThr(test, bad)

    def flattenOption[T](nopt: Task[Option[T]], ifNone: => Anomaly): Task[T] =
      TaskOps.flattenOption(nopt, ifNone)

    def flattenOptionThr[T](nopt: Task[Option[T]], ifNone: => Throwable): Task[T] =
      TaskOps.flattenOptionThr(nopt, ifNone)

    def flattenResult[T](value: Task[Result[T]]): Task[T] =
      TaskOps.flattenResult(value)

    def attemptResult[T](value: Task[T]): Task[Result[T]] =
      TaskOps.attemptResult(value)

    def asFutureUnsafe[T](value: Task[T])(implicit sc: Scheduler): Future[T] =
      TaskOps.asFutureUnsafe(value)

    def asIO[T](value: Task[T])(implicit sc: Scheduler): IO[T] =
      TaskOps.asIO(value)

    def unsafeSyncGet[T](value: Task[T], atMost: FiniteDuration = TaskOps.defaultDuration)(implicit sc: Scheduler): T =
      TaskOps.unsafeSyncGet(value, atMost)

    //=========================================================================
    //================= Run side-effects in varrying scenarios ================
    //=========================================================================

    def effectOnTrue[_](test: Boolean, effect: => Task[_]): Task[Unit] =
      TaskOps.effectOnTrue(test, effect)

    def flatEffectOnTrue[_](test: Task[Boolean], effect: => Task[_]): Task[Unit] =
      TaskOps.flatEffectOnTrue(test, effect)

    def effectOnFalse[_](test: Boolean, effect: => Task[_]): Task[Unit] =
      TaskOps.effectOnFalse(test, effect)

    def flatEffectOnFalse[_](test: Task[Boolean], effect: => Task[_]): Task[Unit] =
      TaskOps.flatEffectOnFalse(test, effect)

    def effectOnEmpty[T, _](value: Option[T], effect: => Task[_]): Task[Unit] =
      TaskOps.effectOnEmpty(value, effect)

    def flatEffectOnEmpty[T, _](value: Task[Option[T]], effect: => Task[_]): Task[Unit] =
      TaskOps.flatEffectOnEmpty(value, effect)

    def effectOnSome[T, _](value: Option[T], effect: T => Task[_]): Task[Unit] =
      TaskOps.effectOnSome(value, effect)

    def flatEffectOnSome[T, _](value: Task[Option[T]], effect: T => Task[_]): Task[Unit] =
      TaskOps.flatEffectOnSome(value, effect)

    def effectOnIncorrect[T, _](value: Result[T], effect: Anomaly => Task[_]): Task[Unit] =
      TaskOps.effectOnIncorrect(value, effect)

    def flatEffectOnIncorrect[T, _](value: Task[Result[T]], effect: Anomaly => Task[_]): Task[Unit] =
      TaskOps.flatEffectOnIncorrect(value, effect)

    def flatEffectOnCorrect[T, _](value: Task[Result[T]], effect: T => Task[_]): Task[Unit] =
      TaskOps.flatEffectOnCorrect(value, effect)

    def effectOnCorrect[T, _](value: Result[T], effect: T => Task[_]): Task[Unit] =
      TaskOps.effectOnCorrect(value, effect)

    //=========================================================================
    //============================== Transformers =============================
    //=========================================================================

    def bimap[T, R](value: Task[T], good: T => R, bad: Throwable => Anomaly): Task[R] =
      TaskOps.bimap(value, good, bad)

    def bimap[T, R](value: Task[T], result: Result[T] => Result[R]): Task[R] =
      TaskOps.bimap(value, result)

    def bimapThr[T, R](value: Task[T], good: T => R, bad: Throwable => Throwable): Task[R] =
      TaskOps.bimapThr(value, good, bad)

    def morph[T, R](value: Task[T], good: T => R, bad: Throwable => R): Task[R] =
      TaskOps.morph(value, good, bad)

    def morph[T, R](value: Task[T], result: Result[T] => R): Task[R] =
      TaskOps.morph(value, result)

    def discardContent[_](value: Task[_]): Task[Unit] =
      TaskOps.discardContent(value)

    //=========================================================================
    //=============================== Traversals ==============================
    //=========================================================================

    def serialize[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => Task[B])(
      implicit
      cbf: CanBuildFrom[C[A], B, C[B]]
    ): Task[C[B]] = TaskOps.serialize(col)(fn)
  }

  /**
    *
    */
  final class ReferenceOps[T](val value: Task[T]) extends AnyVal {

    def attempResult: Task[Result[T]] =
      TaskOps.attemptResult(value)

    def asFutureUnsafe()(implicit sc: Scheduler): Future[T] =
      TaskOps.asFutureUnsafe(value)

    def asIO(implicit sc: Scheduler): IO[T] =
      TaskOps.asIO(value)

    def unsafeSyncGet(atMost: FiniteDuration = TaskOps.defaultDuration)(implicit sc: Scheduler): T =
      TaskOps.unsafeSyncGet(value, atMost)

    def bimap[R](good: T => R, bad: Throwable => Anomaly): Task[R] =
      TaskOps.bimap(value, good, bad)

    def bimap[R](result: Result[T] => Result[R]): Task[R] =
      TaskOps.bimap(value, result)

    def bimapThr[R](good: T => R, bad: Throwable => Throwable): Task[R] =
      TaskOps.bimapThr(value, good, bad)

    def morph[R](good: T => R, bad: Throwable => R): Task[R] =
      TaskOps.morph(value, good, bad)

    def morph[R](result: Result[T] => R): Task[R] =
      TaskOps.morph(value, result)

    def discardContent: Task[Unit] =
      TaskOps.discardContent(value)
  }

  /**
    *
    *
    */
  final class NestedOptionOps[T](private[this] val nopt: Task[Option[T]]) {

    def flattenOption(ifNone: => Anomaly): Task[T] =
      TaskOps.flattenOption(nopt, ifNone)

    def flattenOptionThr(ifNone: => Throwable): Task[T] =
      TaskOps.flattenOptionThr(nopt, ifNone)

    def effectOnEmpty[_](effect: => Task[_]): Task[Unit] =
      TaskOps.flatEffectOnEmpty(nopt, effect)

    def effectOnSome[_](effect: T => Task[_]): Task[Unit] =
      TaskOps.flatEffectOnSome(nopt, effect)

  }

  /**
    *
    */
  final class NestedResultOps[T](private[this] val result: Task[Result[T]]) {

    def flattenResult: Task[T] =
      TaskOps.flattenResult(result)

    def effectOnIncorrect[_](effect: Anomaly => Task[_]): Task[Unit] =
      TaskOps.flatEffectOnIncorrect(result, effect)

    def effectOnCorrect[_](effect: T => Task[_]): Task[Unit] =
      TaskOps.flatEffectOnCorrect(result, effect)
  }

  /**
    *
    *
    */
  final class BooleanOps(private[this] val test: Boolean) {

    def condTask[T](good: => T, bad: => Anomaly): Task[T] =
      TaskOps.cond(test, good, bad)

    def condTaskThr[T](good: => T, bad: => Throwable): Task[T] =
      TaskOps.condThr(test, good, bad)

    def condWithTask[T](good: => Task[T], bad: => Anomaly): Task[T] =
      TaskOps.condWith(test, good, bad)

    def condWithTaskThr[T](good: => Task[T], bad: => Throwable): Task[T] =
      TaskOps.condWithThr(test, good, bad)

    def failOnTrueTask(bad: => Anomaly): Task[Unit] =
      TaskOps.failOnTrue(test, bad)

    def failOnTrueTaskThr(bad: => Throwable): Task[Unit] =
      TaskOps.failOnTrueThr(test, bad)

    def failOnFalseTask(bad: => Anomaly): Task[Unit] =
      TaskOps.failOnFalse(test, bad)

    def failOnFalseTaskThr(bad: => Throwable): Task[Unit] =
      TaskOps.failOnFalseThr(test, bad)

    def effectOnFalseTask[_](effect: => Task[_]): Task[_] =
      TaskOps.effectOnFalse(test, effect)

    def effectOnTrueTask[_](effect: => Task[_]): Task[Unit] =
      TaskOps.effectOnTrue(test, effect)

  }

  /**
    *
    *
    */
  final class NestedBooleanOps(private[this] val test: Task[Boolean]) {

    def cond[T](good: => T, bad: => Anomaly): Task[T] =
      TaskOps.flatCond(test, good, bad)

    def condThr[T](good: => T, bad: => Throwable): Task[T] =
      TaskOps.flatCondThr(test, good, bad)

    def condWith[T](good: => Task[T], bad: => Anomaly): Task[T] =
      TaskOps.flatCondWith(test, good, bad)

    def condWithThr[T](good: => Task[T], bad: => Throwable): Task[T] =
      TaskOps.flatCondWithThr(test, good, bad)

    def failOnTrue(bad: => Anomaly): Task[Unit] =
      TaskOps.flatFailOnTrue(test, bad)

    def failOnTrueThr(bad: => Throwable): Task[Unit] =
      TaskOps.flatFailOnTrueThr(test, bad)

    def failOnFalse(bad: => Anomaly): Task[Unit] =
      TaskOps.flatFailOnFalse(test, bad)

    def failOnFalseThr(bad: => Throwable): Task[Unit] =
      TaskOps.flatFailOnFalseThr(test, bad)

    def effectOnFalse[_](effect: => Task[_]): Task[_] =
      TaskOps.flatEffectOnFalse(test, effect)

    def effectOnTrue[_](effect: => Task[_]): Task[_] =
      TaskOps.flatEffectOnTrue(test, effect)

  }
}

/**
  *
  */
object TaskOps {
  import cats.syntax.applicativeError._
  import cats.syntax.monadError._

  def pure[T](value: T): Task[T] =
    Task.pure(value)

  def fail[T](bad: Anomaly): Task[T] =
    Task.raiseError(bad.asThrowable)

  def failThr[T](bad: Throwable): Task[T] =
    Task.raiseError(bad)

  // —— def unit: Task[Unit] —— already defined on Task object

  def fromOption[T](opt: Option[T], ifNone: => Anomaly): Task[T] = opt match {
    case None        => TaskOps.fail(ifNone)
    case Some(value) => TaskOps.pure(value)
  }

  def suspendOption[T](opt: => Option[T], ifNone: => Anomaly): Task[T] =
    Task.suspend(TaskOps.fromOption(opt, ifNone))

  def fromOptionThr[T](opt: Option[T], ifNone: => Throwable): Task[T] = opt match {
    case None        => TaskOps.failThr(ifNone)
    case Some(value) => TaskOps.pure(value)
  }

  def suspendOptionThr[T](opt: => Option[T], ifNone: => Throwable): Task[T] =
    Task.suspend(TaskOps.fromOptionThr(opt, ifNone))

  // def fromTry[T](tr: Try[T]): Task[T] —— already defined on Task object

  def suspendTry[T](tr: => Try[T]): Task[T] =
    Task.suspend(Task.fromTry(tr))

  def fromEither[L, R](either: Either[L, R], transformLeft: L => Anomaly): Task[R] = either match {
    case Left(value)  => TaskOps.fail(transformLeft(value))
    case Right(value) => TaskOps.pure(value)
  }

  def suspendEither[L, R](either: => Either[L, R], transformLeft: L => Anomaly): Task[R] =
    Task.suspend(TaskOps.fromEither(either, transformLeft))

  def fromEitherThr[L, R](either: Either[L, R])(implicit ev: L <:< Throwable): Task[R] = either match {
    case Left(value)  => TaskOps.failThr(ev(value))
    case Right(value) => TaskOps.pure(value)
  }

  def suspendEitherThr[L, R](either: => Either[L, R])(implicit ev: L <:< Throwable): Task[R] =
    Task.suspend(TaskOps.fromEitherThr(either)(ev))

  def fromEitherThr[L, R](either: Either[L, R], transformLeft: L => Throwable): Task[R] = either match {
    case Left(value)  => TaskOps.failThr(transformLeft(value))
    case Right(value) => TaskOps.pure(value)
  }

  def suspendEitherThr[L, R](either: => Either[L, R], transformLeft: L => Throwable): Task[R] =
    Task.suspend(TaskOps.fromEitherThr(either, transformLeft))

  def fromResult[T](result: Result[T]): Task[T] = result match {
    case Left(value)  => TaskOps.fail(value)
    case Right(value) => TaskOps.pure(value)
  }

  def suspendResult[T](result: => Result[T]): Task[T] =
    Task.suspend(TaskOps.fromResult(result))

  def fromFuturePure[T](value: Future[T]): Task[T] =
    Task.fromFuture(value)

  def suspendFuture[T](value: => Future[T]): Task[T] =
    Task.deferFuture(value)

  def fromIO[T](value: IO[T]): Task[T] =
    Task.fromIO(value)

  def cond[T](test: Boolean, good: => T, bad: => Anomaly): Task[T] =
    if (test) TaskOps.pure(good) else TaskOps.fail(bad)

  def condThr[T](test: Boolean, good: => T, bad: => Throwable): Task[T] =
    if (test) TaskOps.pure(good) else TaskOps.failThr(bad)

  def condWith[T](test: Boolean, good: => Task[T], bad: => Anomaly): Task[T] =
    if (test) good else TaskOps.fail(bad)

  def condWithThr[T](test: Boolean, good: => Task[T], bad: => Throwable): Task[T] =
    if (test) good else TaskOps.failThr(bad)

  def flatCond[T](test: Task[Boolean], good: => T, bad: => Anomaly): Task[T] =
    test.flatMap(t => TaskOps.cond(t, good, bad))

  def flatCondThr[T](test: Task[Boolean], good: => T, bad: => Throwable): Task[T] =
    test.flatMap(t => TaskOps.condThr(t, good, bad))

  def flatCondWith[T](test: Task[Boolean], good: => Task[T], bad: => Anomaly): Task[T] =
    test.flatMap(t => TaskOps.condWith(t, good, bad))

  def flatCondWithThr[T](test: Task[Boolean], good: => Task[T], bad: => Throwable): Task[T] =
    test.flatMap(t => TaskOps.condWithThr(t, good, bad))

  def failOnTrue(test: Boolean, bad: => Anomaly): Task[Unit] =
    if (test) TaskOps.fail(bad) else Task.unit

  def failOnTrueThr(test: Boolean, bad: => Throwable): Task[Unit] =
    if (test) TaskOps.failThr(bad) else Task.unit

  def failOnFalse(test: Boolean, bad: => Anomaly): Task[Unit] =
    if (!test) TaskOps.fail(bad) else Task.unit

  def failOnFalseThr(test: Boolean, bad: => Throwable): Task[Unit] =
    if (!test) TaskOps.failThr(bad) else Task.unit

  def flatFailOnTrue(test: Task[Boolean], bad: => Anomaly): Task[Unit] =
    test.flatMap(t => TaskOps.failOnTrue(t, bad))

  def flatFailOnTrueThr(test: Task[Boolean], bad: => Throwable): Task[Unit] =
    test.flatMap(t => TaskOps.failOnTrueThr(t, bad))

  def flatFailOnFalse(test: Task[Boolean], bad: => Anomaly): Task[Unit] =
    test.flatMap(t => TaskOps.failOnFalse(t, bad))

  def flatFailOnFalseThr(test: Task[Boolean], bad: => Throwable): Task[Unit] =
    test.flatMap(t => TaskOps.failOnFalseThr(t, bad))

  def flattenOption[T](nopt: Task[Option[T]], ifNone: => Anomaly): Task[T] =
    nopt.flatMap {
      case None    => TaskOps.fail(ifNone)
      case Some(v) => TaskOps.pure(v)
    }

  def flattenOptionThr[T](nopt: Task[Option[T]], ifNone: => Throwable): Task[T] =
    nopt.flatMap {
      case None    => TaskOps.failThr(ifNone)
      case Some(v) => TaskOps.pure(v)
    }

  def flattenResult[T](value: Task[Result[T]]): Task[T] = value.flatMap {
    case Left(a)  => TaskOps.fail(a)
    case Right(a) => TaskOps.pure(a)
  }

  def attemptResult[T](value: Task[T]): Task[Result[T]] =
    value.attempt.map((e: Either[Throwable, T]) => Result.fromEitherThr(e))

  def asFutureUnsafe[T](value: Task[T])(implicit sc: Scheduler): CancellableFuture[T] =
    value.runAsync

  def asIO[T](value: Task[T])(implicit sc: Scheduler): IO[T] =
    value.toIO

  def unsafeSyncGet[T](
    value:  Task[T],
    atMost: FiniteDuration = defaultDuration
  )(
    implicit sc: Scheduler
  ): T = value.runAsync.unsafeSyncGet(atMost)

  //=========================================================================
  //================= Run side-effects in varrying scenarios ================
  //=========================================================================

  def effectOnTrue[_](test: Boolean, effect: => Task[_]): Task[Unit] =
    if (test) TaskOps.discardContent(effect) else Task.unit

  def flatEffectOnTrue[_](test: Task[Boolean], effect: => Task[_]): Task[Unit] =
    test.flatMap(t => TaskOps.effectOnTrue(t, effect))

  def effectOnFalse[_](test: Boolean, effect: => Task[_]): Task[Unit] =
    if (!test) TaskOps.discardContent(effect) else Task.unit

  def flatEffectOnFalse[_](test: Task[Boolean], effect: => Task[_]): Task[Unit] =
    test.flatMap(t => TaskOps.effectOnFalse(t, effect))

  def effectOnEmpty[T, _](value: Option[T], effect: => Task[_]): Task[Unit] =
    if (value.isEmpty) TaskOps.discardContent(effect) else Task.unit

  def flatEffectOnEmpty[T, _](value: Task[Option[T]], effect: => Task[_]): Task[Unit] =
    value.flatMap(opt => TaskOps.effectOnEmpty(opt, effect))

  def effectOnSome[T, _](value: Option[T], effect: T => Task[_]): Task[Unit] =
    value match {
      case None    => Task.unit
      case Some(v) => TaskOps.discardContent(effect(v))

    }

  def flatEffectOnSome[T, _](value: Task[Option[T]], effect: T => Task[_]): Task[Unit] =
    value.flatMap(opt => TaskOps.effectOnSome(opt, effect))

  def effectOnIncorrect[T, _](value: Result[T], effect: Anomaly => Task[_]): Task[Unit] = value match {
    case Correct(_)         => Task.unit
    case Incorrect(anomaly) => TaskOps.discardContent(effect(anomaly))
  }

  def flatEffectOnIncorrect[T, _](value: Task[Result[T]], effect: Anomaly => Task[_]): Task[Unit] =
    value.flatMap(result => TaskOps.effectOnIncorrect(result, effect))

  def effectOnCorrect[T, _](value: Result[T], effect: T => Task[_]): Task[Unit] =
    value match {
      case Incorrect(_) => Task.unit
      case Correct(v)   => TaskOps.discardContent(effect(v))
    }

  def flatEffectOnCorrect[T, _](value: Task[Result[T]], effect: T => Task[_]): Task[Unit] =
    value.flatMap(result => TaskOps.effectOnCorrect(result, effect))

  //=========================================================================
  //============================== Transformers =============================
  //=========================================================================

  def bimap[T, R](value: Task[T], good: T => R, bad: Throwable => Anomaly): Task[R] =
    value.map(good).adaptError {
      case NonFatal(t) => bad(t).asThrowable
    }

  def bimap[T, R](value: Task[T], result: Result[T] => Result[R]): Task[R] =
    TaskOps.attemptResult(value).map(result).flatMap {
      case Correct(v)   => TaskOps.pure(v)
      case Incorrect(v) => TaskOps.fail(v)
    }

  def bimapThr[T, R](value: Task[T], good: T => R, bad: Throwable => Throwable): Task[R] =
    value.map(good).adaptError {
      case NonFatal(t) => bad(t)
    }

  def morph[T, R](value: Task[T], good: T => R, bad: Throwable => R): Task[R] =
    value.map(good).recover {
      case NonFatal(t) => bad(t)
    }

  def morph[T, R](value: Task[T], result: Result[T] => R): Task[R] =
    TaskOps.attemptResult(value).map(result)

  private val UnitFunction: Any => Unit = _ => ()

  def discardContent[_](value: Task[_]): Task[Unit] =
    value.map(UnitFunction)

  //=========================================================================
  //=============================== Traversals ==============================
  //=========================================================================
  /**
    *
    * Syntactically inspired from [[Future.traverse]].
    *
    * See [[FutureOps.serialize]] for semantics.
    *
    * Usage:
    * {{{
    *   import busymachines.effects.async._
    *   val patches: Seq[Patch] = //...
    *
    *   //this ensures that no two changes will be applied in parallel.
    *   val allPatches: Task[Seq[Patch]] = Task.serialize(patches){ patch: Patch =>
    *     Task {
    *       //apply patch
    *     }
    *   }
    *   //... and so on, and so on!
    * }}}
    *
    *
    */
  def serialize[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => Task[B])(
    implicit
    cbf: CanBuildFrom[C[A], B, C[B]]
  ): Task[C[B]] = Task.traverse(col)(fn)(cbf)
  //=========================================================================
  //=============================== Constants ===============================
  //=========================================================================

  private[async] val defaultDuration: FiniteDuration = duration.minutes(1)
}
