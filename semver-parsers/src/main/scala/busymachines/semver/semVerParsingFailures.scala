package busymachines.semver

import busymachines.core.exceptions.FailureMessage.Parameters
import busymachines.core.exceptions._

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
  override def id: FailureID = ParsingFailureIDs.InvalidSemanticVersion

  override val parameters: Parameters = Parameters(
    "input"      -> input,
    "parseError" -> parseError
  )
}

final case class InvalidSemanticVersionLabelFailure(input: String, parseError: String)
    extends InvalidSemanticVersionParsingFailure(
      s"Failed to parse label of semantic version '$input' because: $parseError"
    ) {
  override def id: FailureID = ParsingFailureIDs.InvalidSemanticVersionLabel

  override val parameters: Parameters = Parameters(
    "input"      -> input,
    "parseError" -> parseError
  )
}

object ParsingFailureIDs {

  case object InvalidSemanticVersion extends FailureID {
    override def name: String = "BMC_P_1"
  }

  case object InvalidSemanticVersionLabel extends FailureID {
    override def name: String = "BMC_P_2"
  }

}
