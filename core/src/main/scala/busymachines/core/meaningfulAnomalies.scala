package busymachines.core

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Dec 2017
  *
  */
object MeaningfulAnomalies {

  /**
    * Meaning:
    *
    * "you cannot find something; it may or may not exist, and I'm not going
    * to tell you anything else"
    */
  trait NotFound
  private[core] val `Not found` = "Not found"

  /**
    * Meaning:
    *
    * "something is wrong in the way you authorized, you can try again slightly
    * differently"
    */
  trait Unauthorized
  private[core] val `Unauthorized` = "Unauthorized"

  /**
    * Meaning:
    *
    * "it exists, but you're not even allowed to know about that;
    * so for short, you can't find it".
    */
  trait Forbidden
  private[core] val `Forbidden` = "Forbidden"

  /**
    * Meaning:
    *
    * "you know it exists, but you are not allowed to see it"
    */
  trait Denied
  private[core] val `Denied` = "Denied"

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
  trait InvalidInput
  private[core] val `Invalid Input` = "Invalid input"

  /**
    * Special type of invalid input
    *
    * E.g. when you're duplicating something that ought to be unique,
    * like ids, emails.
    */
  trait Conflict
  private[core] val `Conflict` = "Conflict"
}

private[core] case object NotFoundAnomalyID extends AnomalyID {
  override val name: String = "0"
}

private[core] case object UnauthorizedAnomalyID extends AnomalyID {
  override val name: String = "1"
}

private[core] case object ForbiddenAnomalyID extends AnomalyID {
  override val name: String = "2"
}

private[core] case object DeniedAnomalyID extends AnomalyID {
  override val name: String = "3"
}

private[core] case object InvalidInputAnomalyID extends AnomalyID {
  override val name: String = "4"
}

private[core] case object ConflictAnomalyID extends AnomalyID {
  override val name: String = "5"
}
