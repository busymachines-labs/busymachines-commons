package busymachines.core

import busymachines.core.Anomaly.Parameters

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Dec 2017
  *
  */
trait NotFoundAnomaly extends Anomaly with MeaningfulAnomalies.NotFound with Product with Serializable

object NotFoundAnomaly extends AnomalyConstructors[NotFoundAnomaly] {
  override def apply(id: AnomalyID): NotFoundAnomaly = NotFoundAnomalyImpl(id = id)

  override def apply(message: String): NotFoundAnomaly = NotFoundAnomalyImpl(message = message)

  override def apply(parameters: Parameters): NotFoundAnomaly = NotFoundAnomalyImpl(parameters = parameters)

  override def apply(id: AnomalyID, message: String): NotFoundAnomaly =
    NotFoundAnomalyImpl(id = id, message = message)

  override def apply(id: AnomalyID, parameters: Parameters): NotFoundAnomaly =
    NotFoundAnomalyImpl(id = id, parameters = parameters)

  override def apply(message: String, parameters: Parameters): NotFoundAnomaly =
    NotFoundAnomalyImpl(message = message, parameters = parameters)

  override def apply(id: AnomalyID, message: String, parameters: Parameters): NotFoundAnomaly =
    NotFoundAnomalyImpl(id = id, message = message, parameters = parameters)

  override def apply(a: Anomaly): NotFoundAnomaly =
    NotFoundAnomalyImpl(id = a.id, message = a.message, parameters = a.parameters)
}

private[core] final case class NotFoundAnomalyImpl(
  override val id:         AnomalyID  = NotFoundAnomalyID,
  override val message:    String     = MeaningfulAnomalies.`Not found`,
  override val parameters: Parameters = Parameters.empty
) extends NotFoundAnomaly {

  override def asThrowable: Throwable = NotFoundFailureImpl(id, message, parameters)
}

//=============================================================================
//=============================================================================
//=============================================================================

abstract class NotFoundFailure(
  override val message: String,
  causedBy:             Option[Throwable] = None,
) extends AnomalousFailure(message, causedBy) with NotFoundAnomaly {
  override def id: AnomalyID = NotFoundAnomalyID
}

object NotFoundFailure extends FailureConstructors[NotFoundFailure] {
  override def apply(causedBy: Throwable): NotFoundFailure = NotFoundFailureImpl(causedBy = Option(causedBy))

  override def apply(id: AnomalyID, message: String, causedBy: Throwable): NotFoundFailure =
    NotFoundFailureImpl(id = id, message = message, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, parameters: Parameters, causedBy: Throwable): NotFoundFailure =
    NotFoundFailureImpl(id = id, parameters = parameters, causedBy = Option(causedBy))

  override def apply(message: String, parameters: Parameters, causedBy: Throwable): NotFoundFailure =
    NotFoundFailureImpl(message = message, parameters = parameters, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, message: String, parameters: Parameters, causedBy: Throwable): NotFoundFailure =
    NotFoundFailureImpl(id = id, message = message, parameters = parameters, causedBy = Option(causedBy))

  override def apply(a: Anomaly, causedBy: Throwable): NotFoundFailure =
    NotFoundFailureImpl(id = a.id, message = a.message, parameters = a.parameters, causedBy = Option(causedBy))

  override def apply(id: AnomalyID): NotFoundFailure =
    NotFoundFailureImpl(id = id)

  override def apply(message: String): NotFoundFailure =
    NotFoundFailureImpl(message = message)

  override def apply(parameters: Parameters): NotFoundFailure =
    NotFoundFailureImpl(parameters = parameters)

  override def apply(id: AnomalyID, message: String): NotFoundFailure =
    NotFoundFailureImpl(id = id, message = message)

  override def apply(id: AnomalyID, parameters: Parameters): NotFoundFailure =
    NotFoundFailureImpl(id = id, parameters = parameters)

  override def apply(message: String, parameters: Parameters): NotFoundFailure =
    NotFoundFailureImpl(message = message, parameters = parameters)

  override def apply(id: AnomalyID, message: String, parameters: Parameters): NotFoundFailure =
    NotFoundFailureImpl(id = id, message = message, parameters = parameters)

  override def apply(a: Anomaly): NotFoundFailure =
    NotFoundFailureImpl(id = a.id, message = a.message, parameters = a.parameters)
}

private[core] final case class NotFoundFailureImpl(
  override val id:         AnomalyID         = NotFoundAnomalyID,
  override val message:    String            = MeaningfulAnomalies.`Not found`,
  override val parameters: Parameters        = Parameters.empty,
  causedBy:                Option[Throwable] = None,
) extends NotFoundFailure(message, causedBy)
