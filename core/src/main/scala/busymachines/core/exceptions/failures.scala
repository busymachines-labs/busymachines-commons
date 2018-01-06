package busymachines.core.exceptions

import scala.collection.immutable

/**
  * ``THERE'S NOTHING WRONG WITH FAILURE!``
  *
  * There, now that that got your attention, let me elaborate on the
  * intention of these types.
  *
  * Basically, they are a semantically richer way of expressing common
  * failures when developing backend-servers.
  *
  * Furthermore, these exceptions being part of the ``core`` of the application
  * —by reading this file— you have not gauged their full potentiality, yet.
  * The intention is to give richer interpretations to these "common failures"
  * in other ``busymachines-commons`` modules than could be done to the likes
  * of [[Throwable]].
  *
  * The reason why there is a trait [[FailureMessage]], and some types
  * that extend [[Exception]] is that the potentiality of these types can
  * be achieved either through a monad stack approach to building applications,
  * or to a more vanilla scala approach, respectively.
  *
  * There is a hierarchy of [[FailureMessage]] representing one single failure,
  * in richer details.
  *
  * [[FailureMessages]] represents a container of multiple [[FailureMessage]]s.
  * The intended use of the [[FailureMessages.id]] is to signal the general "context"
  * within which the specific [[FailureMessages.messages]] where gathered. While each
  * specific [[FailureMessage]] contains information about what went wrong.
  *
  * There are the following semantically meaningful exceptions (with their plural counterparts elided)
  * that you ought to be using:
  * - [[NotFoundFailure]]     -  [[SemanticFailures.NotFound]]
  * - [[UnauthorizedFailure]] -  [[SemanticFailures.Unauthorized]]
  * - [[ForbiddenFailure]]    -  [[SemanticFailures.Forbidden]]
  * - [[DeniedFailure]]       -  [[SemanticFailures.Denied]]
  * - [[InvalidInputFailure]] -  [[SemanticFailures.InvalidInput]]
  * - [[ConflictFailure]]     -  [[SemanticFailures.Conflict]]
  *
  * Each described in the [[SemanticFailures]] docs.
  *
  * These definitions are also quite flexible enough to allow multiple styles of active
  * development:
  *
  * 1) The quick and dirty, "better than RuntimeException" style. Basically,
  * you just wind up using the default constructors on the companion object,
  * or the companion object itself
  * {{{
  *   //I know I am in the middle of my domain problem, and I know that here
  *   //I can fail because "I did not find something", so I just quickly use:
  *
  *   option.getOrElse(throw NotFoundFailure)
  *   option.getOrElse(throw NotFoundFailure("this specific option, instead of generic"))
  *
  * }}}
  *
  * This style should be kept just that, "quick and dirty". After the first iteration
  * of the implementation these failures should be replaced by the ones in style 2)
  *
  * 2) The long term style. Where you subclass [[NotFoundFailure]] into a more meaningful
  * exception specific to your problem domain, supply some context via the parameters,
  * and assign it an (preferably) application-wide unique [[FailureID]].
  *
  * {{{
  *   object RevolutionaryDomainFailures {
  *     case object CannotBeDone extends FailureID { val name = "rd_001" }
  *     //... and many others
  *   }
  *
  *   case class SolutionNotFoundFailure(problem: String) extends NotFoundFailure(
  *     "Cannot find solution to problem:" + problem
  *   ) {
  *     override def id: FailureID = RevolutionaryDomainFailures.CannotBeDone
  *
  *     override def parameters: Parameters = Map(
  *       "problem" -> problem
  *     )
  *   }
  *
  *   object Main {
  *     //...
  *     solutionToPVSNP.getOrElse(throw SolutionNotFoundFailure("P vs. NP"))
  *     solutionToHaltingProblem.getOrElse(throw SolutionNotFoundFailure("Halting Problem"))
  *     //how refined you want your failures, that's up to you.
  *   }
  * }}}
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 31 Jul 2017
  *
  */
@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
trait FailureID {
  def name: String
}

@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
object FailureID {

