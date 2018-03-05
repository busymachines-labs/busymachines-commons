package busymachines.effects.async

import busymachines.duration._
import busymachines.effects.sync.ConstantsSyncEffects

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Feb 2018
  *
  */
object ConstantsAsyncEffects {
  val UnitFunction1 = ConstantsSyncEffects.UnitFunction1

  val defaultDuration: FiniteDuration = minutes(1)
}
