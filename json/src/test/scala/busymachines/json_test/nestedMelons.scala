package busymachines.json_test

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Oct 2017
  *
  */
private[json_test] sealed trait OutdoorMelon

private[json_test] object OutdoorMelons {

  sealed trait Color

  object Colors {

    case object Green extends Color

  }

  case class WildMelon(
    weight: Int,
    color:  Color
  ) extends OutdoorMelon

}