  private case class GenericFailureID(override val name: String) extends FailureID

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(id: String): FailureID = GenericFailureID(id)
}

@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
object FailureMessage {

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  type ParamValue = StringOrSeqString

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  object ParamValue {

    @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
    def apply(s: String) = StringWrapper(s)

    @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
    def apply(ses: immutable.Seq[String]) = SeqStringWrapper(ses)
  }

  /**
    * This is a hack until dotty (scala 3.0) comes along with union types.
    * Until then, boiler plate freedom is given by the implicit
    * conversions found in the package object
    */
  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  sealed trait StringOrSeqString

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  final case class StringWrapper private (s: String) extends ParamValue

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  final case class SeqStringWrapper private (ses: immutable.Seq[String]) extends ParamValue

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  type Parameters = Map[String, ParamValue]

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  object Parameters {

    @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
    def apply(ps: (String, ParamValue)*): Parameters = Map.apply(ps: _*)

    @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
    def empty: Parameters = Map.empty[String, ParamValue]
  }

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private final case class GenericFailureMessage(
    override val id:         FailureID,
    override val message:    String,
    override val parameters: Parameters
  ) extends FailureMessage

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(
    id:         FailureID,
    message:    String,
    parameters: Parameters = FailureMessage.Parameters.empty
  ): FailureMessage = {
    GenericFailureMessage(id, message, parameters)
  }
}

@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
trait FailureMessage {

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def id: FailureID

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def message: String

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def parameters: FailureMessage.Parameters = FailureMessage.Parameters.empty
}

/**
  * [[FailureMessage]] counterpart, but for situations
  * when multiple such messages can occur.
  *
  * [[FailureMessages#id]], and [[FailureMessages#message]]
  * should be used to convey the general context withing which
  * [[FailureMessages#messages]] where gathered.
  *
  * Guaranteed to have non-empty FailureMessages
  */
@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
trait FailureMessages extends FailureMessage {

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def firstMessage: FailureMessage

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def restOfMessages: immutable.Seq[FailureMessage]

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  final def messages: immutable.Seq[FailureMessage] =
    firstMessage +: restOfMessages

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  final def hasNotFound: Boolean =
    messages.exists(_.isInstanceOf[NotFoundFailure])

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  final def hasUnauthorized: Boolean =
    messages.exists(_.isInstanceOf[UnauthorizedFailure])

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  final def hasForbidden: Boolean =
    messages.exists(_.isInstanceOf[ForbiddenFailure])

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  final def hasDenied: Boolean =
    messages.exists(_.isInstanceOf[DeniedFailure])

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  final def hasInvalidInput: Boolean =
    messages.exists(_.isInstanceOf[InvalidInputFailure])

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  final def hasConflict: Boolean =
    messages.exists(_.isInstanceOf[ConflictFailure])
}

@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
object FailureMessages {

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private final case class GenericFailureMessages(
    override val id:             FailureID,
    override val message:        String,
    override val firstMessage:   FailureMessage,
    override val restOfMessages: immutable.Seq[FailureMessage],
  ) extends FailureMessages

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(id: FailureID, message: String, msg: FailureMessage, msgs: FailureMessage*): FailureMessages = {
    GenericFailureMessages(id, message, msg, msgs.toList)
  }
}

/**
  * Should be extended sparingly outside of this file.
  *
  * Most likely you need to extend one of the other cases.
  */
@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
abstract class FailureBase(
  override val message:    String,
  val cause:               Option[Throwable] = None,
  override val parameters: FailureMessage.Parameters = FailureMessage.Parameters.empty
) extends Exception(message, cause.orNull) with FailureMessage

@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
object FailureBase {

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private final class ReifiedFailure(
    override val id:         FailureID,
    override val message:    String,
    override val parameters: FailureMessage.Parameters,
    cause:                   Option[Throwable] = None
  ) extends FailureBase(
        message    = message,
        cause      = cause,
        parameters = parameters
      )

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(fm: FailureMessage): FailureBase = {
    new ReifiedFailure(fm.id, fm.message, fm.parameters, None)
  }

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(fm: FailureMessage, cause: Throwable): FailureBase = {
    new ReifiedFailure(fm.id, fm.message, fm.parameters, Option(cause))
  }
}

