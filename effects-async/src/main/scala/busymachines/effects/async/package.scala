package busymachines.effects

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 29 Jan 2018
  *
  */
package object async
    extends AnyRef with FutureTypeDefinitions with IOTypeDefinitions with TaskTypeDefinitions
    with TrySyntaxAsync.Implcits with EitherSyntaxAsync.Implcits with ResultSyntaxAsync.Implcits {}
