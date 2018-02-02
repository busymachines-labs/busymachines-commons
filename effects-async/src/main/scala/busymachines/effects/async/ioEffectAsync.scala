package busymachines.effects.async

import busymachines.core._
import busymachines.effects.sync._

import scala.collection.generic.CanBuildFrom
import scala.util.control.NonFatal

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jan 2018
  *
  */
trait IOTypeDefinitions {
  import cats.{effect => ce}

  type IO[T] = ce.IO[T]
  val IO: ce.IO.type = ce.IO
}

object IOSyntax {

  /**
    *
    */
  trait Implicits {
    implicit def bmcIOCompanionObjectOps(obj: IO.type): CompanionObjectOps =
      new CompanionObjectOps(obj)

    implicit def bmcIOReferenceOps[T](value: IO[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit def bmcIONestedOptionOps[T](nopt: IO[Option[T]]): NestedOptionOps[T] =
      new NestedOptionOps(nopt)

    implicit def bmcIONestedResultOps[T](result: IO[Result[T]]): NestedResultOps[T] =
      new NestedResultOps(result)

    implicit def bmcIOBooleanOps(test: Boolean): BooleanOps =
      new BooleanOps(test)

    implicit def bmcIONestedBooleanOps(test: IO[Boolean]): NestedBooleanOps =
      new NestedBooleanOps(test)
  }

  /**
    *
    */
  final class CompanionObjectOps(val obj: IO.type) {

    // —— def pure[T](value: T): IO[T] —— already defined on companion object

    def fail[T](bad: Anomaly): IO[T] =
      IOOps.fail(bad)

    def failThr[T](bad: Throwable): IO[T] =
      IOOps.failThr(bad)

    // —— def unit: IO[Unit] —— already defined on IO object

    def fromOption[T](opt: Option[T], ifNone: => Anomaly): IO[T] =
      IOOps.fromOption(opt, ifNone)

    def suspendOption[T](opt: => Option[T], ifNone: => Anomaly): IO[T] =
      IOOps.suspendOption(opt, ifNone)

    def fromOptionThr[T](opt: Option[T], ifNone: => Throwable): IO[T] =
      IOOps.fromOptionThr(opt, ifNone)

    def suspendOptionThr[T](opt: => Option[T], ifNone: => Throwable): IO[T] =
      IOOps.suspendOptionThr(opt, ifNone)

    def fromTry[T](tr: Try[T]): IO[T] =
      IOOps.fromTry(tr)

    def suspendTry[T](tr: => Try[T]): IO[T] =
      IOOps.suspendTry(tr)

    def fromEitherAnomaly[L, R](either: Either[L, R], transformLeft: L => Anomaly): IO[R] =
      IOOps.fromEither(either, transformLeft)

    def suspendEither[L, R](either: => Either[L, R], transformLeft: L => Anomaly): IO[R] =
      IOOps.suspendEither(either, transformLeft)

    def fromEitherThr[L, R](either: Either[L, R])(implicit ev: L <:< Throwable): IO[R] =
      IOOps.fromEitherThr(either)(ev)

    def suspendEitherThr[L, R](either: => Either[L, R])(implicit ev: L <:< Throwable): IO[R] =
      IOOps.suspendEitherThr(either)(ev)

    def fromEitherThr[L, R](either: Either[L, R], transformLeft: L => Throwable): IO[R] =
      IOOps.fromEitherThr(either, transformLeft)

    def suspendEitherThr[L, R](either: => Either[L, R], transformLeft: L => Throwable): IO[R] =
      IOOps.suspendEitherThr(either, transformLeft)

    def fromResult[T](result: Result[T]): IO[T] =
      IOOps.fromResult(result)

    def suspendResult[T](result: => Result[T]): IO[T] =
      IOOps.suspendResult(result)

    def fromFuturePure[T](future: Future[T])(implicit ec: ExecutionContext): IO[T] =
      IOOps.fromFuturePure(future)

    def suspendFuture[T](result: => Future[T])(implicit ec: ExecutionContext): IO[T] =
      IOOps.suspendFuture(result)

    def fromTask[T](task: Task[T])(implicit sc: Scheduler): IO[T] =
      IOOps.fromTask(task)

    def cond[T](test: Boolean, good: => T, bad: => Anomaly): IO[T] =
      IOOps.cond(test, good, bad)

    def condThr[T](test: Boolean, good: => T, bad: => Throwable): IO[T] =
      IOOps.condThr(test, good, bad)

    def condWith[T](test: Boolean, good: => IO[T], bad: => Anomaly): IO[T] =
      IOOps.condWith(test, good, bad)

    def condWithThr[T](test: Boolean, good: => IO[T], bad: => Throwable): IO[T] =
      IOOps.condWithThr(test, good, bad)

    def flatCond[T](test: IO[Boolean], good: => T, bad: => Anomaly): IO[T] =
      IOOps.flatCond(test, good, bad)

    def flatCondThr[T](test: IO[Boolean], good: => T, bad: => Throwable): IO[T] =
      IOOps.flatCondThr(test, good, bad)

    def flatCondWith[T](test: IO[Boolean], good: => IO[T], bad: => Anomaly): IO[T] =
      IOOps.flatCondWith(test, good, bad)

    def flatCondWithThr[T](test: IO[Boolean], good: => IO[T], bad: => Throwable): IO[T] =
      IOOps.flatCondWithThr(test, good, bad)

    def failOnTrue(test: Boolean, bad: => Anomaly): IO[Unit] =
      IOOps.failOnTrue(test, bad)

    def failOnTrueThr(test: Boolean, bad: => Throwable): IO[Unit] =
      IOOps.failOnTrueThr(test, bad)

    def failOnFalse(test: Boolean, bad: => Anomaly): IO[Unit] =
      IOOps.failOnFalse(test, bad)

    def failOnFalseThr(test: Boolean, bad: => Throwable): IO[Unit] =
      IOOps.failOnFalseThr(test, bad)

    def flatFailOnTrue(test: IO[Boolean], bad: => Anomaly): IO[Unit] =
      IOOps.flatFailOnTrue(test, bad)

    def flatFailOnTrueThr(test: IO[Boolean], bad: => Throwable): IO[Unit] =
      IOOps.flatFailOnTrueThr(test, bad)

    def flatFailOnFalse(test: IO[Boolean], bad: => Anomaly): IO[Unit] =
      IOOps.flatFailOnFalse(test, bad)

    def flatFailOnFalseThr(test: IO[Boolean], bad: => Throwable): IO[Unit] =
      IOOps.flatFailOnFalseThr(test, bad)

    def unpackOption[T](nopt: IO[Option[T]], ifNone: => Anomaly): IO[T] =
      IOOps.unpackOption(nopt, ifNone)

    def unpackOptionThr[T](nopt: IO[Option[T]], ifNone: => Throwable): IO[T] =
      IOOps.unpackOptionThr(nopt, ifNone)

    def unpackResult[T](value: IO[Result[T]]): IO[T] =
      IOOps.unpackResult(value)

    def attemptResult[T](value: IO[T]): IO[Result[T]] =
      IOOps.attemptResult(value)

    def asFutureUnsafe[T](value: IO[T]): Future[T] =
      IOOps.asFutureUnsafe(value)

    def asTask[T](value: IO[T]): Task[T] =
      IOOps.asTask(value)

    def unsafeSyncGet[T](value: IO[T]): T =
      IOOps.unsafeSyncGet(value)

    //=========================================================================
    //================= Run side-effects in varrying scenarios ================
    //=========================================================================

    def effectOnTrue[_](test: Boolean, effect: => IO[_]): IO[Unit] =
      IOOps.effectOnTrue(test, effect)

    def flatEffectOnTrue[_](test: IO[Boolean], effect: => IO[_]): IO[Unit] =
      IOOps.flatEffectOnTrue(test, effect)

    def effectOnFalse[_](test: Boolean, effect: => IO[_]): IO[Unit] =
      IOOps.effectOnFalse(test, effect)

    def flatEffectOnFalse[_](test: IO[Boolean], effect: => IO[_]): IO[Unit] =
      IOOps.flatEffectOnFalse(test, effect)

    def effectOnFail[T, _](value: Option[T], effect: => IO[_]): IO[Unit] =
      IOOps.effectOnFail(value, effect)

    def flatEffectOnNone[T, _](value: IO[Option[T]], effect: => IO[_]): IO[Unit] =
      IOOps.flatEffectOnNone(value, effect)

    def effectOnPure[T, _](value: Option[T], effect: T => IO[_]): IO[Unit] =
      IOOps.effectOnPure(value, effect)

    def flatEffectOnSome[T, _](value: IO[Option[T]], effect: T => IO[_]): IO[Unit] =
      IOOps.flatEffectOnSome(value, effect)

    def effectOnFail[T, _](value: Result[T], effect: Anomaly => IO[_]): IO[Unit] =
      IOOps.effectOnFail(value, effect)

    def flatEffectOnIncorrect[T, _](value: IO[Result[T]], effect: Anomaly => IO[_]): IO[Unit] =
      IOOps.flatEffectOnIncorrect(value, effect)

    def flatEffectOnCorrect[T, _](value: IO[Result[T]], effect: T => IO[_]): IO[Unit] =
      IOOps.flatEffectOnCorrect(value, effect)

    def effectOnPure[T, _](value: Result[T], effect: T => IO[_]): IO[Unit] =
      IOOps.effectOnPure(value, effect)

    //=========================================================================
    //============================== Transformers =============================
    //=========================================================================

    def bimap[T, R](value: IO[T], good: T => R, bad: Throwable => Anomaly): IO[R] =
      IOOps.bimap(value, good, bad)

    def bimap[T, R](value: IO[T], result: Result[T] => Result[R]): IO[R] =
      IOOps.bimap(value, result)

    def bimapThr[T, R](value: IO[T], good: T => R, bad: Throwable => Throwable): IO[R] =
      IOOps.bimapThr(value, good, bad)

    def morph[T, R](value: IO[T], good: T => R, bad: Throwable => R): IO[R] =
      IOOps.morph(value, good, bad)

    def morph[T, R](value: IO[T], result: Result[T] => R): IO[R] =
      IOOps.morph(value, result)

    def discardContent[_](value: IO[_]): IO[Unit] =
      IOOps.discardContent(value)

    //=========================================================================
    //=============================== Traversals ==============================
    //=========================================================================

    def traverse[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => IO[B])(
      implicit
      cbf: CanBuildFrom[C[A], B, C[B]]
    ): IO[C[B]] = IOOps.traverse(col)(fn)

    def sequence[A, M[X] <: TraversableOnce[X]](in: M[IO[A]])(
      implicit
      cbf: CanBuildFrom[M[IO[A]], A, M[A]]
    ): IO[M[A]] = IOOps.sequence(in)

    def serialize[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => IO[B])(
      implicit
      cbf: CanBuildFrom[C[A], B, C[B]]
    ): IO[C[B]] = IOOps.serialize(col)(fn)

  }

  /**
    *
    */
  final class ReferenceOps[T](val value: IO[T]) extends AnyVal {

    def attempResult: IO[Result[T]] =
      IOOps.attemptResult(value)

    def asFutureUnsafe(): Future[T] =
      IOOps.asFutureUnsafe(value)

    def asTask: Task[T] =
      IOOps.asTask(value)

    def unsafeSyncGet(): T =
      IOOps.unsafeSyncGet(value)

    def bimap[R](good: T => R, bad: Throwable => Anomaly): IO[R] =
      IOOps.bimap(value, good, bad)

    def bimap[R](result: Result[T] => Result[R]): IO[R] =
      IOOps.bimap(value, result)

    def bimapThr[R](good: T => R, bad: Throwable => Throwable): IO[R] =
      IOOps.bimapThr(value, good, bad)

    def morph[R](good: T => R, bad: Throwable => R): IO[R] =
      IOOps.morph(value, good, bad)

    def morph[R](result: Result[T] => R): IO[R] =
      IOOps.morph(value, result)

    def discardContent: IO[Unit] =
      IOOps.discardContent(value)
  }

  /**
    *
    *
    */
  final class NestedOptionOps[T](private[this] val nopt: IO[Option[T]]) {

    def unpack(ifNone: => Anomaly): IO[T] =
      IOOps.unpackOption(nopt, ifNone)

    def unpackThr(ifNone: => Throwable): IO[T] =
      IOOps.unpackOptionThr(nopt, ifNone)

    def effectOnFail[_](effect: => IO[_]): IO[Unit] =
      IOOps.flatEffectOnNone(nopt, effect)

    def effectOnPure[_](effect: T => IO[_]): IO[Unit] =
      IOOps.flatEffectOnSome(nopt, effect)

  }

  /**
    *
    */
  final class NestedResultOps[T](private[this] val result: IO[Result[T]]) {

    def unpack: IO[T] =
      IOOps.unpackResult(result)

    def effectOnFail[_](effect: Anomaly => IO[_]): IO[Unit] =
      IOOps.flatEffectOnIncorrect(result, effect)

    def effectOnPure[_](effect: T => IO[_]): IO[Unit] =
      IOOps.flatEffectOnCorrect(result, effect)
  }

  /**
    *
    *
    */
  final class BooleanOps(private[this] val test: Boolean) {

    def condIO[T](good: => T, bad: => Anomaly): IO[T] =
      IOOps.cond(test, good, bad)

    def condIOThr[T](good: => T, bad: => Throwable): IO[T] =
      IOOps.condThr(test, good, bad)

    def condWithIO[T](good: => IO[T], bad: => Anomaly): IO[T] =
      IOOps.condWith(test, good, bad)

    def condWithIOThr[T](good: => IO[T], bad: => Throwable): IO[T] =
      IOOps.condWithThr(test, good, bad)

    def failOnTrueIO(bad: => Anomaly): IO[Unit] =
      IOOps.failOnTrue(test, bad)

    def failOnTrueIOThr(bad: => Throwable): IO[Unit] =
      IOOps.failOnTrueThr(test, bad)

    def failOnFalseIO(bad: => Anomaly): IO[Unit] =
      IOOps.failOnFalse(test, bad)

    def failOnFalseIOThr(bad: => Throwable): IO[Unit] =
      IOOps.failOnFalseThr(test, bad)

    def effectOnFalseIO[_](effect: => IO[_]): IO[_] =
      IOOps.effectOnFalse(test, effect)

    def effectOnTrueIO[_](effect: => IO[_]): IO[Unit] =
      IOOps.effectOnTrue(test, effect)

  }

  /**
    *
    *
    */
  final class NestedBooleanOps(private[this] val test: IO[Boolean]) {

    def cond[T](good: => T, bad: => Anomaly): IO[T] =
      IOOps.flatCond(test, good, bad)

    def condThr[T](good: => T, bad: => Throwable): IO[T] =
      IOOps.flatCondThr(test, good, bad)

    def condWith[T](good: => IO[T], bad: => Anomaly): IO[T] =
      IOOps.flatCondWith(test, good, bad)

    def condWithThr[T](good: => IO[T], bad: => Throwable): IO[T] =
      IOOps.flatCondWithThr(test, good, bad)

    def failOnTrue(bad: => Anomaly): IO[Unit] =
      IOOps.flatFailOnTrue(test, bad)

    def failOnTrueThr(bad: => Throwable): IO[Unit] =
      IOOps.flatFailOnTrueThr(test, bad)

    def failOnFalse(bad: => Anomaly): IO[Unit] =
      IOOps.flatFailOnFalse(test, bad)

    def failOnFalseThr(bad: => Throwable): IO[Unit] =
      IOOps.flatFailOnFalseThr(test, bad)

    def effectOnFalse[_](effect: => IO[_]): IO[_] =
      IOOps.flatEffectOnFalse(test, effect)

    def effectOnTrue[_](effect: => IO[_]): IO[_] =
      IOOps.flatEffectOnTrue(test, effect)

  }
}

/**
  *
  */
object IOOps {
  import cats.syntax.applicativeError._
  import cats.syntax.monadError._

