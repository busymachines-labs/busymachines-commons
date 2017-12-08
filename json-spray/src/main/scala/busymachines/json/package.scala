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

  type ValueEncoder[A] = spray.json.JsonWriter[A]
  final val ValueEncoder: spray.json.JsonWriter.type = spray.json.JsonWriter
  type Encoder[A] = spray.json.RootJsonWriter[A]

  type ValueDecoder[A] = spray.json.JsonReader[A]
  final val ValueDecoder: spray.json.JsonReader.type = spray.json.JsonReader
  type Decoder[A] = spray.json.RootJsonReader[A]

  /**
    * Since [[Codec]] is the desired type signature when using
    * derivation if we want to keep it syntactically consistent
    * with the other json module, then we have no choice but to
    * sacrifice a bit of semantic sense (for this particular case)
    * for the greater good.
    */
  type Codec[A]      = spray.json.RootJsonFormat[A]
  type ValueCodec[A] = spray.json.JsonFormat[A]

  type Json       = spray.json.JsValue
  type JsonObject = spray.json.JsObject
  val JsonObject: spray.json.JsObject.type = spray.json.JsObject

}
