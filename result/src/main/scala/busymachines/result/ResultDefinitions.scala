package busymachines.result

import busymachines.core._

import scala.util.Try

/**
  *
  * In case you're wondering why we use implicit defs which instantiate a wrapper
  * class instead of defining an implicit class directly, well, mostly to keep
  * this file a bit more readable. We could have done that just as easily:
  *
  * https://docs.scala-lang.org/overviews/core/implicit-classes.html
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 09 Jan 2018
  *
  */
trait ResultDefinitions {
  type Result[T]    = Either[Anomaly, T]
  type Correct[T]   = Right[Anomaly,  T]
  type Incorrect[T] = Left[Anomaly,   T]

  implicit def bmCommonsResultOps[T](t: Result[T]): ResultOps[T] =
    new ResultOps(t)

  implicit def bmCommonsSuspendedResultOps[T](t: => Result[T]): SuspendedResultOps[T] =
    new SuspendedResultOps(t)

  implicit def bmCommonsResultFromOptionOps[T](opt: Option[T]): OptionToResultOps[T] =
    new OptionToResultOps(opt)

  implicit def bmCommonsResultFromTryOps[T](tr: Try[T]): TryToResultOps[T] =
    new TryToResultOps(tr)

  implicit def bmCommonsResultFromEitherOps[L, R](eit: Either[L, R]): EitherToResultOps[L, R] =
    new EitherToResultOps(eit)

  implicit def bmCommonsResultFromBooleanOps(b: Boolean): BooleanToResultOps =
    new BooleanToResultOps(b)
}
