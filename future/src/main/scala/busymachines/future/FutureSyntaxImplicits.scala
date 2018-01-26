package busymachines.future

import busymachines.result.Result

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 25 Jan 2018
  *
  */
trait FutureSyntaxImplicits {

  implicit def bmCommonsUnsafeFutureOps[T](f: Future[T]): UnsafeFutureOps[T] =
    new UnsafeFutureOps[T](f)

  implicit def bmCommonsFutureCompanionOps(f: Future.type): CompanionFutureOps.type =
    CompanionFutureOps

  implicit def bmCommonsOptionToFutureOps[T](opt: Option[T]) =
    new OptionToFutureOps(opt)

  implicit def bmCommonsFutureOptionToFutureOps[T](fopt: Future[Option[T]]) =
    new FutureOptionToFutureOps(fopt)

  implicit def bmCommonsEitherToFutureOps[L, R](eit: Either[L, R]) =
    new EitherToFutureOps(eit)

  implicit def bmCommonsBooleanToFutureOps(b: Boolean) =
    new BooleanToFutureOps(b)

  implicit def bmCommonsFutureBooleanToFutureOps(fb: Future[Boolean]) =
    new FutureBooleanToFutureOps(fb)
}
