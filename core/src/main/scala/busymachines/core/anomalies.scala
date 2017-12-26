package busymachines.core

import scala.collection.immutable

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Dec 2017
  *
  */
trait Anomalies extends Anomaly with Product with Serializable {
  def firstAnomaly: Anomaly

  def restOfAnomalies: immutable.Seq[Anomaly]

  final def messages: immutable.Seq[Anomaly] =
    firstAnomaly +: restOfAnomalies
}

object Anomalies {

  def apply(id: AnomalyID, message: String, msg: Anomaly, msgs: Anomaly*): Anomalies = {
    AnomaliesImpl(id, message, msg, msgs.toList)
  }
}

private[core] final case class AnomaliesImpl(
  override val id:              AnomalyID,
  override val message:         String,
  override val firstAnomaly:    Anomaly,
  override val restOfAnomalies: immutable.Seq[Anomaly],
) extends Anomalies {

  override def asThrowable: Throwable = AnomalousFailuresImpl(
    id,
    message,
    firstAnomaly,
    restOfAnomalies
  )
}

abstract class AnomalousFailures(
  override val id:              AnomalyID,
  override val message:         String,
  override val firstAnomaly:    Anomaly,
  override val restOfAnomalies: immutable.Seq[Anomaly],
) extends AnomalousFailure(message) with Anomalies with Product with Serializable

private[core] case class AnomalousFailuresImpl(
  override val id:              AnomalyID,
  override val message:         String,
  override val firstAnomaly:    Anomaly,
  override val restOfAnomalies: immutable.Seq[Anomaly],
) extends AnomalousFailures(id, message, firstAnomaly, restOfAnomalies) with Product with Serializable
