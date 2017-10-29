package busymachines

/**
  * This package is meant to give you a rought equivalent experience to using the
  * intented "busymachines-commons-json" module. Therefore, type alias are the same
  * in both modules, but here they are mapped approximately to spray versions (ObjectEncoder is
  * not quite the same as RootJsonFormat), but for most practical purposes it will work.
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
package object json {

  type Encoder[A] = spray.json.JsonWriter[A]
  final val Encoder: spray.json.JsonWriter.type = spray.json.JsonWriter
  type ObjectEncoder[A] = spray.json.RootJsonWriter[A]

  type Decoder[A] = spray.json.JsonReader[A]
  final val Decoder: spray.json.JsonReader.type = spray.json.JsonReader
  type ObjectDecoder[A] = spray.json.RootJsonReader[A]

  type Codec[A] = spray.json.RootJsonFormat[A]
  type ValueCodec[A] = spray.json.JsonFormat[A]

  type Json = spray.json.JsValue
  type JsonObject = spray.json.JsObject
  val JsonObject: spray.json.JsObject.type = spray.json.JsObject


}
