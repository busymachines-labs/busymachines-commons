package busymachines.effects.sync.validation_test

import busymachines.core._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Feb 2018
  *
  */
private[validation_test] case class TestValidationFailures(
  bad:  Anomaly,
  bads: List[Anomaly] = Nil,
) extends AnomalousFailures(
      TVFsID,
      s"Test validation failed with ${bads.length + 1} anomalies",
      bad,
      bads,
    )

private[validation_test] case object TVFsID extends AnomalyID {
  override def name = "test_validation_001"
}
