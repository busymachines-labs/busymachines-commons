package busymachines.core.exceptions

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
  * in other ``busymachines-commons`` module than could be done to the likes
  * of [[Throwable]].
  *
  * The reason why there is a trait [[FailureMessage]], and some types
  * that extend [[Exception]] is that the potentiality of these types can
  * be achieved either through a monad stack approach to building applications,
  * or to a more vanilla scala approach, respectively.
  *
  * There are two quasi-parallel hierarchies of failures:
  * I) the [[FailureMessage]] representing one single failure
  * II) the [[FailureMessages]] representing a container of multiple [[FailureMessage]].
  * The intended use of the [[FailureMessages.id]] (and other ones inherited from [[FailureMessage]]
  * is to signal the general "context" within which the specific [[FailureMessages.messages]]
  * where gathered.
  *
  * There are the following semantically meaningful exceptions (with their plural counterparts elided)
  * that you ought to be using:
  * - [[NotFoundFailure]]     -  [[SemanticFailures.NotFound]]
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
  *     s"Solution to problem $problem not found."
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
trait FailureID {
  def name: String
}

object FailureID {

  private case class GenericFailureID(override val name: String) extends FailureID

  def apply(id: String): FailureID = GenericFailureID(id)
}

object FailureMessage {

  /**
    * This is a hack until dotty (scala 3.0) comes along with union types.
    * Until then, boiler plate freedom is given by the implicit
    * conversions found in the package object
    */
  sealed trait StringOrSeqString

  case class StringWrapper private[exceptions](s: String) extends StringOrSeqString

  case class SeqStringWrapper private[exceptions](ses: Seq[String]) extends StringOrSeqString

  type Parameters = Map[String, StringOrSeqString]

  object Parameters {
    def apply(ps: (String, StringOrSeqString)*): Parameters = Map.apply(ps: _*)

    def empty: Parameters = Map.empty[String, StringOrSeqString]
  }

  private case class GenericFailureMessage(
    override val id: FailureID,
    override val message: String,
    override val parameters: Parameters
  ) extends FailureMessage

  def apply(id: FailureID, message: String, parameters: Parameters = FailureMessage.Parameters.empty): FailureMessage = {
    GenericFailureMessage(id, message, parameters)
  }
}

trait FailureMessage {
  def id: FailureID

  def message: String

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
  */
trait FailureMessages extends FailureMessage {
  def messages: Seq[FailureMessage]
}

object FailureMessages {

  private case class GenericFailureMessages(
    override val id: FailureID,
    override val message: String,
    override val messages: Seq[FailureMessage],
  ) extends FailureMessages

  def apply(id: FailureID, message: String, messages: Seq[FailureMessage]): FailureMessages = {
    GenericFailureMessages(id, message, messages)
  }
}

/**
  * Should be extended sparingly outside of this file.
  *
  * Most likely you need to extend one of the other cases.
  */
abstract class Failure(
  override val message: String,
  val cause: Option[Throwable] = None
) extends Exception(message, cause.orNull) with FailureMessage

/**
  * Similar to [[Failure]] but encapsulate multiple causes.
  *
  * Primarily used as containers for validation failures.
  */
abstract class Failures(
  override val message: String,
  val messages: Seq[FailureMessage]
) extends Exception(message) with FailureMessages

/**
  * Marker traits, so that both the [[Failure]] and [[Failures]]
  * can be marked with the same semantic meaning
  */
object SemanticFailures {

  /**
    * Meaning:
    *
    * "you cannot find something; it may or may not exist, and I'm not going
    * to tell you anything else"
    */
  trait NotFound

  private[exceptions] case object NotFoundID extends FailureID {
    override def name: String = "0"
  }

  private[exceptions] val `Not found` = "Not found"

  /**
    * Meaning:
    *
    * "something is wrong in the way you authorized, you can try again slightly
    * differently"
    */
  trait Unauthorized

  private[exceptions] case object UnauthorizedID extends FailureID {
    override def name: String = "1"
  }

  private[exceptions] val `Unauthorized` = "Unauthorized"

  /**
    * Meaning:
    *
    * "it exists, but you're not even allowed to know about that;
    * so for short, you can't find it".
    */
  trait Forbidden

  private[exceptions] case object ForbiddenID extends FailureID {
    override def name: String = "2"
  }

  private[exceptions] val `Forbidden` = "Forbidden"

  /**
    * Meaning:
    *
    * "you know it exists, but you are not allowed to see it"
    */
  trait Denied

  private[exceptions] case object DeniedID extends FailureID {
    override def name: String = "3"
  }

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
    *
    * Therefore, specialize frantically.
    */
  trait InvalidInput

  private[exceptions] case object InvalidInputID extends FailureID {
    override def name: String = "4"
  }

  private[exceptions] val `Invalid Input` = "Invalid input"

  /**
    * Special type of wrong data.
    *
    * E.g. when you're duplicating something that ought to be unique,
    * like ids, emails.
    */
  trait Conflict

  private[exceptions] case object ConflictID extends FailureID {
    override def name: String = "5"
  }

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
abstract class NotFoundFailure(
  message: String,
  cause: Option[Throwable] = None
) extends Failure(message, cause) with SemanticFailures.NotFound

object NotFoundFailure extends NotFoundFailure(SemanticFailures.`Not found`, None) {

  override def id: FailureID = SemanticFailures.NotFoundID

  private final class ReifiedNotFoundFailure(
    message: String,
    cause: Option[Throwable] = None
  ) extends NotFoundFailure(message, cause) {
    override def id: FailureID = SemanticFailures.NotFoundID
  }

  def apply(msg: String): NotFoundFailure =
    new ReifiedNotFoundFailure(msg)

  def apply(msg: String, cause: Throwable): NotFoundFailure =
    new ReifiedNotFoundFailure(msg, Some(cause))

  def apply(cause: Throwable): NotFoundFailure =
    new ReifiedNotFoundFailure(cause.getMessage, Some(cause))
}

/**
  * Plural counterpart of [[NotFoundFailure]]
  *
  * See scaladoc at top of file for general picture.
  *
  * See [[SemanticFailures.NotFound]] for intended use.
  */
abstract class NotFoundFailures(
  message: String,
  messages: Seq[FailureMessage]
) extends Failures(message, messages) with SemanticFailures.NotFound

object NotFoundFailures extends NotFoundFailures(SemanticFailures.`Not found`, Seq.empty) {

  override def id: FailureID = SemanticFailures.NotFoundID

  private final class ReifiedNotFoundFailures(
    message: String,
    messages: Seq[FailureMessage]
  ) extends NotFoundFailures(message, messages) {
    override def id: FailureID = SemanticFailures.NotFoundID
  }

  def apply(msg: String): NotFoundFailures =
    new ReifiedNotFoundFailures(msg, Seq.empty)

  def apply(msg: String, messages: Seq[FailureMessage]): NotFoundFailures =
    new ReifiedNotFoundFailures(msg, messages)

  def apply(msgs: Seq[String]): NotFoundFailures =
    new ReifiedNotFoundFailures(SemanticFailures.`Not found`, msgs.map(this.apply))
}


//=============================================================================
//=============================================================================
//=============================================================================

/**
  * See scaladoc at top of file for general picture.
  *
  * See [[SemanticFailures.Unauthorized]] for intended use.
  */
abstract class UnauthorizedFailure(
  message: String,
  cause: Option[Throwable] = None
) extends Failure(message, cause) with SemanticFailures.Unauthorized

object UnauthorizedFailure extends NotFoundFailure(SemanticFailures.`Unauthorized`, None) {

  override def id: FailureID = SemanticFailures.UnauthorizedID

  private final class ReifiedUnauthorizedFailure(
    message: String,
    cause: Option[Throwable] = None
  ) extends UnauthorizedFailure(message, cause) {
    override def id: FailureID = SemanticFailures.UnauthorizedID
  }

  def apply(msg: String): UnauthorizedFailure =
    new ReifiedUnauthorizedFailure(msg)

  def apply(msg: String, cause: Throwable): UnauthorizedFailure =
    new ReifiedUnauthorizedFailure(msg, Some(cause))

  def apply(cause: Throwable): UnauthorizedFailure =
    new ReifiedUnauthorizedFailure(cause.getMessage, Some(cause))
}

/**
  * Plural counterpart of [[UnauthorizedFailure]]
  *
  * See scaladoc at top of file for general picture.
  *
  * See [[SemanticFailures.Unauthorized]] for intended use.
  */
abstract class UnauthorizedFailures(
  message: String,
  messages: Seq[FailureMessage]
) extends Failures(message, messages) with SemanticFailures.Unauthorized

object UnauthorizedFailures extends NotFoundFailures(SemanticFailures.`Unauthorized`, Seq.empty) {

  override def id: FailureID = SemanticFailures.UnauthorizedID

  private final class ReifiedUnauthorizedFailures(
    message: String,
    messages: Seq[FailureMessage]
  ) extends UnauthorizedFailures(message, messages) {
    override def id: FailureID = SemanticFailures.UnauthorizedID

    def apply(msg: String): UnauthorizedFailures =
      new ReifiedUnauthorizedFailures(msg, Seq.empty)

    def apply(msg: String, messages: Seq[FailureMessage]): UnauthorizedFailures =
      new ReifiedUnauthorizedFailures(msg, messages)

    def apply(msgs: Seq[String]): UnauthorizedFailures =
      new ReifiedUnauthorizedFailures(SemanticFailures.`Unauthorized`, msgs.map(this.apply))
  }

}

//=============================================================================
//=============================================================================
//=============================================================================


/**
  * See scaladoc at top of file for general picture.
  *
  * See [[SemanticFailures.Forbidden]] for intended use.
  */
abstract class ForbiddenFailure(
  message: String,
  cause: Option[Throwable] = None
) extends Failure(message, cause) with SemanticFailures.Forbidden

object ForbiddenFailure extends ForbiddenFailure(SemanticFailures.`Forbidden`, None) {

  override def id: FailureID = SemanticFailures.ForbiddenID

  private final class ReifiedForbiddenFailure(
    message: String,
    cause: Option[Throwable] = None
  ) extends ForbiddenFailure(message, cause) {
    override def id: FailureID = SemanticFailures.ForbiddenID
  }

  def apply(msg: String): ForbiddenFailure =
    new ReifiedForbiddenFailure(msg)

  def apply(msg: String, cause: Throwable): ForbiddenFailure =
    new ReifiedForbiddenFailure(msg, Some(cause))

  def apply(cause: Throwable): ForbiddenFailure =
    new ReifiedForbiddenFailure(cause.getMessage, Some(cause))
}

/**
  * Plural counterpart of [[ForbiddenFailure]]
  *
  * See scaladoc at top of file for general picture.
  *
  * See [[SemanticFailures.Forbidden]] for intended use.
  */
abstract class ForbiddenFailures(
  message: String,
  messages: Seq[FailureMessage]
) extends Failures(message, messages) with SemanticFailures.Forbidden

object ForbiddenFailures extends ForbiddenFailures(SemanticFailures.`Forbidden`, Seq.empty) {

  override def id: FailureID = SemanticFailures.ForbiddenID

  private final class ReifiedForbiddenFailures(
    message: String,
    messages: Seq[FailureMessage]
  ) extends ForbiddenFailures(message, messages) {
    override def id: FailureID = SemanticFailures.ForbiddenID
  }

  def apply(msg: String): ForbiddenFailures =
    new ReifiedForbiddenFailures(msg, Seq.empty)

  def apply(msg: String, messages: Seq[FailureMessage]): ForbiddenFailures =
    new ReifiedForbiddenFailures(msg, messages)

  def apply(msgs: Seq[String]): ForbiddenFailures =
    new ReifiedForbiddenFailures(SemanticFailures.`Forbidden`, msgs.map(this.apply))
}


//=============================================================================
//=============================================================================
//=============================================================================


/**
  * See scaladoc at top of file for general picture.
  *
  * See [[SemanticFailures.Denied]] for intended use.
  */
abstract class DeniedFailure(
  message: String,
  cause: Option[Throwable] = None
) extends Failure(message, cause) with SemanticFailures.Denied

object DeniedFailure extends ForbiddenFailure(SemanticFailures.`Denied`, None) {

  override def id: FailureID = SemanticFailures.DeniedID

  private final class ReifiedDeniedFailure(
    message: String,
    cause: Option[Throwable] = None
  ) extends DeniedFailure(message, cause) {
    override def id: FailureID = SemanticFailures.DeniedID
  }

  def apply(msg: String): DeniedFailure =
    new ReifiedDeniedFailure(msg)

  def apply(msg: String, cause: Throwable): DeniedFailure =
    new ReifiedDeniedFailure(msg, Some(cause))

  def apply(cause: Throwable): DeniedFailure =
    new ReifiedDeniedFailure(cause.getMessage, Some(cause))
}

/**
  * Plural counterpart of [[DeniedFailure]]
  *
  * See scaladoc at top of file for general picture.
  *
  * See [[SemanticFailures.Denied]] for intended use.
  */
abstract class DeniedFailures(
  message: String,
  messages: Seq[FailureMessage]
) extends Failures(message, messages) with SemanticFailures.Denied

object DeniedFailures extends DeniedFailures(SemanticFailures.`Denied`, Seq.empty) {

  override def id: FailureID = SemanticFailures.DeniedID

  private final class ReifiedDeniedFailures(
    message: String,
    messages: Seq[FailureMessage]
  ) extends DeniedFailures(message, messages) {
    override def id: FailureID = SemanticFailures.DeniedID
  }

  def apply(msg: String): DeniedFailures =
    new ReifiedDeniedFailures(msg, Seq.empty)

  def apply(msg: String, messages: Seq[FailureMessage]): DeniedFailures =
    new ReifiedDeniedFailures(msg, messages)

  def apply(msgs: Seq[String]): DeniedFailures =
    new ReifiedDeniedFailures(SemanticFailures.`Denied`, msgs.map(this.apply))
}


//=============================================================================
//=============================================================================
//=============================================================================


/**
  * See scaladoc at top of file for general picture.
  *
  * See [[SemanticFailures.InvalidInput]] for intended use.
  */
abstract class InvalidInputFailure(
  message: String,
  cause: Option[Throwable] = None
) extends Failure(message, cause) with SemanticFailures.InvalidInput

object InvalidInputFailure extends ForbiddenFailure(SemanticFailures.`Invalid Input`, None) {

  override def id: FailureID = SemanticFailures.InvalidInputID

  private final class ReifiedInvalidInputFailure(
    message: String,
    cause: Option[Throwable] = None
  ) extends InvalidInputFailure(message, cause) {
    override def id: FailureID = SemanticFailures.InvalidInputID
  }

  def apply(msg: String): InvalidInputFailure =
    new ReifiedInvalidInputFailure(msg)

  def apply(msg: String, cause: Throwable): InvalidInputFailure =
    new ReifiedInvalidInputFailure(msg, Some(cause))

  def apply(cause: Throwable): InvalidInputFailure =
    new ReifiedInvalidInputFailure(cause.getMessage, Some(cause))
}

/**
  * Plural counterpart of [[InvalidInputFailure]]
  *
  * See scaladoc at top of file for general picture.
  *
  * See [[SemanticFailures.InvalidInput]] for intended use.
  */
abstract class InvalidInputFailures(
  message: String,
  messages: Seq[FailureMessage]
) extends Failures(message, messages) with SemanticFailures.InvalidInput

object InvalidInputFailures extends InvalidInputFailures(SemanticFailures.`Invalid Input`, Seq.empty) {

  override def id: FailureID = SemanticFailures.InvalidInputID

  private final class ReifiedInvalidInputFailures(
    message: String,
    messages: Seq[FailureMessage]
  ) extends InvalidInputFailures(message, messages) {
    override def id: FailureID = SemanticFailures.InvalidInputID
  }

  def apply(msg: String): InvalidInputFailures =
    new ReifiedInvalidInputFailures(msg, Seq.empty)

  def apply(msg: String, messages: Seq[FailureMessage]): InvalidInputFailures =
    new ReifiedInvalidInputFailures(msg, messages)

  def apply(msgs: Seq[String]): InvalidInputFailures =
    new ReifiedInvalidInputFailures(SemanticFailures.`Invalid Input`, msgs.map(this.apply))
}


//=============================================================================
//=============================================================================
//=============================================================================


/**
  * See scaladoc at top of file for general picture.
  *
  * See [[SemanticFailures.Conflict]] for intended use.
  */
abstract class ConflictFailure(
  message: String,
  cause: Option[Throwable] = None
) extends Failure(message, cause) with SemanticFailures.Conflict

object ConflictFailure extends ConflictFailure(SemanticFailures.`Conflict`, None) {

  override def id: FailureID = SemanticFailures.ConflictID

  private final class ReifiedConflictFailure(
    message: String,
    cause: Option[Throwable] = None
  ) extends
    ConflictFailure(message, cause) {
    override def id: FailureID = SemanticFailures.ConflictID
  }

  def apply(msg: String): ConflictFailure =
    new ReifiedConflictFailure(msg)

  def apply(msg: String, cause: Throwable): ConflictFailure =
    new ReifiedConflictFailure(msg, Some(cause))

  def apply(cause: Throwable): ConflictFailure =
    new ReifiedConflictFailure(cause.getMessage, Some(cause))
}

/**
  * Plural counterpart of [[ConflictFailure]]
  *
  * See scaladoc at top of file for general picture.
  *
  * See [[SemanticFailures.Conflict]] for intended use.
  */
abstract class ConflictFailures(
  message: String,
  messages: Seq[FailureMessage]
) extends Failures(message, messages) with SemanticFailures.Conflict

object ConflictFailures extends ConflictFailures(SemanticFailures.`Conflict`, Seq.empty) {

  override def id: FailureID = SemanticFailures.ConflictID

  private final class ReifiedConflictFailures(
    message: String,
    messages: Seq[FailureMessage]
  ) extends ConflictFailures(message, messages) {
    override def id: FailureID = SemanticFailures.ConflictID
  }

  def apply(msg: String): ConflictFailures =
    new ReifiedConflictFailures(msg, Seq.empty)

  def apply(msg: String, messages: Seq[FailureMessage]): ConflictFailures =
    new ReifiedConflictFailures(msg, messages)

  def apply(msgs: Seq[String]): ConflictFailures =
    new ReifiedConflictFailures(SemanticFailures.`Conflict`, msgs.map(this.apply))
}

