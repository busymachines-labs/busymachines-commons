/**
  * Copyright (c) 2017-2018 BusyMachines
  *
  * See company homepage at: https://www.busymachines.com/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
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
