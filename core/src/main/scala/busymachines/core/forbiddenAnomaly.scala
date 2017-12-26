package busymachines.core

import busymachines.core.Anomaly.Parameters

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Dec 2017
  *
  */
trait ForbiddenAnomaly extends Anomaly with MeaningfulAnomalies.Forbidden with Product with Serializable

object ForbiddenAnomaly extends AnomalyConstructors[ForbiddenAnomaly] {
  override def apply(id: AnomalyID): ForbiddenAnomaly = ForbiddenAnomalyImpl(id = id)

  override def apply(message: String): ForbiddenAnomaly = ForbiddenAnomalyImpl(message = message)

  override def apply(parameters: Parameters): ForbiddenAnomaly = ForbiddenAnomalyImpl(parameters = parameters)

  override def apply(id: AnomalyID, message: String): ForbiddenAnomaly =
    ForbiddenAnomalyImpl(id = id, message = message)

  override def apply(id: AnomalyID, parameters: Parameters): ForbiddenAnomaly =
    ForbiddenAnomalyImpl(id = id, parameters = parameters)

  override def apply(message: String, parameters: Parameters): ForbiddenAnomaly =
    ForbiddenAnomalyImpl(message = message, parameters = parameters)

  override def apply(id: AnomalyID, message: String, parameters: Parameters): ForbiddenAnomaly =
    ForbiddenAnomalyImpl(id = id, message = message, parameters = parameters)

  override def apply(a: Anomaly): ForbiddenAnomaly =
    ForbiddenAnomalyImpl(id = a.id, message = a.message, parameters = a.parameters)
}

private[core] final case class ForbiddenAnomalyImpl(
  override val id:         AnomalyID  = ForbiddenAnomalyID,
  override val message:    String     = MeaningfulAnomalies.`Forbidden`,
  override val parameters: Parameters = Parameters.empty
) extends ForbiddenAnomaly with Product with Serializable {

  override def asThrowable: Throwable = ForbiddenFailureImpl(id, message, parameters)
}

//=============================================================================
//=============================================================================
//=============================================================================

abstract class ForbiddenFailure(
  override val message: String,
  causedBy:             Option[Throwable] = None,
) extends AnomalousFailure(message, causedBy) with ForbiddenAnomaly with Product with Serializable {
  override def id: AnomalyID = ForbiddenAnomalyID
}

object ForbiddenFailure extends FailureConstructors[ForbiddenFailure] {
  override def apply(causedBy: Throwable): ForbiddenFailure = ForbiddenFailureImpl(causedBy = Option(causedBy))

  override def apply(id: AnomalyID, message: String, causedBy: Throwable): ForbiddenFailure =
    ForbiddenFailureImpl(id = id, message = message, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, parameters: Parameters, causedBy: Throwable): ForbiddenFailure =
    ForbiddenFailureImpl(id = id, parameters = parameters, causedBy = Option(causedBy))

  override def apply(message: String, parameters: Parameters, causedBy: Throwable): ForbiddenFailure =
    ForbiddenFailureImpl(message = message, parameters = parameters, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, message: String, parameters: Parameters, causedBy: Throwable): ForbiddenFailure =
    ForbiddenFailureImpl(id = id, message = message, parameters = parameters, causedBy = Option(causedBy))

  override def apply(a: Anomaly, causedBy: Throwable): ForbiddenFailure =
    ForbiddenFailureImpl(id = a.id, message = a.message, parameters = a.parameters, causedBy = Option(causedBy))

  override def apply(id: AnomalyID): ForbiddenFailure =
    ForbiddenFailureImpl(id = id)

  override def apply(message: String): ForbiddenFailure =
    ForbiddenFailureImpl(message = message)

  override def apply(parameters: Parameters): ForbiddenFailure =
    ForbiddenFailureImpl(parameters = parameters)

  override def apply(id: AnomalyID, message: String): ForbiddenFailure =
    ForbiddenFailureImpl(id = id, message = message)

  override def apply(id: AnomalyID, parameters: Parameters): ForbiddenFailure =
    ForbiddenFailureImpl(id = id, parameters = parameters)

  override def apply(message: String, parameters: Parameters): ForbiddenFailure =
    ForbiddenFailureImpl(message = message, parameters = parameters)

  override def apply(id: AnomalyID, message: String, parameters: Parameters): ForbiddenFailure =
    ForbiddenFailureImpl(id = id, message = message, parameters = parameters)

  override def apply(a: Anomaly): ForbiddenFailure =
    ForbiddenFailureImpl(id = a.id, message = a.message, parameters = a.parameters)

  override def apply(message: String, causedBy: Throwable): ForbiddenFailure =
    ForbiddenFailureImpl(message = message, causedBy = Option(causedBy))
}

private[core] final case class ForbiddenFailureImpl(
  override val id:         AnomalyID         = ForbiddenAnomalyID,
  override val message:    String            = MeaningfulAnomalies.`Forbidden`,
  override val parameters: Parameters        = Parameters.empty,
  causedBy:                Option[Throwable] = None,
) extends ForbiddenFailure(message, causedBy) with Product with Serializable
