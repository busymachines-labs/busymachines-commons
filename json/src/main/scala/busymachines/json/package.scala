package busymachines

import busymachines.core.exceptions._

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
package object json extends DefaultTypeDiscriminatorConfig {

  type Encoder[A] = io.circe.Encoder[A]
  final val Encoder: io.circe.Encoder.type = io.circe.Encoder
  type ObjectEncoder[A] = io.circe.ObjectEncoder[A]
  final val ObjectEncoder: io.circe.ObjectEncoder.type = io.circe.ObjectEncoder

  type Decoder[A] = io.circe.Decoder[A]
  final val Decoder: io.circe.Decoder.type = io.circe.Decoder
  type ObjectDecoder[A] = io.circe.ObjectEncoder[A]
  final val ObjectDecoder: io.circe.ObjectEncoder.type = io.circe.ObjectEncoder

  type Configuration = io.circe.generic.extras.Configuration
  final val Configuration: io.circe.generic.extras.Configuration.type = io.circe.generic.extras.Configuration

  type Json = io.circe.Json
  val Json: io.circe.Json.type = io.circe.Json
  type JsonObject = io.circe.JsonObject
  val JsonObject: io.circe.JsonObject.type = io.circe.JsonObject
  type HCursor = io.circe.HCursor
  val HCursor: io.circe.HCursor.type = io.circe.HCursor

  type JsonDecodingResult[A] = Either[Failure, A]
  type JsonParsingResult = Either[Failure, Json]
}
