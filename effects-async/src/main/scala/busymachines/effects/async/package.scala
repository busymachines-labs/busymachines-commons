package busymachines.effects

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 29 Jan 2018
  *
  */
package object async
    extends AnyRef with FutureTypeDefinitions with IOTypeDefinitions with TaskTypeDefinitions
    with OptionSyntaxAsync.Implcits with TrySyntaxAsync.Implcits with EitherSyntaxAsync.Implcits
    with ResultSyntaxAsync.Implcits with FutureSyntax.Implicits with IOSyntax.Implicits with TaskSyntax.Implicits {

  object tr extends TrySyntaxAsync.Implcits

  object option extends OptionSyntaxAsync.Implcits

  object either extends EitherSyntaxAsync.Implcits

  object result extends ResultSyntaxAsync.Implcits

  object validated extends ValidatedSyntaxAsync.Implcits

  object io extends IOTypeDefinitions with IOSyntax.Implicits

  object future extends FutureTypeDefinitions with FutureSyntax.Implicits

  @scala.deprecated(
    "0.3.0-RC11",
    "Monix support will be dropped in 0.4.x — replace w/ cats-effect, or roll your own monix syntax",
  )
  object task extends TaskTypeDefinitions with TaskSyntax.Implicits

}