  def pure[T](value: T): IO[T] =
    IO.pure(value)

  def fail[T](bad: Anomaly): IO[T] =
    IO.raiseError(bad.asThrowable)

  def failThr[T](bad: Throwable): IO[T] =
    IO.raiseError(bad)

  // —— def unit: IO[Unit] —— already defined on IO object

  def fromOption[T](opt: Option[T], ifNone: => Anomaly): IO[T] = opt match {
    case None        => IOOps.fail(ifNone)
    case Some(value) => IOOps.pure(value)
  }

  def suspendOption[T](opt: => Option[T], ifNone: => Anomaly): IO[T] =
    IO.suspend(IOOps.fromOption(opt, ifNone))

  def fromOptionThr[T](opt: Option[T], ifNone: => Throwable): IO[T] = opt match {
    case None        => IOOps.failThr(ifNone)
    case Some(value) => IOOps.pure(value)
  }

  def suspendOptionThr[T](opt: => Option[T], ifNone: => Throwable): IO[T] =
    IO.suspend(IOOps.fromOptionThr(opt, ifNone))

  def fromTry[T](tr: Try[T]): IO[T] = tr match {
    case scala.util.Success(v) => IO.pure(v)
    case scala.util.Failure(t) => IO.raiseError(t)
  }

  def suspendTry[T](tr: => Try[T]): IO[T] =
    IO.suspend(IOOps.fromTry(tr))

