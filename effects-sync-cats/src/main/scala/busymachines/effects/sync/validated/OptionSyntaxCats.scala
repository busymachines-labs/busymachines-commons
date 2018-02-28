package busymachines.effects.sync.validated

import busymachines.core._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 27 Feb 2018
  *
  */
object OptionSyntaxCats {

  trait Implicits {
    implicit final def bmcOptionCatsReferenceOps[T](value: Option[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit final def bmcOptionCatsCompanionObjectOpsOps(value: Option.type): CompanionObjectOps =
      new CompanionObjectOps(value)
  }

  final class ReferenceOps[T](val value: Option[T]) extends AnyVal {
    @inline def asValidated(ifNone: => Anomaly): Validated[T] = ValidatedOps.fromOption(value, ifNone)
  }

  final class CompanionObjectOps(val OptionObj: Option.type) extends AnyVal {

    @inline def asValidated[T](value: Option[T], ifNone: => Anomaly): Validated[T] =
      ValidatedOps.fromOption(value, ifNone)
  }
}