/**
  * Similar to [[FailureBase]] but encapsulate multiple causes.
  *
  * Primarily used as containers for validation failures.
  */
@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
abstract class Failures(
  override val id:             FailureID,
  override val message:        String,
  override val firstMessage:   FailureMessage,
  override val restOfMessages: immutable.Seq[FailureMessage],
) extends Exception(message) with FailureMessages

@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
object Failures {

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private final class ReifiedFailures(
    id:             FailureID,
    message:        String,
    firstMessage:   FailureMessage,
    restOfMessages: immutable.Seq[FailureMessage],
  ) extends Failures(id, message, firstMessage, restOfMessages)

  def apply(id: FailureID, message: String, fmsg: FailureMessage, fmsgs: FailureMessage*): Failures =
    new ReifiedFailures(id, message, fmsg, fmsgs.toList)
}

/**
  * Marker traits, so that both the [[FailureBase]] and [[Failures]]
  * can be marked with the same semantic meaning
  */
@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
object SemanticFailures {

  /**
    * Meaning:
    *
    * "you cannot find something; it may or may not exist, and I'm not going
    * to tell you anything else"
    */
  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  trait NotFound

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private[exceptions] case object NotFoundID extends FailureID {
    override def name: String = "0"
  }

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private[exceptions] val `Not found` = "Not found"

  /**
    * Meaning:
    *
    * "something is wrong in the way you authorized, you can try again slightly
    * differently"
    */
  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  trait Unauthorized

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private[exceptions] case object UnauthorizedID extends FailureID {
    override def name: String = "1"
  }

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private[exceptions] val `Unauthorized` = "Unauthorized"

  /**
    * Meaning:
    *
    * "it exists, but you're not even allowed to know about that;
    * so for short, you can't find it".
    */
  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  trait Forbidden

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private[exceptions] case object ForbiddenID extends FailureID {
    override def name: String = "2"
  }

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private[exceptions] val `Forbidden` = "Forbidden"

  /**
    * Meaning:
    *
    * "you know it exists, but you are not allowed to see it"
    */
  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  trait Denied

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private[exceptions] case object DeniedID extends FailureID {
    override def name: String = "3"
  }

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private[exceptions] val `Denied` = "Denied"

  /**
    * Obviously, whenever some input data is wrong.
    *
    * This one is probably your best friend, and the one you
    * have to specialize the most for any given problem domain.
    * Otherwise you just wind up with a bunch of nonsense, obtuse
    * errors like:
    * - "the input was wrong"
    * - "gee, thanks, more details, please?"
    * - sometimes you might be tempted to use NotFound, but this
    * might be better suited. For instance, when you are dealing
    * with a "foreign key" situation, and the foreign key is
    * the input of the client. You'd want to be able to tell
    * the user that their input was wrong because something was
    * not found, not simply that it was not found.
    *
    * Therefore, specialize frantically.
    */
  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  trait InvalidInput

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private[exceptions] case object InvalidInputID extends FailureID {
    override def name: String = "4"
  }

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private[exceptions] val `Invalid Input` = "Invalid input"

  /**
    * Special type of invalid input
    *
    * E.g. when you're duplicating something that ought to be unique,
    * like ids, emails.
    */
  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  trait Conflict

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private[exceptions] case object ConflictID extends FailureID {
    override def name: String = "5"
  }

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private[exceptions] val `Conflict` = "Conflict"

}

//=============================================================================
//=============================================================================
//=============================================================================

/**
  * See scaladoc at top of file for general picture.
  *
  * See [[SemanticFailures.NotFound]] for intended use.
  */
@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
abstract class NotFoundFailure(
  message:    String,
  cause:      Option[Throwable] = None,
  parameters: FailureMessage.Parameters = FailureMessage.Parameters.empty
) extends FailureBase(message, cause, parameters) with SemanticFailures.NotFound

