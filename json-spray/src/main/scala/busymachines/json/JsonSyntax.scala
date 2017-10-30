package busymachines.json

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 19 Oct 2017
  *
  */
trait JsonSyntax {

  implicit final class EncoderOps[A](val wrappedEncodeable: A) {
    def asJson(implicit encoder: ValueEncoder[A]): Json = encoder.write(wrappedEncodeable)

    def asJsonObject(implicit encoder: Encoder[A]): JsonObject =
      encoder.write(wrappedEncodeable).asJsObject("... well, this sucks, this is why this module is deprecated. You tried to convert an array to a JsObject")
  }

  implicit final class DecoderOpsString(val rawJson: String) {
    def unsafeAsJson: Json = JsonParsing.unsafeParseString(rawJson)

    def unsafeDecodeAs[A](implicit decoder: ValueDecoder[A]): A =
      JsonDecoding.unsafeDecodeAs[A](rawJson)

  }

  implicit final class DecoderOpsJson(val js: Json) {
    def unsafeDecodeAs[A](implicit decoder: ValueDecoder[A]): A =
      JsonDecoding.unsafeDecodeAs[A](js)

    def noSpaces: String = PrettyJson.noSpaces(js)

    def spaces2: String = PrettyJson.spaces2(js)

    def spaces4: String = PrettyJson.spaces2(js)

    def noSpacesNoNulls: String = noSpaces

    def spaces2NoNulls: String = spaces2

    def spaces4NoNulls: String = spaces4
  }

}
