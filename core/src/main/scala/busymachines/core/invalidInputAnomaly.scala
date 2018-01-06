package busymachines.core

import busymachines.core.Anomaly.Parameters

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Dec 2017
  *
  */
trait InvalidInputAnomaly extends Anomaly with MeaningfulAnomalies.InvalidInput with Product with Serializable

object InvalidInputAnomaly extends AnomalyConstructors[InvalidInputAnomaly] {
  override def apply(id: AnomalyID): InvalidInputAnomaly = InvalidInputAnomalyImpl(id = id)

  override def apply(message: String): InvalidInputAnomaly = InvalidInputAnomalyImpl(message = message)

  override def apply(parameters: Parameters): InvalidInputAnomaly = InvalidInputAnomalyImpl(parameters = parameters)

  override def apply(id: AnomalyID, message: String): InvalidInputAnomaly =
    InvalidInputAnomalyImpl(id = id, message = message)

  override def apply(id: AnomalyID, parameters: Parameters): InvalidInputAnomaly =
    InvalidInputAnomalyImpl(id = id, parameters = parameters)

  override def apply(message: String, parameters: Parameters): InvalidInputAnomaly =
    InvalidInputAnomalyImpl(message = message, parameters = parameters)

  override def apply(id: AnomalyID, message: String, parameters: Parameters): InvalidInputAnomaly =
    InvalidInputAnomalyImpl(id = id, message = message, parameters = parameters)

  override def apply(a: Anomaly): InvalidInputAnomaly =
    InvalidInputAnomalyImpl(id = a.id, message = a.message, parameters = a.parameters)
}

private[core] final case class InvalidInputAnomalyImpl(
  override val id:         AnomalyID  = InvalidInputAnomalyID,
  override val message:    String     = MeaningfulAnomalies.InvalidInputMsg,
  override val parameters: Parameters = Parameters.empty
) extends InvalidInputAnomaly with Product with Serializable {

  override def asThrowable: Throwable = InvalidInputFailureImpl(id, message, parameters)
}

//=============================================================================
//=============================================================================
//=============================================================================

abstract class InvalidInputFailure(
  override val message: String,
  causedBy:             Option[Throwable] = None,
) extends AnomalousFailure(message, causedBy) with InvalidInputAnomaly with Product with Serializable {
  override def id: AnomalyID = InvalidInputAnomalyID
}

object InvalidInputFailure
    extends InvalidInputFailure(MeaningfulAnomalies.InvalidInputMsg, None) with SingletonAnomalyProduct with
      FailureConstructors[InvalidInputFailure] {
  override def apply(causedBy: Throwable): InvalidInputFailure = InvalidInputFailureImpl(causedBy = Option(causedBy))

  override def apply(id: AnomalyID, message: String, causedBy: Throwable): InvalidInputFailure =
    InvalidInputFailureImpl(id = id, message = message, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, parameters: Parameters, causedBy: Throwable): InvalidInputFailure =
    InvalidInputFailureImpl(id = id, parameters = parameters, causedBy = Option(causedBy))

  override def apply(message: String, parameters: Parameters, causedBy: Throwable): InvalidInputFailure =
    InvalidInputFailureImpl(message = message, parameters = parameters, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, message: String, parameters: Parameters, causedBy: Throwable): InvalidInputFailure =
    InvalidInputFailureImpl(id = id, message = message, parameters = parameters, causedBy = Option(causedBy))

  override def apply(a: Anomaly, causedBy: Throwable): InvalidInputFailure =
    InvalidInputFailureImpl(id = a.id, message = a.message, parameters = a.parameters, causedBy = Option(causedBy))

  override def apply(id: AnomalyID): InvalidInputFailure =
    InvalidInputFailureImpl(id = id)

  override def apply(message: String): InvalidInputFailure =
    InvalidInputFailureImpl(message = message)

  override def apply(parameters: Parameters): InvalidInputFailure =
    InvalidInputFailureImpl(parameters = parameters)

  override def apply(id: AnomalyID, message: String): InvalidInputFailure =
    InvalidInputFailureImpl(id = id, message = message)

  override def apply(id: AnomalyID, parameters: Parameters): InvalidInputFailure =
    InvalidInputFailureImpl(id = id, parameters = parameters)

  override def apply(message: String, parameters: Parameters): InvalidInputFailure =
    InvalidInputFailureImpl(message = message, parameters = parameters)

  override def apply(id: AnomalyID, message: String, parameters: Parameters): InvalidInputFailure =
    InvalidInputFailureImpl(id = id, message = message, parameters = parameters)

  override def apply(a: Anomaly): InvalidInputFailure =
    InvalidInputFailureImpl(id = a.id, message = a.message, parameters = a.parameters)

  override def apply(message: String, causedBy: Throwable): InvalidInputFailure =
    InvalidInputFailureImpl(message = message, causedBy = Option(causedBy))
}

private[core] final case class InvalidInputFailureImpl(
  override val id:         AnomalyID         = InvalidInputAnomalyID,
  override val message:    String            = MeaningfulAnomalies.InvalidInputMsg,
  override val parameters: Parameters        = Parameters.empty,
  causedBy:                Option[Throwable] = None,
) extends InvalidInputFailure(message, causedBy) with Product with Serializable