@scala.deprecated("Use the types from busymachines.core",                    "0.2.0-RC8")
object NotFoundFailure extends NotFoundFailure(SemanticFailures.`Not found`, None, FailureMessage.Parameters.empty) {

  override def id: FailureID = SemanticFailures.NotFoundID

  private final class ReifiedNotFoundFailure(
    message:    String,
    cause:      Option[Throwable],
    parameters: FailureMessage.Parameters
  ) extends NotFoundFailure(message, cause, parameters) {
    override def id: FailureID = SemanticFailures.NotFoundID
  }

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String): NotFoundFailure =
    new ReifiedNotFoundFailure(message = msg, cause = None, parameters = FailureMessage.Parameters.empty)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String, cause: Throwable): NotFoundFailure =
    new ReifiedNotFoundFailure(message = msg, cause = Some(cause), parameters = FailureMessage.Parameters.empty)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String, params: FailureMessage.Parameters): NotFoundFailure =
    new ReifiedNotFoundFailure(message = msg, cause = None, parameters = params)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(cause: Throwable): NotFoundFailure =
    new ReifiedNotFoundFailure(
      message    = cause.getMessage,
      cause      = Some(cause),
      parameters = FailureMessage.Parameters.empty
    )

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(cause: Throwable, params: FailureMessage.Parameters): NotFoundFailure =
    new ReifiedNotFoundFailure(message = cause.getMessage, cause = None, parameters = params)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String, cause: Throwable, params: FailureMessage.Parameters): NotFoundFailure =
    new ReifiedNotFoundFailure(message = msg, cause = Some(cause), parameters = params)
}

//=============================================================================
//=============================================================================
//=============================================================================

/**
  * See scaladoc at top of file for general picture.
  *
  * See [[SemanticFailures.Unauthorized]] for intended use.
  */
@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
abstract class UnauthorizedFailure(
  message:    String,
  cause:      Option[Throwable] = None,
  parameters: FailureMessage.Parameters = FailureMessage.Parameters.empty
) extends FailureBase(message, cause, parameters) with SemanticFailures.Unauthorized

@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
object UnauthorizedFailure
    extends UnauthorizedFailure(SemanticFailures.`Unauthorized`, None, FailureMessage.Parameters.empty) {

  override def id: FailureID = SemanticFailures.UnauthorizedID

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private final class ReifiedUnauthorizedFailure(
    message:    String,
    cause:      Option[Throwable],
    parameters: FailureMessage.Parameters
  ) extends UnauthorizedFailure(message, cause, parameters) {
    override def id: FailureID = SemanticFailures.UnauthorizedID
  }

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String): UnauthorizedFailure =
    new ReifiedUnauthorizedFailure(message = msg, cause = None, parameters = FailureMessage.Parameters.empty)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String, cause: Throwable): UnauthorizedFailure =
    new ReifiedUnauthorizedFailure(message = msg, cause = Some(cause), parameters = FailureMessage.Parameters.empty)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String, params: FailureMessage.Parameters): UnauthorizedFailure =
    new ReifiedUnauthorizedFailure(message = msg, cause = None, parameters = params)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(cause: Throwable): UnauthorizedFailure =
    new ReifiedUnauthorizedFailure(
      message    = cause.getMessage,
      cause      = Some(cause),
      parameters = FailureMessage.Parameters.empty
    )

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(cause: Throwable, params: FailureMessage.Parameters): UnauthorizedFailure =
    new ReifiedUnauthorizedFailure(message = cause.getMessage, cause = None, parameters = params)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String, cause: Throwable, params: FailureMessage.Parameters): UnauthorizedFailure =
    new ReifiedUnauthorizedFailure(message = msg, cause = Some(cause), parameters = params)
}

//=============================================================================
//=============================================================================
//=============================================================================

/**
  * See scaladoc at top of file for general picture.
  *
  * See [[SemanticFailures.Forbidden]] for intended use.
  */
