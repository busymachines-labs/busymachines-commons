package busymachines

import busymachines.effects.future.{FutureSyntaxImplicits, FutureTypeDefinitions}
import busymachines.effects.result.{ResultSyntaxImplicits, ResultTypeDefinitions}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jan 2018
  *
  */
package object effects
    extends ResultTypeDefinitions with FutureTypeDefinitions with IOTypeDefinitions with TaskTypeDefinitions
    with ResultSyntaxImplicits with FutureSyntaxImplicits with OptionEffectsSyntaxImplicits
    with TryEffectsSyntaxImplicits with ResultEffectsSyntaxImplicits with FutureEffectsSyntaxImplicits
    with IOEffectsSyntaxImplicits with TaskEffectsSyntaxImplicits {

  val Correct:   busymachines.effects.result.Correct.type   = busymachines.effects.result.Correct
  val Incorrect: busymachines.effects.result.Incorrect.type = busymachines.effects.result.Incorrect
}
