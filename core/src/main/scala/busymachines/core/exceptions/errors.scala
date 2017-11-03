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

trait ErrorMessage extends FailureMessage

abstract class Error(
  override val message: String,
  val cause: Option[Throwable] = None
) extends Exception(message, cause.orNull) with FailureMessage with ErrorMessage


object Error extends Error(SemanticErrors.Error, None) {

  override def id: FailureID = SemanticErrors.GenericErrorID

  private final class ReifiedError(
    message: String,
    cause: Option[Throwable] = None
  ) extends Error(message, cause) {
    override def id: FailureID = SemanticErrors.GenericErrorID
  }

  def apply(msg: String): Error =
    new ReifiedError(msg)

  def apply(msg: String, cause: Throwable): Error =
    new ReifiedError(msg, Some(cause))

  def apply(cause: Throwable): Error =
    new ReifiedError(cause.getMessage, Some(cause))
}

//=============================================================================
//=============================================================================
//=============================================================================


object SemanticErrors {

  trait InconsistentState

  private[exceptions] case object InconsistentStateID extends FailureID {
    override def name: String = "is_state"
  }

  private[exceptions] val `Inconsistent state` = "Inconsistent state error"

  private[exceptions] case object GenericErrorID extends FailureID {
    override def name: String = "error"
  }

  private[exceptions] val `Error` = "Error"
}

/**
  *
  * See [[SemanticErrors.InconsistentState]] for intended use.
  */
abstract class InconsistentStateError(
  message: String,
  cause: Option[Throwable] = None
) extends Error(message, cause) with SemanticErrors.InconsistentState

object InconsistentStateError extends InconsistentStateError(SemanticErrors.`Inconsistent state`, None) {

  override def id: FailureID = SemanticErrors.InconsistentStateID

  private final class ReifiedInconsistentStateError(
    message: String,
    cause: Option[Throwable] = None
  ) extends InconsistentStateError(message, cause) {
    override def id: FailureID = SemanticErrors.InconsistentStateID
  }

  def apply(msg: String): InconsistentStateError =
    new ReifiedInconsistentStateError(msg)

  def apply(msg: String, cause: Throwable): InconsistentStateError =
    new ReifiedInconsistentStateError(msg, Some(cause))

  def apply(cause: Throwable): InconsistentStateError =
    new ReifiedInconsistentStateError(cause.getMessage, Some(cause))
}