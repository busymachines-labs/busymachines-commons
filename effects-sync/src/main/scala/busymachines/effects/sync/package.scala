package busymachines.effects

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 29 Jan 2018
  *
  */
package object sync
    extends TryTypeDefinitons with ResultTypeDefinitions with OptionSyntax.Implicits with EitherSyntax.Implicits
    with ResultSyntax.Implicits with TrySyntax.Implicits {

  object tr extends TryTypeDefinitons with TrySyntax.Implicits

  object option extends OptionSyntax.Implicits

  object either extends EitherSyntax.Implicits

  object result extends ResultTypeDefinitions with ResultSyntax.Implicits {
    val Result:    busymachines.effects.sync.Result.type    = busymachines.effects.sync.Result
    val Correct:   busymachines.effects.sync.Correct.type   = busymachines.effects.sync.Correct
    val Incorrect: busymachines.effects.sync.Incorrect.type = busymachines.effects.sync.Incorrect
  }

}