  def fromEither[L, R](either: Either[L, R], transformLeft: L => Anomaly): IO[R] = either match {
    case Left(value)  => IOOps.fail(transformLeft(value))
    case Right(value) => IOOps.pure(value)
  }

  def suspendEither[L, R](either: => Either[L, R], transformLeft: L => Anomaly): IO[R] =
    IO.suspend(IOOps.fromEither(either, transformLeft))

  def fromEitherThr[L, R](either: Either[L, R])(implicit ev: L <:< Throwable): IO[R] = either match {
    case Left(value)  => IOOps.failThr(ev(value))
    case Right(value) => IOOps.pure(value)
  }

  def suspendEitherThr[L, R](either: => Either[L, R])(implicit ev: L <:< Throwable): IO[R] =
    IO.suspend(IOOps.fromEitherThr(either)(ev))

  def fromEitherThr[L, R](either: Either[L, R], transformLeft: L => Throwable): IO[R] = either match {
    case Left(value)  => IOOps.failThr(transformLeft(value))
    case Right(value) => IOOps.pure(value)
  }

  def suspendEitherThr[L, R](either: => Either[L, R], transformLeft: L => Throwable): IO[R] =
    IO.suspend(IOOps.fromEitherThr(either, transformLeft))

  def fromResult[T](result: Result[T]): IO[T] = result match {
    case Left(value)  => IOOps.fail(value)
    case Right(value) => IOOps.pure(value)
  }

