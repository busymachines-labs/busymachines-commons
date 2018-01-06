package busymachines.core

/**
  * Some suggested naming conventions are put here so that they're easily accessible.
  * These can also be found in the scaladoc of [[busymachines.core.AnomalyID]]
  *
  * - [[busymachines.core.MeaningfulAnomalies.NotFound]]
  *   - range: 000-099; e.g. pone_001, ptwo_076, pthree_099
  *
  * - [[busymachines.core.MeaningfulAnomalies.UnauthorizedMsg]]
  *   - range: 100-199; e.g. pone_100, ptwo_176, pthree_199
  *
  * - [[busymachines.core.MeaningfulAnomalies.ForbiddenMsg]]
  *   - range: 200-299; e.g. pone_200, ptwo_276, pthree_299
  *
  * - [[busymachines.core.MeaningfulAnomalies.DeniedMsg]]
  *   - range: 300-399; e.g. pone_300, ptwo_376, pthree_399
  *
  * - [[busymachines.core.MeaningfulAnomalies.InvalidInput]]
  *   - range: 400-499; e.g. pone_400, ptwo_476, pthree_499
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Dec 2017
  */
object MeaningfulAnomalies {

  /**
    * Meaning:
    *
    * "you cannot find something; it may or may not exist, and I'm not going
    * to tell you anything else"
    */
  trait NotFound
  private[core] val NotFoundMsg = "Not found"

  /**
    * Meaning:
    *
    * "something is wrong in the way you authorized, you can try again slightly
    * differently"
    */
  trait Unauthorized
  private[core] val UnauthorizedMsg = "Unauthorized"

  /**
    * Meaning:
    *
    * "it exists, but you're not even allowed to know about that;
    * so for short, you can't find it".
    */
  trait Forbidden
  private[core] val ForbiddenMsg = "Forbidden"

  /**
    * Meaning:
    *
    * "you know it exists, but you are not allowed to see it"
    */
  trait Denied
  private[core] val DeniedMsg = "Denied"

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
  private[core] val InvalidInputMsg = "Invalid input"

  /**
    * Special type of invalid input
    *
    * E.g. when you're duplicating something that ought to be unique,
    * like ids, emails.
    */
  trait Conflict
  private[core] val ConflictMsg = "Conflict"
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