@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
abstract class ForbiddenFailure(
  message:    String,
  cause:      Option[Throwable] = None,
  parameters: FailureMessage.Parameters = FailureMessage.Parameters.empty
) extends FailureBase(message, cause, parameters) with SemanticFailures.Forbidden

@scala.deprecated("Use the types from busymachines.core",                      "0.2.0-RC8")
object ForbiddenFailure extends ForbiddenFailure(SemanticFailures.`Forbidden`, None, FailureMessage.Parameters.empty) {

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  override def id: FailureID = SemanticFailures.ForbiddenID

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private final class ReifiedForbiddenFailure(
    message:    String,
    cause:      Option[Throwable],
    parameters: FailureMessage.Parameters
  ) extends ForbiddenFailure(message, cause, parameters) {
    override def id: FailureID = SemanticFailures.ForbiddenID
  }

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String): ForbiddenFailure =
    new ReifiedForbiddenFailure(message = msg, cause = None, parameters = FailureMessage.Parameters.empty)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String, cause: Throwable): ForbiddenFailure =
    new ReifiedForbiddenFailure(message = msg, cause = Some(cause), parameters = FailureMessage.Parameters.empty)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String, params: FailureMessage.Parameters): ForbiddenFailure =
    new ReifiedForbiddenFailure(message = msg, cause = None, parameters = params)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(cause: Throwable): ForbiddenFailure =
    new ReifiedForbiddenFailure(
      message    = cause.getMessage,
      cause      = Some(cause),
      parameters = FailureMessage.Parameters.empty
    )

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(cause: Throwable, params: FailureMessage.Parameters): ForbiddenFailure =
    new ReifiedForbiddenFailure(message = cause.getMessage, cause = None, parameters = params)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String, cause: Throwable, params: FailureMessage.Parameters): ForbiddenFailure =
    new ReifiedForbiddenFailure(message = msg, cause = Some(cause), parameters = params)
}

//=============================================================================
//=============================================================================
//=============================================================================

/**
  * See scaladoc at top of file for general picture.
  *
  * See [[SemanticFailures.Denied]] for intended use.
  */
@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
abstract class DeniedFailure(
  message:    String,
  cause:      Option[Throwable] = None,
  parameters: FailureMessage.Parameters = FailureMessage.Parameters.empty
) extends FailureBase(message, cause, parameters) with SemanticFailures.Denied

@scala.deprecated("Use the types from busymachines.core",             "0.2.0-RC8")
object DeniedFailure extends DeniedFailure(SemanticFailures.`Denied`, None, FailureMessage.Parameters.empty) {

  override def id: FailureID = SemanticFailures.DeniedID

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private final class ReifiedDeniedFailure(
    message:    String,
    cause:      Option[Throwable],
    parameters: FailureMessage.Parameters
  ) extends DeniedFailure(message, cause, parameters) {
    override def id: FailureID = SemanticFailures.DeniedID
  }

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String): DeniedFailure =
    new ReifiedDeniedFailure(message = msg, cause = None, parameters = FailureMessage.Parameters.empty)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String, cause: Throwable): DeniedFailure =
    new ReifiedDeniedFailure(message = msg, cause = Some(cause), parameters = FailureMessage.Parameters.empty)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String, params: FailureMessage.Parameters): DeniedFailure =
    new ReifiedDeniedFailure(message = msg, cause = None, parameters = params)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(cause: Throwable): DeniedFailure =
    new ReifiedDeniedFailure(
      message    = cause.getMessage,
      cause      = Some(cause),
      parameters = FailureMessage.Parameters.empty
    )

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(cause: Throwable, params: FailureMessage.Parameters): DeniedFailure =
    new ReifiedDeniedFailure(message = cause.getMessage, cause = None, parameters = params)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String, cause: Throwable, params: FailureMessage.Parameters): DeniedFailure =
    new ReifiedDeniedFailure(message = msg, cause = Some(cause), parameters = params)
}

//=============================================================================
//=============================================================================
//=============================================================================

/**
  * See scaladoc at top of file for general picture.
  *
  * See [[SemanticFailures.InvalidInput]] for intended use.
  */
