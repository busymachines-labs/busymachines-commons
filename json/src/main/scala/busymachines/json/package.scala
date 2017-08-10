package busymachines

import busymachines.core.exceptions._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
package object json {

  object semiauto extends SemiAutoDerivation

  object auto extends io.circe.generic.extras.AutoDerivation

  type Encoder[A] = io.circe.Encoder[A]
  final val Encoder: io.circe.Encoder.type = io.circe.Encoder
  type ObjectEncoder[A] = io.circe.ObjectEncoder[A]
  final val ObjectEncoder: io.circe.ObjectEncoder.type = io.circe.ObjectEncoder

  type Decoder[A] = io.circe.Decoder[A]
  final val Decoder: io.circe.Decoder.type = io.circe.Decoder
  type ObjectDecoder[A] = io.circe.ObjectEncoder[A]
  final val ObjectDecoder: io.circe.ObjectEncoder.type = io.circe.ObjectEncoder

  type Configuration = io.circe.generic.extras.Configuration
  final val Configuration: io.circe.generic.extras.Configuration.type =
    io.circe.generic.extras.Configuration

  type Json = io.circe.Json
  type JsonObject = io.circe.JsonObject
  type HCursor = io.circe.HCursor

  final val JsonTypeString: String = "_type"

  final implicit val defaultDerivationConfiguration: Configuration = Configuration.default
    .withDiscriminator(JsonTypeString)

  type JsonDecodingResult[A] = Either[Failure, A]
  type JsonParsingResult = Either[Failure, Json]
}
