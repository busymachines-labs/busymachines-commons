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
package busymachines.core

import org.scalatest.flatspec.AnyFlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 31 Jul 2017
  *
  */
class UnionTypeWorkaroundTest extends AnyFlatSpec {

  behavior of "Anomalies"

  it should "... apply implicit conversions as a workaround to union types" in {
    assertCompiles {
      """
        |
        |import busymachines.core._
        |
        |object RevolutionaryDomainFailures {
        |
        |  case object CannotBeDone extends AnomalyID {
        |    val name = "rd_001"
        |  }
        |
        |}
        |
        |case class SolutionNotFoundFailure(problem: String, attempts: List[String]) extends NotFoundFailure(
        |  s"Solution to problem $problem not found."
        |) {
        |  override def id: AnomalyID = RevolutionaryDomainFailures.CannotBeDone
        |
        |  override def parameters: Anomaly.Parameters = Anomaly.Parameters(
        |    "problem" -> problem,
        |    "attempts" -> attempts
        |  )
        |}
        |
        |object JustSomeScopeWithStuffToSolve {
        |  //...
        |  val solutionToPVSNP: Option[Boolean] = ???
        |  solutionToPVSNP.getOrElse(throw SolutionNotFoundFailure("P vs. NP", List("1", "2", "3")))
        |
        |  val solutionToHaltingProblem: Option[Boolean] = ???
        |  solutionToHaltingProblem.getOrElse(throw SolutionNotFoundFailure("Halting Problem", List("stop", "12")))
        |}
        |
  """.stripMargin
    }
  }
}
