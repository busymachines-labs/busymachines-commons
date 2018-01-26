package busymachines.json

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jan 2018
  *
  */
trait JsonTypeDefinitions {
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
}