@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
abstract class InvalidInputFailure(
  message:    String,
  cause:      Option[Throwable] = None,
  parameters: FailureMessage.Parameters = FailureMessage.Parameters.empty
) extends FailureBase(message, cause, parameters) with SemanticFailures.InvalidInput

@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
object InvalidInputFailure
    extends InvalidInputFailure(SemanticFailures.`Invalid Input`, None, FailureMessage.Parameters.empty) {

  override def id: FailureID = SemanticFailures.InvalidInputID

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private final class ReifiedInvalidInputFailure(
    message:    String,
    cause:      Option[Throwable],
    parameters: FailureMessage.Parameters
  ) extends InvalidInputFailure(message, cause, parameters) {
    override def id: FailureID = SemanticFailures.InvalidInputID
  }

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String): InvalidInputFailure =
    new ReifiedInvalidInputFailure(message = msg, cause = None, parameters = FailureMessage.Parameters.empty)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String, cause: Throwable): InvalidInputFailure =
    new ReifiedInvalidInputFailure(message = msg, cause = Some(cause), parameters = FailureMessage.Parameters.empty)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String, params: FailureMessage.Parameters): InvalidInputFailure =
    new ReifiedInvalidInputFailure(message = msg, cause = None, parameters = params)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(cause: Throwable): InvalidInputFailure =
    new ReifiedInvalidInputFailure(
      message    = cause.getMessage,
      cause      = Some(cause),
      parameters = FailureMessage.Parameters.empty
    )

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(cause: Throwable, params: FailureMessage.Parameters): InvalidInputFailure =
    new ReifiedInvalidInputFailure(message = cause.getMessage, cause = None, parameters = params)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String, cause: Throwable, params: FailureMessage.Parameters): InvalidInputFailure =
    new ReifiedInvalidInputFailure(message = msg, cause = Some(cause), parameters = params)
}

//=============================================================================
//=============================================================================
//=============================================================================

/**
  * See scaladoc at top of file for general picture.
  *
  * See [[SemanticFailures.Conflict]] for intended use.
  */
@scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
abstract class ConflictFailure(
  message:    String,
  cause:      Option[Throwable] = None,
  parameters: FailureMessage.Parameters = FailureMessage.Parameters.empty
) extends FailureBase(message, cause, parameters) with SemanticFailures.Conflict

@scala.deprecated("Use the types from busymachines.core",                   "0.2.0-RC8")
object ConflictFailure extends ConflictFailure(SemanticFailures.`Conflict`, None, FailureMessage.Parameters.empty) {

  override def id: FailureID = SemanticFailures.ConflictID

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  private final class ReifiedConflictFailure(
    message:    String,
    cause:      Option[Throwable],
    parameters: FailureMessage.Parameters
  ) extends ConflictFailure(message, cause, parameters) {
    override def id: FailureID = SemanticFailures.ConflictID
  }

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String): ConflictFailure =
    new ReifiedConflictFailure(message = msg, cause = None, parameters = FailureMessage.Parameters.empty)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String, cause: Throwable): ConflictFailure =
    new ReifiedConflictFailure(message = msg, cause = Some(cause), parameters = FailureMessage.Parameters.empty)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String, params: FailureMessage.Parameters): ConflictFailure =
    new ReifiedConflictFailure(message = msg, cause = None, parameters = params)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(cause: Throwable): ConflictFailure =
    new ReifiedConflictFailure(
      message    = cause.getMessage,
      cause      = Some(cause),
      parameters = FailureMessage.Parameters.empty
    )

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(cause: Throwable, params: FailureMessage.Parameters): ConflictFailure =
    new ReifiedConflictFailure(message = cause.getMessage, cause = None, parameters = params)

  @scala.deprecated("Use the types from busymachines.core", "0.2.0-RC8")
  def apply(msg: String, cause: Throwable, params: FailureMessage.Parameters): ConflictFailure =
    new ReifiedConflictFailure(message = msg, cause = Some(cause), parameters = params)
}