  def suspendResult[T](result: => Result[T]): IO[T] =
    IO.suspend(IOOps.fromResult(result))

  def fromFuturePure[T](value: Future[T])(implicit ec: ExecutionContext): IO[T] =
    IO.fromFuture(IO(value))

  def suspendFuture[T](value: => Future[T])(implicit ec: ExecutionContext): IO[T] =
    IO.fromFuture(IO(value))

  def fromTask[T](task: Task[T])(implicit sc: Scheduler): IO[T] =
    TaskOps.asIO(task)

  def cond[T](test: Boolean, good: => T, bad: => Anomaly): IO[T] =
    if (test) IOOps.pure(good) else IOOps.fail(bad)

  def condThr[T](test: Boolean, good: => T, bad: => Throwable): IO[T] =
    if (test) IOOps.pure(good) else IOOps.failThr(bad)

  def condWith[T](test: Boolean, good: => IO[T], bad: => Anomaly): IO[T] =
    if (test) good else IOOps.fail(bad)

  def condWithThr[T](test: Boolean, good: => IO[T], bad: => Throwable): IO[T] =
    if (test) good else IOOps.failThr(bad)

  def flatCond[T](test: IO[Boolean], good: => T, bad: => Anomaly): IO[T] =
    test.flatMap(t => IOOps.cond(t, good, bad))

