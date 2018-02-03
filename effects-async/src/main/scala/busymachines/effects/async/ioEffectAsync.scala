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

  /**
    * N.B. pass only pure values. If you have side effects, then
    * use [[IO.apply]] to suspend them inside this future.
    */
  def pure[T](value: T): IO[T] =
    IO.pure(value)

  /**
    * Failed effect but with an [[Anomaly]]
    */
  def fail[T](bad: Anomaly): IO[T] =
    IO.raiseError(bad.asThrowable)

  /**
    * Failed effect but with a [[Throwable]]
    */
  def failThr[T](bad: Throwable): IO[T] =
    IO.raiseError(bad)

  // —— def unit: IO[Unit] —— already defined on IO object

  /**
    * Lift this [[Option]] and transform it into a failed effect if it is [[None]]
    */
  def fromOption[T](opt: Option[T], ifNone: => Anomaly): IO[T] = opt match {
    case None        => IOOps.fail(ifNone)
    case Some(value) => IOOps.pure(value)
  }

  /**
    *
    * Suspend any side-effects that might happen during the creation of this [[Option]].
    * If the option is [[None]] then we get back a failed effect with the given [[Anomaly]]
    *
    * N.B. this is useless if the [[Option]] was previously assigned to a "val".
    * You might as well use [[IOOps.fromOption]]
    */
  def suspendOption[T](opt: => Option[T], ifNone: => Anomaly): IO[T] =
    IO.suspend(IOOps.fromOption(opt, ifNone))

  /**
    * Lift this [[Option]] and transform it into a failed effect if it is [[None]]
    */
  def fromOptionThr[T](opt: Option[T], ifNone: => Throwable): IO[T] = opt match {
    case None        => IOOps.failThr(ifNone)
    case Some(value) => IOOps.pure(value)
  }

  /**
    *
    * Suspend any side-effects that might happen during the creation of this [[Option]].
    * If the option is [[None]] then we get back a failed effect with the given [[Throwable]]
    *
    * N.B. this is useless if the [[Option]] was previously assigned to a "val".
    * You might as well use [[IOOps.fromOption]]
    */
  def suspendOptionThr[T](opt: => Option[T], ifNone: => Throwable): IO[T] =
    IO.suspend(IOOps.fromOptionThr(opt, ifNone))

  /**
    * [[scala.util.Failure]] is sequenced into this effect
    * [[scala.util.Success]] is the pure value of this effect
    */
  def fromTry[T](tr: Try[T]): IO[T] = tr match {
    case scala.util.Success(v) => IO.pure(v)
    case scala.util.Failure(t) => IO.raiseError(t)
  }

  /**
    *
    * Suspend any side-effects that might happen during the creation of this [[Try]].
    * Failed Try yields a failed effect
    * Successful Try yields a pure effect
    *
    * N.B. this is useless if the [[Try]] was previously assigned to a "val".
    * You might as well use [[IOOps.fromTry]]
    */
  def suspendTry[T](tr: => Try[T]): IO[T] =
    IO.suspend(IOOps.fromTry(tr))

  /**
    * Lift this [[Either]] and transform its left-hand side into a [[Anomaly]] and sequence it within
    * this effect, yielding a failed effect.
    */
  def fromEither[L, R](either: Either[L, R], transformLeft: L => Anomaly): IO[R] = either match {
    case Left(value)  => IOOps.fail(transformLeft(value))
    case Right(value) => IOOps.pure(value)
  }

  /**
    *
    * Suspend any side-effects that might happen during the creation of this [[Either]].
    * And transform its left-hand side into a [[Anomaly]] and sequence it within
    * this effect, yielding a failed effect.
    *
    * N.B. this is useless if the [[Either]] was previously assigned to a "val".
    * You might as well use [[fromEither]]
    */
  def suspendEither[L, R](either: => Either[L, R], transformLeft: L => Anomaly): IO[R] =
    IO.suspend(IOOps.fromEither(either, transformLeft))

  /**
    * Lift this [[Either]] and  sequence its left-hand-side [[Throwable]] within this effect
    * if it is a [[Throwable]].
    */
  def fromEitherThr[L, R](either: Either[L, R])(implicit ev: L <:< Throwable): IO[R] = either match {
    case Left(value)  => IOOps.failThr(ev(value))
    case Right(value) => IOOps.pure(value)
  }

  /**
    *
    * Suspend any side-effects that might happen during the creation of this [[Either]].
    * And sequence its left-hand-side [[Throwable]] within this effect if it is a [[Throwable]]
    *
    * N.B. this is useless if the [[Either]] was previously assigned to a "val".
    * You might as well use [[fromEither]]
    */
  def suspendEitherThr[L, R](either: => Either[L, R])(implicit ev: L <:< Throwable): IO[R] =
    IO.suspend(IOOps.fromEitherThr(either)(ev))

  /**
    * Lift this [[Either]] and transform its left-hand side into a [[Throwable]] and sequence it within
    * this effect, yielding a failed effect.
    */
  def fromEitherThr[L, R](either: Either[L, R], transformLeft: L => Throwable): IO[R] = either match {
    case Left(value)  => IOOps.failThr(transformLeft(value))
    case Right(value) => IOOps.pure(value)
  }

  /**
    * Suspend any side-effects that might happen during the creation of this [[Either]].
    * And transform its left-hand side into a [[Throwable]] and sequence it within
    * this effect, yielding a failed effect.
    *
    * N.B. this is useless if the [[Either]] was previously assigned to a "val".
    * You might as well use [[fromEither]]
    */
  def suspendEitherThr[L, R](either: => Either[L, R], transformLeft: L => Throwable): IO[R] =
    IO.suspend(IOOps.fromEitherThr(either, transformLeft))

  /**
    *
    * Lift the [[Result]] in this effect
    * [[Incorrect]] becomes a failed effect
    * [[Correct]] becomes a pure effect
    *
    */
  def fromResult[T](result: Result[T]): IO[T] = result match {
    case Left(value)  => IOOps.fail(value)
    case Right(value) => IOOps.pure(value)
  }

  /**
    * Suspend any side-effects that might happen during the creation of this [[Result]].
    * Other than that it has the semantics of [[fromResult]]
    *
    * N.B. this is useless if the [[Result]] was previously assigned to a "val".
    * You might as well use [[fromResult]]
    */
  def suspendResult[T](result: => Result[T]): IO[T] =
    IO.suspend(IOOps.fromResult(result))

  /**
    * !!! USE WITH CARE !!!
    *
    * In 99% of the cases you actually want to use [[suspendFuture]]
    *
    * If you are certain that this [[Future]] is pure, then you can use
    * this method to lift it into [[IO]].
    */
  def fromFuturePure[T](value: Future[T])(implicit ec: ExecutionContext): IO[T] =
    IO.fromFuture(IO(value))

  /**
    *
    * Suspend the side-effects of this [[Future]] into an [[IO]]. This is the
    * most important operation when it comes to inter-op between the two effects.
    *
    * Usage. N.B. that this only makes sense if the creation of the Future itself
    * is also suspended in the [[IO]].
    * {{{
    *   def writeToDB(v: Int, s: String): Future[Long] = ???
    *   //...
    *   val io = IO.suspendFuture(writeToDB(42, "string"))
    *   //no database writes happened yet, since the future did
    *   //not do its annoying running of side-effects immediately!
    *
    *   //when we want side-effects:
    *   io.unsafeGetSync()
    * }}}
    *
    * This is almost useless unless you are certain that ??? is a pure computation
    * might as well use IO.fromFuturePure(???)
    * {{{
    *   val f: Future[Int] = Future.apply(???)
    *   IO.suspendFuture(f)
    * }}}
    *
    */
  def suspendFuture[T](value: => Future[T])(implicit ec: ExecutionContext): IO[T] =
    IO.fromFuture(IO(value))

  /**
    *
    * Transform a monix [[Task]] into an [[IO]]. No gotchas because pure
    * functional programming is awesome.
    */
  def fromTask[T](task: Task[T])(implicit sc: Scheduler): IO[T] =
    TaskOps.asIO(task)

  /**
    * @return
    *   pure effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[Anomaly]] if boolean is false
    */
  def cond[T](test: Boolean, good: => T, bad: => Anomaly): IO[T] =
    if (test) IOOps.pure(good) else IOOps.fail(bad)

  /**
    * @return
    *   pure effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[Throwable]] if boolean is false
    */
  def condThr[T](test: Boolean, good: => T, bad: => Throwable): IO[T] =
    if (test) IOOps.pure(good) else IOOps.failThr(bad)

  /**
    * @return
    *   effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[Anomaly]] if boolean is false
    */
  def condWith[T](test: Boolean, good: => IO[T], bad: => Anomaly): IO[T] =
    if (test) good else IOOps.fail(bad)

  /**
    * @return
    *   effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[Throwable]] if boolean is false
    */
  def condWithThr[T](test: Boolean, good: => IO[T], bad: => Throwable): IO[T] =
    if (test) good else IOOps.failThr(bad)

  /**
    * @return
    *   pure effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[Anomaly]] if boolean is false
    *   failed effect if the effect wrapping the boolean is already failed
    */
  def flatCond[T](test: IO[Boolean], good: => T, bad: => Anomaly): IO[T] =
    test.flatMap(t => IOOps.cond(t, good, bad))

  /**
    * @return
    *   pure effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[Throwable]] if boolean is false
    *   failed effect if the effect wrapping the boolean is already failed
    */
  def flatCondThr[T](test: IO[Boolean], good: => T, bad: => Throwable): IO[T] =
    test.flatMap(t => IOOps.condThr(t, good, bad))

  /**
    * @return
    *   effect resulted from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[Anomaly]] if boolean is false
    *   failed effect if the effect wrapping the boolean is already failed
    */
  def flatCondWith[T](test: IO[Boolean], good: => IO[T], bad: => Anomaly): IO[T] =
    test.flatMap(t => IOOps.condWith(t, good, bad))

  /**
    * @return
    *   effect resulted from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[Throwable]] if boolean is false
    *   failed effect if the effect wrapping the boolean is already failed
    */
  def flatCondWithThr[T](test: IO[Boolean], good: => IO[T], bad: => Throwable): IO[T] =
    test.flatMap(t => IOOps.condWithThr(t, good, bad))

  /**
    * @return
    *   Failed effect, if the boolean is true
    */
  def failOnTrue(test: Boolean, bad: => Anomaly): IO[Unit] =
    if (test) IOOps.fail(bad) else IO.unit

  /**
    * @return
    *   Failed effect, if the boolean is true
    */
  def failOnTrueThr(test: Boolean, bad: => Throwable): IO[Unit] =
    if (test) IOOps.failThr(bad) else IO.unit

  /**
    * @return
    *   Failed effect, if the boolean is false
    */
  def failOnFalse(test: Boolean, bad: => Anomaly): IO[Unit] =
    if (!test) IOOps.fail(bad) else IO.unit

  /**
    * @return
    *   Failed effect, if the boolean is false
    */
  def failOnFalseThr(test: Boolean, bad: => Throwable): IO[Unit] =
    if (!test) IOOps.failThr(bad) else IO.unit

  /**
    * @return
    *   Failed effect, if the boxed boolean is true, or if the original effect is failed
    */
  def flatFailOnTrue(test: IO[Boolean], bad: => Anomaly): IO[Unit] =
    test.flatMap(t => IOOps.failOnTrue(t, bad))

  /**
    * @return
    *   Failed effect, if the boxed boolean is true, or if the original effect is failed
    */
  def flatFailOnTrueThr(test: IO[Boolean], bad: => Throwable): IO[Unit] =
    test.flatMap(t => IOOps.failOnTrueThr(t, bad))

  /**
    * @return
    *   Failed effect, if the boxed boolean is false, or if the original effect is failed
    */
  def flatFailOnFalse(test: IO[Boolean], bad: => Anomaly): IO[Unit] =
    test.flatMap(t => IOOps.failOnFalse(t, bad))

  /**
    * @return
    *   Failed effect, if the boxed boolean is false, or if the original effect is failed
    */
  def flatFailOnFalseThr(test: IO[Boolean], bad: => Throwable): IO[Unit] =
    test.flatMap(t => IOOps.failOnFalseThr(t, bad))

  /**
    * Sequences the given [[Anomaly]] of the if Option is [[None]] into this effect
    *
    * The failure of this effect takes precedence over the given failure
    */
  def unpackOption[T](nopt: IO[Option[T]], ifNone: => Anomaly): IO[T] =
    nopt.flatMap {
      case None    => IOOps.fail(ifNone)
      case Some(v) => IOOps.pure(v)
    }

  /**
    * Sequences the given [[Throwable]] of the if Option is [[None]] into this effect
    *
    * The failure of this effect takes precedence over the given failure
    */
  def unpackOptionThr[T](nopt: IO[Option[T]], ifNone: => Throwable): IO[T] =
    nopt.flatMap {
      case None    => IOOps.failThr(ifNone)
      case Some(v) => IOOps.pure(v)
    }

  /**
    * Sequences the failure of the [[Incorrect]] [[Result]] into this effect.
    *
    * The failure of this effect takes precedence over the failure of the [[Incorrect]] value.
    */
  def unpackResult[T](value: IO[Result[T]]): IO[T] = value.flatMap {
    case Left(a)  => IOOps.fail(a)
    case Right(a) => IOOps.pure(a)
  }

  /**
    * Makes the failure, and non-failure part of this effect explicit in a [[Result]] type.
    *
    * This transforms any failed effect, into a pure one with and [[Incorrect]] value.
    */
  def attemptResult[T](value: IO[T]): IO[Result[T]] =
    value.attempt.map((e: Either[Throwable, T]) => Result.fromEitherThr(e))

  /**
    * !!! USE WITH CARE !!!
    *
    * The moment you call this, the side-effects suspended in this [[IO]] start being
    * executed.
    */
  def asFutureUnsafe[T](value: IO[T]): Future[T] =
    value.unsafeToFuture()

  /**
    * No gotchas. Pure functional programming = <3
    */
  def asTask[T](value: IO[T]): Task[T] =
    TaskOps.fromIO(value)

  /**
    * !!! USE WITH CARE !!!
    *
    * Mostly here for testing. There is almost no reason whatsover for you to explicitely
    * call this in your code. You have libraries that do this for you "at the end of the world"
    * parts of your program: e.g. akka-http when waiting for the response value to a request.
    */
  def unsafeSyncGet[T](value: IO[T]): T =
    value.unsafeRunSync()

  //=========================================================================
  //================= Run side-effects in varrying scenarios ================
  //=========================================================================

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[Boolean]] is ``true``
    *   Does not run the side-effect if the value is also a failed effect.
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  def effectOnTrue[_](test: Boolean, effect: => IO[_]): IO[Unit] =
    if (test) IOOps.discardContent(effect) else IO.unit

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[Boolean]] is ``true``
    *   Does not run the side-effect if the value is also a failed effect.
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  def flatEffectOnTrue[_](test: IO[Boolean], effect: => IO[_]): IO[Unit] =
    test.flatMap(t => IOOps.effectOnTrue(t, effect))

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[Boolean]] is ``false``
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  def effectOnFalse[_](test: Boolean, effect: => IO[_]): IO[Unit] =
    if (!test) IOOps.discardContent(effect) else IO.unit

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[Boolean]] is ``false``
    *   Does not run the side-effect if the value is also a failed effect.
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  def flatEffectOnFalse[_](test: IO[Boolean], effect: => IO[_]): IO[Unit] =
    test.flatMap(t => IOOps.effectOnFalse(t, effect))

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[Option]] is [[None]]
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  def effectOnFail[T, _](value: Option[T], effect: => IO[_]): IO[Unit] =
    if (value.isEmpty) IOOps.discardContent(effect) else IO.unit

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[Option]] is [[None]]
    *   Does not run the side-effect if the value is also a failed effect.
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  def flatEffectOnNone[T, _](value: IO[Option[T]], effect: => IO[_]): IO[Unit] =
    value.flatMap(opt => IOOps.effectOnFail(opt, effect))

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[Option]] is [[Some]]
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  def effectOnPure[T, _](value: Option[T], effect: T => IO[_]): IO[Unit] =
    value match {
      case None    => IO.unit
      case Some(v) => IOOps.discardContent(effect(v))

    }

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[Option]] is [[Some]]
    *   Does not run the side-effect if the value is also a failed effect.
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  def flatEffectOnSome[T, _](value: IO[Option[T]], effect: T => IO[_]): IO[Unit] =
    value.flatMap(opt => IOOps.effectOnPure(opt, effect))

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[Result]] is [[Incorrect]]
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  def effectOnFail[T, _](value: Result[T], effect: Anomaly => IO[_]): IO[Unit] = value match {
    case Correct(_)         => IO.unit
    case Incorrect(anomaly) => IOOps.discardContent(effect(anomaly))
  }

  /**
    *
    * @param value
    *   Runs the given effect when the boxed value of this [[Result]] is [[Incorrect]]
    *   Does not run the side-effect if the value is also a failed effect.
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  def flatEffectOnIncorrect[T, _](value: IO[Result[T]], effect: Anomaly => IO[_]): IO[Unit] =
    value.flatMap(result => IOOps.effectOnFail(result, effect))

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[Result]] is [[Correct]]
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  def effectOnPure[T, _](value: Result[T], effect: T => IO[_]): IO[Unit] =
    value match {
      case Incorrect(_) => IO.unit
      case Correct(v)   => IOOps.discardContent(effect(v))
    }

  /**
    *
    * @param value
    *   Runs the given effect when the boxed value of this [[Result]] is [[Correct]]
    *   Does not run the side-effect if the value is also a failed effect.
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  def flatEffectOnCorrect[T, _](value: IO[Result[T]], effect: T => IO[_]): IO[Unit] =
    value.flatMap(result => IOOps.effectOnPure(result, effect))

  //=========================================================================
  //============================== Transformers =============================
  //=========================================================================

  /**
    * Used to transform both the "pure" part of the effect, and the "fail" part. Hence the name
    * "bi" map, because it also allows you to change both branches of the effect, not just the
    * happy path.
    */
  def bimap[T, R](value: IO[T], good: T => R, bad: Throwable => Anomaly): IO[R] =
    value.map(good).adaptError {
      case NonFatal(t) => bad(t).asThrowable
    }

  /**
    * Similar to the overload, but the [[Correct]] branch of the result is used to change the "pure" branch of this
    * effect, and [[Incorrect]] branch is used to change the "fail" branch of the effect.
    */
  def bimap[T, R](value: IO[T], result: Result[T] => Result[R]): IO[R] =
    IOOps.attemptResult(value).map(result).flatMap {
      case Correct(v)   => IOOps.pure(v)
      case Incorrect(v) => IOOps.fail(v)
    }

  /**
    * Similar to the overload, but the [[Correct]] branch of the result is used to change the "pure" branch of this
    * effect, and [[Incorrect]] branch is used to change the "fail" branch of the effect.
    *
    * The overload that uses [[Throwable]] instead of [[Anomaly]]
    */
  def bimapThr[T, R](value: IO[T], good: T => R, bad: Throwable => Throwable): IO[R] =
    value.map(good).adaptError {
      case NonFatal(t) => bad(t)
    }

  /**
    *
    * Given the basic two-pronged nature of this effect.
    * the ``good`` function transforms the underlying "pure" (correct, successful, etc) if that's the case.
    * the ``bad`` function transforms the underlying "failure" part of the effect into a "pure" part.
    *
    * Therefore, by using ``morph`` you are defining the rules by which to make the effect into a successful one
    * that does not short-circuit monadic flatMap chains.
    *
    * e.g:
    * {{{
    *   val f: Future[Int] = Future.fail(InvalidInputFailure)
    *   Future.morph(f, (i: Int) => i *2, (t: Throwable) => 42)
    * }}}
    *
    * Undefined behavior if you throw exceptions in the method. DO NOT do that!
    */
  def morph[T, R](value: IO[T], good: T => R, bad: Throwable => R): IO[R] =
    value.map(good).recover {
      case NonFatal(t) => bad(t)
    }

  /**
    * Semantically equivalent to the overload ``morph`` that accepts two functions, but those encoded
    * as the corresponding branches of a Result type.
    *
    * Undefined behavior if you throw exceptions in the method. DO NOT do that!
    */
  def morph[T, R](value: IO[T], result: Result[T] => R): IO[R] =
    IOOps.attemptResult(value).map(result)

  /**
    *
    * Explicitely discard the contents of this effect, and return [[Unit]] instead.
    *
    * N.B. computation, and side-effects captured within this effect are still executed,
    * it's just the final value that is discarded
    *
    */
  def discardContent[_](value: IO[_]): IO[Unit] =
    value.map(UnitFunction)

  //=========================================================================
  //=============================== Traversals ==============================
  //=========================================================================

  /**
    * see:
    * https://typelevel.org/cats/api/cats/Traverse.html
    *
    * {{{
    *   def indexToFilename(i: Int): IO[String] = ???
    *
    *   val fileIndex: List[Int] = List(0,1,2,3,4)
    *   val fileNames: IO[List[String]] = IO.traverse(fileIndex){ i =>
    *     indexToFilename(i)
    *   }
    * }}}
    */
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

  /**
    * see:
    * https://typelevel.org/cats/api/cats/Traverse.html
    *
    * Specialized case of [[traverse]]
    *
    * {{{
    *   def indexToFilename(i: Int): IO[String] = ???
    *
    *   val fileNamesIO: List[IO[String]] = List(0,1,2,3,4).map(indexToFileName)
    *   val fileNames: IO[List[String]] = IO.sequence(fileNamesIO)
    * }}}
    */
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

  private val UnitFunction: Any => Unit = _ => ()
}
