package busymachines.effects.sync.validated

import busymachines.effects.sync._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 27 Feb 2018
  *
  */
object ResultSyntaxCats {

  trait Implicits {
    implicit def bmcResultCatsReferenceOps[T](value: Result[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit def bmcResultCatsCompanionObjectOpsOps(value: Result.type): CompanionObjectOps =
      new CompanionObjectOps(value)
  }

  final class ReferenceOps[T](val value: Result[T]) extends AnyVal {
    @inline def asValidated: Validated[T] = ValidatedOps.fromResult(value)
  }

  final class CompanionObjectOps(val ResultObj: Result.type) extends AnyVal {
    @inline def asValidated[T](value: Result[T]): Validated[T] = ValidatedOps.fromResult(value)
  }
}
