package busymachines.core
import busymachines.core.Anomaly.Parameters

/**
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Dec 2017
  *
  */
trait Catastrophe extends Anomaly with Product with Serializable

/**
  * [[java.lang.Error]] is a reasonable choice for a super-class.
  * It's not caught by NonFatal(_)pattern matches. Which means that
  * we can properly propagate "irrecoverable errors" one a per "request"
  * basis and at the same time not crash our application into oblivion.
  */
abstract class CatastrophicError(
  override val message: String,
  causedBy:             Option[Throwable] = None
) extends Error(message, causedBy.orNull) with Catastrophe with Product with Serializable {
  final override def asThrowable: Throwable = this
}

object CatastrophicError extends CatastrophicErrorConstructors[CatastrophicError] {
  private[core] val `Catastrophic Error` = "Catastrophic Error"

  override def apply(causedBy: Throwable): CatastrophicError = CatastrophicErrorImpl(causedBy = Option(causedBy))

  override def apply(id: AnomalyID, message: String, causedBy: Throwable): CatastrophicError =
    CatastrophicErrorImpl(id = id, message = message, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, parameters: Parameters, causedBy: Throwable): CatastrophicError =
    CatastrophicErrorImpl(id = id, parameters = parameters, causedBy = Option(causedBy))

  override def apply(message: String, parameters: Parameters, causedBy: Throwable): CatastrophicError =
    CatastrophicErrorImpl(message = message, parameters = parameters, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, message: String, parameters: Parameters, causedBy: Throwable): CatastrophicError =
    CatastrophicErrorImpl(id = id, message = message, parameters = parameters, causedBy = Option(causedBy))

  override def apply(a: Anomaly, causedBy: Throwable): CatastrophicError =
    CatastrophicErrorImpl(id = a.id, message = a.message, parameters = a.parameters, causedBy = Option(causedBy))

  override def apply(id: AnomalyID): CatastrophicError =
    CatastrophicErrorImpl(id = id)

  override def apply(message: String): CatastrophicError =
    CatastrophicErrorImpl(message = message)

  override def apply(parameters: Parameters): CatastrophicError =
    CatastrophicErrorImpl(parameters = parameters)

  override def apply(id: AnomalyID, message: String): CatastrophicError =
    CatastrophicErrorImpl(id = id, message = message)

  override def apply(id: AnomalyID, parameters: Parameters): CatastrophicError =
    CatastrophicErrorImpl(id = id, parameters = parameters)

  override def apply(message: String, parameters: Parameters): CatastrophicError =
    CatastrophicErrorImpl(message = message, parameters = parameters)

  override def apply(id: AnomalyID, message: String, parameters: Parameters): CatastrophicError =
    CatastrophicErrorImpl(id = id, message = message, parameters = parameters)

  override def apply(a: Anomaly): CatastrophicError =
    CatastrophicErrorImpl(id = a.id, message = a.message, parameters = a.parameters, causedBy = Option(a.asThrowable))

  override def apply(message: String, causedBy: Throwable): CatastrophicError =
    CatastrophicErrorImpl(message = message, causedBy = Option(causedBy))
}

private[core] case object CatastrophicErrorID extends AnomalyID with Product with Serializable {
  override val name: String = "CE_0"
}

private[core] final case class CatastrophicErrorImpl(
  override val id:         AnomalyID          = CatastrophicErrorID,
  override val message:    String             = CatastrophicError.`Catastrophic Error`,
  override val parameters: Anomaly.Parameters = Anomaly.Parameters.empty,
  causedBy:                Option[Throwable]  = None
) extends CatastrophicError(message, causedBy = causedBy)

//=============================================================================
//=============================================================================
//=============================================================================

trait InconsistentStateCatastrophe extends Catastrophe

private[core] case object InconsistentStateCatastropheID extends AnomalyID with Product with Serializable {
  override val name: String = "IS_0"
}

object InconsistentStateError extends CatastrophicErrorConstructors[InconsistentStateError] {
  private[core] val InconsistentStateError: String = "Inconsistent State Error"

  override def apply(causedBy: Throwable): InconsistentStateError =
    InconsistentStateErrorImpl(causedBy = Option(causedBy))

  override def apply(id: AnomalyID, message: String, causedBy: Throwable): InconsistentStateError =
    InconsistentStateErrorImpl(id = id, message = message, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, parameters: Parameters, causedBy: Throwable): InconsistentStateError =
    InconsistentStateErrorImpl(id = id, parameters = parameters, causedBy = Option(causedBy))

  override def apply(message: String, parameters: Parameters, causedBy: Throwable): InconsistentStateError =
    InconsistentStateErrorImpl(message = message, parameters = parameters, causedBy = Option(causedBy))

  override def apply(
    id:         AnomalyID,
    message:    String,
    parameters: Parameters,
    causedBy:   Throwable
  ): InconsistentStateError =
    InconsistentStateErrorImpl(id = id, message = message, parameters = parameters, causedBy = Option(causedBy))

  override def apply(a: Anomaly, causedBy: Throwable): InconsistentStateError =
    InconsistentStateErrorImpl(id = a.id, message = a.message, parameters = a.parameters, causedBy = Option(causedBy))

  override def apply(id: AnomalyID): InconsistentStateError =
    InconsistentStateErrorImpl(id = id)

  override def apply(message: String): InconsistentStateError =
    InconsistentStateErrorImpl(message = message)

  override def apply(parameters: Parameters): InconsistentStateError =
    InconsistentStateErrorImpl(parameters = parameters)

  override def apply(id: AnomalyID, message: String): InconsistentStateError =
    InconsistentStateErrorImpl(id = id, message = message)

  override def apply(id: AnomalyID, parameters: Parameters): InconsistentStateError =
    InconsistentStateErrorImpl(id = id, parameters = parameters)

  override def apply(message: String, parameters: Parameters): InconsistentStateError =
    InconsistentStateErrorImpl(message = message, parameters = parameters)

  override def apply(id: AnomalyID, message: String, parameters: Parameters): InconsistentStateError =
    InconsistentStateErrorImpl(id = id, message = message, parameters = parameters)

  override def apply(a: Anomaly): InconsistentStateError =
    InconsistentStateErrorImpl(
      id         = a.id,
      message    = a.message,
      parameters = a.parameters,
      causedBy   = Option(a.asThrowable)
    )

  override def apply(message: String, causedBy: Throwable): InconsistentStateError =
    InconsistentStateErrorImpl(message = message, causedBy = Option(causedBy))
}

abstract class InconsistentStateError(
  override val message: String,
  causedBy:             Option[Throwable] = None
) extends CatastrophicError(message, causedBy) with InconsistentStateCatastrophe with Product with Serializable

private[core] final case class InconsistentStateErrorImpl(
  override val id:         AnomalyID          = InconsistentStateCatastropheID,
  override val message:    String             = InconsistentStateError.InconsistentStateError,
  override val parameters: Anomaly.Parameters = Anomaly.Parameters.empty,
  causedBy:                Option[Throwable]  = None
) extends InconsistentStateError(message, causedBy = causedBy)
