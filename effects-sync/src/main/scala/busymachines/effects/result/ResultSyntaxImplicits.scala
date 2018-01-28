package busymachines.effects.result

import scala.util.Try

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 25 Jan 2018
  *
  */
trait ResultSyntaxImplicits {

  implicit def bmCommonsResultOps[T](t: Result[T]): ResultOps[T] =
    new ResultOps(t)

  implicit def bmCommonsOptionAsResultOps[T](opt: Option[T]): OptionAsResultOps[T] =
    new OptionAsResultOps(opt)

  implicit def bmCommonsResultOptionAsResultOps[T](ropt: Result[Option[T]]): ResultOptionAsResultOps[T] =
    new ResultOptionAsResultOps(ropt)

  implicit def bmCommonsTryAsResultOps[T](tr: Try[T]): TryAsResultOps[T] =
    new TryAsResultOps(tr)

  implicit def bmCommonsEitherAsResultOps[L, R](eit: Either[L, R]): EitherAsResultOps[L, R] =
    new EitherAsResultOps(eit)

  implicit def bmCommonsBooleanAsResultOps(b: Boolean): BooleanAsResultOps =
    new BooleanAsResultOps(b)
}
