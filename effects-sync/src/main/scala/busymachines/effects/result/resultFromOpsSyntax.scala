package busymachines.effects.result

import busymachines.core.Anomaly
import scala.util.Try

/**
  *
  *
  */
final class OptionAsResultOps[T](private[this] val opt: Option[T]) {

  def asResult(ifNone: => Anomaly): Result[T] = Result.fromOption(opt, ifNone)

}

/**
  *
  *
  */
final class ResultOptionAsResultOps[T](private[this] val ropt: Result[Option[T]]) {

  def flatten(ifNone: => Anomaly): Result[T] =
    ropt flatMap (opt => Result.fromOption(opt, ifNone))
}

/**
  *
  *
  */
final class EitherAsResultOps[L, R](private[this] val eit: Either[L, R]) {

  def asResult(implicit ev: L <:< Throwable): Result[R] = Result.fromEither(eit)

  def asResult(transformLeft: L => Anomaly): Result[R] = Result.fromEither(eit, transformLeft)
}

/**
  *
  *
  */
final class TryAsResultOps[T](private[this] val t: Try[T]) {

  def asResult: Result[T] = Result.fromTry(t)
}

/**
  *
  *
  */
final class BooleanAsResultOps(private[this] val b: Boolean) {

  def cond[T](correct: => T, anomaly: => Anomaly): Result[T] = Result.cond(b, correct, anomaly)

  def failOnTrue(anomaly: => Anomaly): Result[Unit] = Result.failOnTrue(b, anomaly)

  def failOnFalse(anomaly: => Anomaly): Result[Unit] = Result.failOnFalse(b, anomaly)
}

/**
  *
  *
  */
final class ResultBooleanAsResultOps(private[this] val br: Result[Boolean]) {

  def cond[T](correct: => T, anomaly: => Anomaly): Result[T] = Result.flatCond(br, correct, anomaly)

  def failOnTrue(anomaly: => Anomaly): Result[Unit] = Result.flatFailOnTrue(br, anomaly)

  def failOnFalse(anomaly: => Anomaly): Result[Unit] = Result.flatFailOnFalse(br, anomaly)
}
