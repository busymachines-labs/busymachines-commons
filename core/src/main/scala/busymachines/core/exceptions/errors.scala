package busymachines.core.exceptions

/**
  * ``THERE'S EVERYTHING WRONG WITH ERRORS!``
  *
  * The more meaningful design decisions are the ones made in failures.scala
  * file detailing the definitions of various [[busymachines.core.exceptions.FailureMessage]]
  *
  * Since the types here are a minority, the design here emulates the one
  * from the other more commonly used file.
  */
@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
trait ErrorMessage extends FailureMessage

@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
abstract class Error(
  override val message: String,
  val cause:            Option[Throwable] = None
) extends Exception(message, cause.orNull) with FailureMessage with ErrorMessage

@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
object Error extends Error(SemanticErrors.Error,          None) {

  override def id: FailureID = SemanticErrors.GenericErrorID

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private final class ReifiedError(
    message: String,
    cause:   Option[Throwable] = None
  ) extends Error(message, cause) {
    override def id: FailureID = SemanticErrors.GenericErrorID
  }

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String): Error =
    new ReifiedError(msg)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String, cause: Throwable): Error =
    new ReifiedError(msg, Some(cause))

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(cause: Throwable): Error =
    new ReifiedError(cause.getMessage, Some(cause))
}

//=============================================================================
//=============================================================================
//=============================================================================

@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
object SemanticErrors {

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  trait InconsistentState

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private[exceptions] case object InconsistentStateID extends FailureID {
    override def name: String = "is_state"
  }

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private[exceptions] val `Inconsistent state` = "Inconsistent state error"

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private[exceptions] case object GenericErrorID extends FailureID {
    override def name: String = "error"
  }

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private[exceptions] val `Error` = "Error"
}

/**
  *
  * See [[SemanticErrors.InconsistentState]] for intended use.
  */
@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
abstract class InconsistentStateError(
  message: String,
  cause:   Option[Throwable] = None
) extends Error(message, cause) with SemanticErrors.InconsistentState

@scala.deprecated("Use the types from busymachines.core",                                         "0.2.0-RC8")
object InconsistentStateError extends InconsistentStateError(SemanticErrors.`Inconsistent state`, None) {

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  override def id: FailureID = SemanticErrors.InconsistentStateID

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private final class ReifiedInconsistentStateError(
    message: String,
    cause:   Option[Throwable] = None
  ) extends InconsistentStateError(message, cause) {
    override def id: FailureID = SemanticErrors.InconsistentStateID
  }

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String): InconsistentStateError =
    new ReifiedInconsistentStateError(msg)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String, cause: Throwable): InconsistentStateError =
    new ReifiedInconsistentStateError(msg, Some(cause))

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(cause: Throwable): InconsistentStateError =
    new ReifiedInconsistentStateError(cause.getMessage, Some(cause))
}
