package busymachines.core

/**
  *
  * Used to signal that an API is not yet stable, and provides a version until
  * it is expected to be stable.
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 07 Sep 2017
  *
  */
case class SubjectToChange(until: String) extends scala.annotation.StaticAnnotation
