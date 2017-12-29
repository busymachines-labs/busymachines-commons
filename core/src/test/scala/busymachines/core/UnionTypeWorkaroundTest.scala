package busymachines.core

import org.scalatest.FlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 31 Jul 2017
  *
  */
class UnionTypeWorkaroundTest extends FlatSpec {

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
