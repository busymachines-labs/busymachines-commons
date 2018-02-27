package busymachines.effects.sync

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Feb 2018
  *
  */
package object validated
    extends AnyRef with ValidatedTypeDefinitions with ValidatedSyntax.Implicits with OptionSyntaxCats.Implicits
    with TrySyntaxCats.Implicits with ResultSyntaxCats.Implicits {}
