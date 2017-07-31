package com.busymachines.core.exceptions

import com.busymachines.core.exceptions.FailureMessage.Parameters
import org.scalatest.FlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 31 Jul 2017
  *
  */
class UnionTypeWorkaroundTest extends FlatSpec {

  behavior of "Failures"

  it should "... apply implicit conversions as a workaround to union types" in {
    assertCompiles {
      """
        |
        |object RevolutionaryDomainFailures {
        |
        |  case object CannotBeDone extends FailureID {
        |    val name = "rd_001"
        |  }
        |
        |}
        |
        |case class SolutionNotFoundFailure(problem: String, attempts: Seq[String]) extends NotFoundFailure(
        |  s"Solution to problem $problem not found."
        |) {
        |  override def id: FailureID = RevolutionaryDomainFailures.CannotBeDone
        |
        |  override def parameters: Parameters = Map(
        |    "problem" -> problem,
        |    "attempts" -> attempts
        |  )
        |}
        |
        |object JustSomeScopeWithStuffToSolve {
        |  //...
        |  val solutionToPVSNP: Option[Boolean] = ???
        |  solutionToPVSNP.getOrElse(throw SolutionNotFoundFailure("P vs. NP", Seq("1", "2", "3")))
        |
        |  val solutionToHaltingProblem: Option[Boolean] = ???
        |  solutionToHaltingProblem.getOrElse(throw SolutionNotFoundFailure("Halting Problem", Seq("stop", "12")))
        |}
        |
  """.stripMargin
    }
  }
}


