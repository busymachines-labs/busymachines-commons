package busymachines.effects

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 09 Jan 2018
  *
  */
package object sync
    extends TryTypeDefinitons with ResultTypeDefinitions with OptionSyntax.Implicits with EitherSyntax.Implicits
    with ResultSyntax.Implicits with TrySyntax.Implicits
