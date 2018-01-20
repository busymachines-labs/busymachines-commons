package busymachines.result

import busymachines.core.Anomaly
import scala.util.Try

/**
  *
  *
  */
final class OptionToResultOps[T](private[this] val opt: Option[T]) {

  def toResult(ifNone: => Anomaly): Result[T] = Result.fromOption(opt, ifNone)

}

/**
  *
  *
  */
final class ResultOptionToResultOps[T](private[this] val ropt: Result[Option[T]]) {

  def flatten(ifNone: => Anomaly): Result[T] =
    ropt flatMap (opt => Result.fromOption(opt, ifNone))
}

/**
  *
  *
  */
final class EitherToResultOps[L, R](private[this] val eit: Either[L, R]) {

  def toResult(implicit ev: L <:< Throwable): Result[R] = Result.fromEither(eit)

  def toResult(transformLeft: L => Anomaly): Result[R] = Result.fromEither(eit, transformLeft)
}

/**
  *
  *
  */
final class TryToResultOps[T](private[this] val t: Try[T]) {

  def toResult: Result[T] = Result.fromTry(t)
}

/**
  *
  *
  */
final class BooleanToResultOps(private[this] val b: Boolean) {

  def toResult[T](correct: => T, anomaly: => Anomaly): Result[T] =
    Result.cond(b, correct, anomaly)

  def failOnTrue(anomaly: => Anomaly): Result[Unit] =
    if (b) Result.incorrect(anomaly) else Result.unit

  def failOnFalse(anomaly: => Anomaly): Result[Unit] =
    if (!b) Result.incorrect(anomaly) else Result.unit
}

/**
  *
  *
  */
final class ResultBooleanToResultOps(private[this] val br: Result[Boolean]) {

  def cond[T](correct: => T, anomaly: => Anomaly): Result[T] =
    br flatMap (b => Result.cond(b, correct, anomaly))

  def failOnTrue(anomaly: => Anomaly): Result[Unit] =
    br flatMap (b => if (b) Result.incorrect(anomaly) else Result.unit)

  def failOnFalse(anomaly: => Anomaly): Result[Unit] =
    br flatMap (b => if (!b) Result.incorrect(anomaly) else Result.unit)

}
