package busymachines.semver

import busymachines.core._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 13 Nov 2017
  *
  */
sealed abstract class InvalidSemanticVersionParsingFailure(m: String) extends InvalidInputFailure(m)

final case class InvalidSemanticVersionFailure(input: String, parseError: String)
    extends InvalidSemanticVersionParsingFailure(
      s"Failed to parse semantic version '$input' because: $parseError"
    ) {
  override def id: AnomalyID = ParsingAnomalyIDs.InvalidSemanticVersion

  override val parameters: Anomaly.Parameters = Anomaly.Parameters(
    "input"      -> input,
    "parseError" -> parseError
  )
}

final case class InvalidSemanticVersionLabelFailure(input: String, parseError: String)
    extends InvalidSemanticVersionParsingFailure(
      s"Failed to parse label of semantic version '$input' because: $parseError"
    ) {
  override def id: AnomalyID = ParsingAnomalyIDs.InvalidSemanticVersionLabel

  override val parameters: Anomaly.Parameters = Anomaly.Parameters(
    "input"      -> input,
    "parseError" -> parseError
  )
}

object ParsingAnomalyIDs {

  case object InvalidSemanticVersion extends AnomalyID {
    override def name: String = "BMC_P_1"
  }

  case object InvalidSemanticVersionLabel extends AnomalyID {
    override def name: String = "BMC_P_2"
  }

}