  def flatCondThr[T](test: IO[Boolean], good: => T, bad: => Throwable): IO[T] =
    test.flatMap(t => IOOps.condThr(t, good, bad))

  def flatCondWith[T](test: IO[Boolean], good: => IO[T], bad: => Anomaly): IO[T] =
    test.flatMap(t => IOOps.condWith(t, good, bad))

  def flatCondWithThr[T](test: IO[Boolean], good: => IO[T], bad: => Throwable): IO[T] =
    test.flatMap(t => IOOps.condWithThr(t, good, bad))

  def failOnTrue(test: Boolean, bad: => Anomaly): IO[Unit] =
    if (test) IOOps.fail(bad) else IO.unit

  def failOnTrueThr(test: Boolean, bad: => Throwable): IO[Unit] =
    if (test) IOOps.failThr(bad) else IO.unit

  def failOnFalse(test: Boolean, bad: => Anomaly): IO[Unit] =
    if (!test) IOOps.fail(bad) else IO.unit

  def failOnFalseThr(test: Boolean, bad: => Throwable): IO[Unit] =
    if (!test) IOOps.failThr(bad) else IO.unit

  def flatFailOnTrue(test: IO[Boolean], bad: => Anomaly): IO[Unit] =
    test.flatMap(t => IOOps.failOnTrue(t, bad))

