package busymachines.effects.sync.validated

import busymachines.core._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Feb 2018
  *
  */
case class GenericValidationFailures(
  bad:  Anomaly,
  bads: List[Anomaly] = Nil
) extends AnomalousFailures(
      GenericValidationFailuresID,
      s"Validation failed with ${bads.length + 1} anomalies",
      bad,
      bads
    )

case object GenericValidationFailuresID extends AnomalyID {
  override def name = "bmc_v_001"
}
