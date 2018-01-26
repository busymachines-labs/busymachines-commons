package busymachines

/**
  * The reason we break the modularity of circe is rather pragmatic. The design philosophy of this
  * library is to have one import for the most common use case. And that is making it easy to encode/decode
  * to and from REST api and, or various database drivers.
  *
  * The ideal is that if you just want to be able to use transform a bunch of case classes into
  * JSON you should really just have to import
  * {{{
  *   import busymachines.json._
  *   import busymachines.json.autoderive._
  * }}}
  *
  * If you also need explicit parsing/pretty printing, or other operations, then it should be realizeable with
  * and additional:
  * {{{
  *   import busymachines.syntax._
  * }}}
  *
  * This is all fine, as long as the basic types [[io.circe.Encoder]] and [[io.circe.Decoder]] keep their
  * current form
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
package object json extends JsonTypeDefinitions with DefaultTypeDiscriminatorConfig {}
