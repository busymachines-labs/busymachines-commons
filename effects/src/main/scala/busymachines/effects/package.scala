package busymachines

import busymachines.effects.future.{FutureSyntaxImplicits, FutureTypeDefinitions}
import busymachines.effects.sync.{ResultSyntax,            ResultTypeDefinitions, TrySyntax}

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

  val Result:    busymachines.effects.sync.Result.type    = busymachines.effects.sync.Result
  val Correct:   busymachines.effects.sync.Correct.type   = busymachines.effects.sync.Correct
  val Incorrect: busymachines.effects.sync.Incorrect.type = busymachines.effects.sync.Incorrect
}