  def flatFailOnTrueThr(test: IO[Boolean], bad: => Throwable): IO[Unit] =
    test.flatMap(t => IOOps.failOnTrueThr(t, bad))

  def flatFailOnFalse(test: IO[Boolean], bad: => Anomaly): IO[Unit] =
    test.flatMap(t => IOOps.failOnFalse(t, bad))

  def flatFailOnFalseThr(test: IO[Boolean], bad: => Throwable): IO[Unit] =
    test.flatMap(t => IOOps.failOnFalseThr(t, bad))

  def unpackOption[T](nopt: IO[Option[T]], ifNone: => Anomaly): IO[T] =
    nopt.flatMap {
      case None    => IOOps.fail(ifNone)
      case Some(v) => IOOps.pure(v)
    }

  def unpackOptionThr[T](nopt: IO[Option[T]], ifNone: => Throwable): IO[T] =
    nopt.flatMap {
      case None    => IOOps.failThr(ifNone)
      case Some(v) => IOOps.pure(v)
    }

  def unpackResult[T](value: IO[Result[T]]): IO[T] = value.flatMap {
    case Left(a)  => IOOps.fail(a)
    case Right(a) => IOOps.pure(a)
  }

  def attemptResult[T](value: IO[T]): IO[Result[T]] =
    value.attempt.map((e: Either[Throwable, T]) => Result.fromEitherThr(e))

  def asFutureUnsafe[T](value: IO[T]): Future[T] =
    value.unsafeToFuture()

  def asTask[T](value: IO[T]): Task[T] =
    TaskOps.fromIO(value)

  def unsafeSyncGet[T](value: IO[T]): T =
    value.unsafeRunSync()

  //=========================================================================
  //================= Run side-effects in varrying scenarios ================
  //=========================================================================

  def effectOnTrue[_](test: Boolean, effect: => IO[_]): IO[Unit] =
    if (test) IOOps.discardContent(effect) else IO.unit

  def flatEffectOnTrue[_](test: IO[Boolean], effect: => IO[_]): IO[Unit] =
    test.flatMap(t => IOOps.effectOnTrue(t, effect))

  def effectOnFalse[_](test: Boolean, effect: => IO[_]): IO[Unit] =
    if (!test) IOOps.discardContent(effect) else IO.unit

  def flatEffectOnFalse[_](test: IO[Boolean], effect: => IO[_]): IO[Unit] =
    test.flatMap(t => IOOps.effectOnFalse(t, effect))

  def effectOnFail[T, _](value: Option[T], effect: => IO[_]): IO[Unit] =
    if (value.isEmpty) IOOps.discardContent(effect) else IO.unit

