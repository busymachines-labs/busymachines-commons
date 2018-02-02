package busymachines

import busymachines.effects.async._
import busymachines.effects.sync._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 02 Feb 2018
  *
  */
package object effects
    extends AnyRef with OptionSyntax.Implicits with OptionSyntaxAsync.Implcits with TryTypeDefinitons
    with TrySyntax.Implicits with TrySyntaxAsync.Implcits with EitherSyntax.Implicits with EitherSyntaxAsync.Implcits
    with ResultTypeDefinitions with ResultCompanionAliases with ResultSyntax.Implicits with ResultSyntaxAsync.Implcits
    with FutureTypeDefinitions with FutureSyntax.Implicits with IOTypeDefinitions with IOSyntax.Implicits
    with TaskTypeDefinitions with TaskSyntax.Implicits {

  object option extends OptionSyntax.Implicits with OptionSyntaxAsync.Implcits

  object tr extends TryTypeDefinitons with TrySyntax.Implicits with TrySyntaxAsync.Implcits

  object either extends EitherSyntax.Implicits with EitherSyntaxAsync.Implcits

  object result
      extends ResultTypeDefinitions with ResultCompanionAliases with ResultSyntax.Implicits
      with ResultSyntaxAsync.Implcits

  object io extends IOTypeDefinitions with IOSyntax.Implicits

  object future extends FutureTypeDefinitions with FutureSyntax.Implicits

  object task extends TaskTypeDefinitions with TaskSyntax.Implicits

}
