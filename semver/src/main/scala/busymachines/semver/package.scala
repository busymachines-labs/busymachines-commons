package busymachines

import scala.language.implicitConversions

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 13 Nov 2017
  *
  */
package object semver {

  /**
    * convenient aliases to [[SemanticVersion]]. A lot of people use these names
    * because of lazyness, might as well support some trivial type aliases
    * out of the box.
    */
  type SemVer = SemanticVersion
  val SemVer: SemanticVersion.type = SemanticVersion

  /**
    * In this particular case this is by no means a "dangerous" implicit,
    * since we just want to do automatic lifting into Option, because
    * it is the only way labels are used
    */
  implicit def labelToOptLabel(label: Label): Option[Label] = Option[Label](label)
}
