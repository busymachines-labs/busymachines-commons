package busymachines.semver

import busymachines.effects.sync._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 13 Nov 2017
  *
  */
object syntax {

  implicit class SemanticVersionCompanionOps(doNotCare: SemanticVersion.type) {

    def fromString(semVer: String): Result[SemanticVersion] =
      SemanticVersionParsers.parseSemanticVersion(semVer)

    def unsafeFromString(semVer: String): SemanticVersion =
      SemanticVersionParsers.unsafeParseSemanticVersion(semVer)
  }

  implicit class SemanticVersionLabelCompanionOps(doNotCare: Labels.type) {

    def fromString(semVer: String): Result[Label] =
      SemanticVersionParsers.parseLabel(semVer)

    def unsafeFromString(label: String): Label =
      SemanticVersionParsers.unsafeParseLabel(label)
  }

}
