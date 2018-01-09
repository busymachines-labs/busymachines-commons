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
