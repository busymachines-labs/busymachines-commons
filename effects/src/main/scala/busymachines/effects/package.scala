package busymachines

import busymachines.effects.future.{FutureSyntaxImplicits, FutureTypeDefinitions}
import busymachines.effects.result.{ResultSyntax,          ResultTypeDefinitions}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jan 2018
  *
  */
package object effects
    extends ResultTypeDefinitions with FutureTypeDefinitions with IOTypeDefinitions with TaskTypeDefinitions
    with ResultSyntax.Implicits with FutureSyntaxImplicits with OptionEffectsSyntaxImplicits with TrySyntax.Implicits
    with ResultEffectsSyntaxImplicits with FutureEffectsSyntaxImplicits with IOEffectsSyntaxImplicits
    with TaskEffectsSyntaxImplicits {

  val Correct:   busymachines.effects.result.Correct.type   = busymachines.effects.result.Correct
  val Incorrect: busymachines.effects.result.Incorrect.type = busymachines.effects.result.Incorrect
}
