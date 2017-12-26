package busymachines.core

import busymachines.core.Anomaly.Parameters

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Dec 2017
  *
  */
trait DeniedAnomaly extends Anomaly with MeaningfulAnomalies.Denied with Product with Serializable

object DeniedAnomaly extends AnomalyConstructors[DeniedAnomaly] {
  override def apply(id: AnomalyID): DeniedAnomaly = DeniedAnomalyImpl(id = id)

  override def apply(message: String): DeniedAnomaly = DeniedAnomalyImpl(message = message)

  override def apply(parameters: Parameters): DeniedAnomaly = DeniedAnomalyImpl(parameters = parameters)

  override def apply(id: AnomalyID, message: String): DeniedAnomaly =
    DeniedAnomalyImpl(id = id, message = message)

  override def apply(id: AnomalyID, parameters: Parameters): DeniedAnomaly =
    DeniedAnomalyImpl(id = id, parameters = parameters)

  override def apply(message: String, parameters: Parameters): DeniedAnomaly =
    DeniedAnomalyImpl(message = message, parameters = parameters)

  override def apply(id: AnomalyID, message: String, parameters: Parameters): DeniedAnomaly =
    DeniedAnomalyImpl(id = id, message = message, parameters = parameters)

  override def apply(a: Anomaly): DeniedAnomaly =
    DeniedAnomalyImpl(id = a.id, message = a.message, parameters = a.parameters)
}

private[core] final case class DeniedAnomalyImpl(
  override val id:         AnomalyID  = DeniedAnomalyID,
  override val message:    String     = MeaningfulAnomalies.`Denied`,
  override val parameters: Parameters = Parameters.empty
) extends DeniedAnomaly with Product with Serializable {

  override def asThrowable: Throwable = DeniedFailureImpl(id, message, parameters)
}

//=============================================================================
//=============================================================================
//=============================================================================

abstract class DeniedFailure(
  override val message: String,
  causedBy:             Option[Throwable] = None,
) extends AnomalousFailure(message, causedBy) with DeniedAnomaly with Product with Serializable {
  override def id: AnomalyID = DeniedAnomalyID
}

object DeniedFailure extends FailureConstructors[DeniedFailure] {
  override def apply(causedBy: Throwable): DeniedFailure = DeniedFailureImpl(causedBy = Option(causedBy))

  override def apply(id: AnomalyID, message: String, causedBy: Throwable): DeniedFailure =
    DeniedFailureImpl(id = id, message = message, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, parameters: Parameters, causedBy: Throwable): DeniedFailure =
    DeniedFailureImpl(id = id, parameters = parameters, causedBy = Option(causedBy))

  override def apply(message: String, parameters: Parameters, causedBy: Throwable): DeniedFailure =
    DeniedFailureImpl(message = message, parameters = parameters, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, message: String, parameters: Parameters, causedBy: Throwable): DeniedFailure =
    DeniedFailureImpl(id = id, message = message, parameters = parameters, causedBy = Option(causedBy))

  override def apply(a: Anomaly, causedBy: Throwable): DeniedFailure =
    DeniedFailureImpl(id = a.id, message = a.message, parameters = a.parameters, causedBy = Option(causedBy))

  override def apply(id: AnomalyID): DeniedFailure =
    DeniedFailureImpl(id = id)

  override def apply(message: String): DeniedFailure =
    DeniedFailureImpl(message = message)

  override def apply(parameters: Parameters): DeniedFailure =
    DeniedFailureImpl(parameters = parameters)

  override def apply(id: AnomalyID, message: String): DeniedFailure =
    DeniedFailureImpl(id = id, message = message)

  override def apply(id: AnomalyID, parameters: Parameters): DeniedFailure =
    DeniedFailureImpl(id = id, parameters = parameters)

  override def apply(message: String, parameters: Parameters): DeniedFailure =
    DeniedFailureImpl(message = message, parameters = parameters)

  override def apply(id: AnomalyID, message: String, parameters: Parameters): DeniedFailure =
    DeniedFailureImpl(id = id, message = message, parameters = parameters)

  override def apply(a: Anomaly): DeniedFailure =
    DeniedFailureImpl(id = a.id, message = a.message, parameters = a.parameters)
}

private[core] final case class DeniedFailureImpl(
  override val id:         AnomalyID         = DeniedAnomalyID,
  override val message:    String            = MeaningfulAnomalies.`Denied`,
  override val parameters: Parameters        = Parameters.empty,
  causedBy:                Option[Throwable] = None,
) extends DeniedFailure(message, causedBy) with Product with Serializable
