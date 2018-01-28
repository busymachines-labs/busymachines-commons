package busymachines.effects.future

import busymachines.core.Anomaly
import busymachines.effects.result.Result

import scala.util.Try

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 25 Jan 2018
  *
  */
final class OptionToFutureOps[T](private[this] val opt: Option[T]) {

  def asFuture(ifNone: => Anomaly): Future[T] = FutureUtil.fromOption(opt, ifNone)

}

/**
  *
  *
  */
final class FutureOptionToFutureOps[T](private[this] val fopt: Future[Option[T]]) {

  def flatten(ifNone: => Anomaly)(implicit ec: ExecutionContext): Future[T] =
    FutureUtil.flattenOption(fopt, ifNone)
}

/**
  *
  *
  */
final class EitherToFutureOps[L, R](private[this] val eit: Either[L, R]) {

  def asFuture(implicit ev: L <:< Throwable): Future[R] = FutureUtil.fromEither(eit)

  def asFuture(transformLeft: L => Anomaly): Future[R] = FutureUtil.fromEither(eit, transformLeft)
}

/**
  *
  *
  */
final class ResultToFutureOps[R](private[this] val r: Result[R]) {

  def asFuture: Future[R] = FutureUtil.fromResult(r)
}

/**
  *
  *
  */
final class TryToFutureOps[T](private[this] val t: Try[T]) {

  def asFuture: Future[T] = Future.fromTry(t)
}

/**
  *
  *
  */
final class BooleanToFutureOps(private[this] val b: Boolean) {

  def cond[T](ifBranch: => T, elseBranch: => Anomaly): Future[T] =
    FutureUtil.cond(b, ifBranch, elseBranch)

  def failOnTrue(anomaly: => Anomaly): Future[Unit] =
    FutureUtil.failOnTrue(b, anomaly)

  def failOnFalse(anomaly: => Anomaly): Future[Unit] =
    FutureUtil.failOnFalse(b, anomaly)

  def effectOnTrue[T](eff: => Future[T])(implicit ec: ExecutionContext): Future[Unit] =
    FutureUtil.effectOnTrue(b, eff)

  def effectOnFalse[T](eff: => Future[T])(implicit ec: ExecutionContext): Future[Unit] =
    FutureUtil.effectOnFalse(b, eff)

}

/**
  *
  *
  */
final class FutureBooleanToFutureOps(private[this] val fb: Future[Boolean]) {

  def cond[T](correct: => T, anomaly: => Anomaly)(implicit ec: ExecutionContext): Future[T] =
    FutureUtil.flatCond(fb, correct, anomaly)

  def failOnTrue(anomaly: => Anomaly)(implicit ec: ExecutionContext): Future[Unit] =
    FutureUtil.flatFailOnTrue(fb, anomaly)

  def failOnFalse(anomaly: => Anomaly)(implicit ec: ExecutionContext): Future[Unit] =
    FutureUtil.flatFailOnFalse(fb, anomaly)

  def effectOnTrue[T](eff: => Future[T])(implicit ec: ExecutionContext): Future[Unit] =
    FutureUtil.flatEffectOnTrue(fb, eff)

  def effectOnFalse[T](eff: => Future[T])(implicit ec: ExecutionContext): Future[Unit] =
    FutureUtil.flatEffectOnFalse(fb, eff)
}