  def flatEffectOnNone[T, _](value: IO[Option[T]], effect: => IO[_]): IO[Unit] =
    value.flatMap(opt => IOOps.effectOnFail(opt, effect))

  def effectOnPure[T, _](value: Option[T], effect: T => IO[_]): IO[Unit] =
    value match {
      case None    => IO.unit
      case Some(v) => IOOps.discardContent(effect(v))

    }

  def flatEffectOnSome[T, _](value: IO[Option[T]], effect: T => IO[_]): IO[Unit] =
    value.flatMap(opt => IOOps.effectOnPure(opt, effect))

  def effectOnFail[T, _](value: Result[T], effect: Anomaly => IO[_]): IO[Unit] = value match {
    case Correct(_)         => IO.unit
    case Incorrect(anomaly) => IOOps.discardContent(effect(anomaly))
  }

  def flatEffectOnIncorrect[T, _](value: IO[Result[T]], effect: Anomaly => IO[_]): IO[Unit] =
    value.flatMap(result => IOOps.effectOnFail(result, effect))

  def effectOnPure[T, _](value: Result[T], effect: T => IO[_]): IO[Unit] =
    value match {
      case Incorrect(_) => IO.unit
      case Correct(v)   => IOOps.discardContent(effect(v))
    }

  def flatEffectOnCorrect[T, _](value: IO[Result[T]], effect: T => IO[_]): IO[Unit] =
    value.flatMap(result => IOOps.effectOnPure(result, effect))

  //=========================================================================
  //============================== Transformers =============================
  //=========================================================================

  def bimap[T, R](value: IO[T], good: T => R, bad: Throwable => Anomaly): IO[R] =
    value.map(good).adaptError {
      case NonFatal(t) => bad(t).asThrowable
    }

  def bimap[T, R](value: IO[T], result: Result[T] => Result[R]): IO[R] =
    IOOps.attemptResult(value).map(result).flatMap {
      case Correct(v)   => IOOps.pure(v)
      case Incorrect(v) => IOOps.fail(v)
    }

  def bimapThr[T, R](value: IO[T], good: T => R, bad: Throwable => Throwable): IO[R] =
    value.map(good).adaptError {
      case NonFatal(t) => bad(t)
    }

  def morph[T, R](value: IO[T], good: T => R, bad: Throwable => R): IO[R] =
    value.map(good).recover {
      case NonFatal(t) => bad(t)
    }

  def morph[T, R](value: IO[T], result: Result[T] => R): IO[R] =
    IOOps.attemptResult(value).map(result)

  private val UnitFunction: Any => Unit = _ => ()

  def discardContent[_](value: IO[_]): IO[Unit] =
    value.map(UnitFunction)

  //=========================================================================
  //=============================== Traversals ==============================
  //=========================================================================

  def traverse[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => IO[B])(
    implicit
    cbf: CanBuildFrom[C[A], B, C[B]]
  ): IO[C[B]] = {
    import cats.instances.list._
    import cats.syntax.traverse._
    import scala.collection.mutable

    if (col.isEmpty) {
      IO.pure(cbf.apply().result())
    }
    else {
      //OK, super inneficient, need a better implementation
      val result:  IO[List[B]] = col.toList.traverse(fn)
      val builder: mutable.Builder[B, C[B]] = cbf.apply()
      result.map(_.foreach(e => builder.+=(e))).map(_ => builder.result())
    }
  }

  def sequence[A, M[X] <: TraversableOnce[X]](in: M[IO[A]])(
    implicit
    cbf: CanBuildFrom[M[IO[A]], A, M[A]]
  ): IO[M[A]] = IOOps.traverse(in)(identity)

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
    *   val allPatches: IO[Seq[Patch]] = IO.serialize(patches){ patch: Patch =>
    *     IO {
    *       //apply patch
    *     }
    *   }
    *   //... and so on, and so on!
    * }}}
    *
    *
    */
  def serialize[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => IO[B])(
    implicit
    cbf: CanBuildFrom[C[A], B, C[B]]
  ): IO[C[B]] = IOOps.traverse(col)(fn)(cbf)

  //=========================================================================
  //=============================== Constants ===============================
  //=========================================================================
}
