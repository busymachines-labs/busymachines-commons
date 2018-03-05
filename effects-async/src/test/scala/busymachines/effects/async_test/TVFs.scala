package busymachines.effects.async_test

import busymachines.core._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Feb 2018
  *
  */
private[async_test] case class TVFs(
  bad:  Anomaly,
  bads: List[Anomaly] = Nil
) extends AnomalousFailures(
      TVFsID,
      s"Test validation failed with ${bads.length + 1} anomalies",
      bad,
      bads
    )

private[async_test] case object TVFsID extends AnomalyID {
  override def name = "test_async_validation"
}
