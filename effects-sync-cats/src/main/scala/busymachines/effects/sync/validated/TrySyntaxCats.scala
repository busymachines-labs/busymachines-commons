package busymachines.effects.sync.validated

import busymachines.effects.sync._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 27 Feb 2018
  *
  */
object TrySyntaxCats {

  trait Implicits {
    implicit final def bmcTryCatsReferenceOps[T](value: Try[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit final def bmcTryCatsCompanionObjectOpsOps(value: scala.util.Try.type): CompanionObjectOps =
      new CompanionObjectOps(value)
  }

  final class ReferenceOps[T](val value: Try[T]) extends AnyVal {
    @inline def asValidated: Validated[T] = ValidatedOps.fromTry(value)
  }

  final class CompanionObjectOps(val tryObj: scala.util.Try.type) extends AnyVal {
    @inline def asValidated[T](value: Try[T]): Validated[T] = ValidatedOps.fromTry(value)
  }
}
