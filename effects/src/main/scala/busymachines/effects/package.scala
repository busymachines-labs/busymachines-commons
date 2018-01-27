package busymachines

import busymachines.future.{FutureSyntaxImplicits, FutureTypeDefinitions}
import busymachines.result.{ResultSyntaxImplicits, ResultTypeDefinitions}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jan 2018
  *
  */
package object effects
    extends ResultTypeDefinitions with FutureTypeDefinitions with IOTypeDefinitions with TaskTypeDefinitions
    with ResultSyntaxImplicits with FutureSyntaxImplicits with ResultEffectsSyntaxImplicits
    with FutureEffectsSyntaxImplicits with IOEffectsSyntaxImplicits {

  val Result:    busymachines.result.Result.type    = busymachines.result.Result
  val Correct:   busymachines.result.Correct.type   = busymachines.result.Correct
  val Incorrect: busymachines.result.Incorrect.type = busymachines.result.Incorrect
}
