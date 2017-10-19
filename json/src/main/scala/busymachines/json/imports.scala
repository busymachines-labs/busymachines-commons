package busymachines.json

/**
  *
  * This file exists for two reasons:
  * 1) - didn't want to have the library depend too much on package objects
  * 2) - out of the blue we started getting the following compiler error in client code
  *
  * {{{
  *   Error:(20, 11) package cats contains object and package with same name: implicits
  *    one of them needs to be removed from classpath
  *    c.as[String].right.map(FailureID.apply)
  * }}}
  *
  * Therefore "package-like" imports are modeled as much as possible as static imports of
  * the properties in a simple object
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 19 Oct 2017
  *
  */

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
object syntax extends JsonSyntax

object auto extends DefaultTypeDiscriminatorConfig with io.circe.generic.extras.AutoDerivation

object semiauto extends DefaultTypeDiscriminatorConfig with SemiAutoDerivation
