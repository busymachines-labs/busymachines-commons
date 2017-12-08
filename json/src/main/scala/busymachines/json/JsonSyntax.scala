package busymachines.json

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 19 Oct 2017
  *
  */
trait JsonSyntax {

  implicit final class EncoderOps[A](val wrappedEncodeable: A) {
    def asJson(implicit encoder: Encoder[A]): Json = encoder(wrappedEncodeable)

    def asJsonObject(implicit encoder: ObjectEncoder[A]): JsonObject =
      encoder.encodeObject(wrappedEncodeable)
  }

  implicit final class DecoderOpsString(val rawJson: String) {
    def unsafeAsJson: Json = JsonParsing.unsafeParseString(rawJson)

    def unsafeDecodeAs[A](implicit decoder: Decoder[A]): A =
      JsonDecoding.unsafeDecodeAs[A](rawJson)

    def decodeAs[A](implicit decoder: Decoder[A]): JsonDecodingResult[A] = {
      JsonDecoding.decodeAs[A](rawJson)
    }
  }

  implicit final class DecoderOpsJson(val js: Json) {

    def unsafeDecodeAs[A](implicit decoder: Decoder[A]): A =
      JsonDecoding.unsafeDecodeAs[A](js)

    def decodeAs[A](implicit decoder: Decoder[A]): JsonDecodingResult[A] = {
      JsonDecoding.decodeAs[A](js)
    }

    def noSpacesNoNulls: String = js.pretty(PrettyJson.noSpacesNoNulls)

    def spaces2NoNulls: String = js.pretty(PrettyJson.spaces2NoNulls)

    def spaces4NoNulls: String = js.pretty(PrettyJson.spaces4NoNulls)
  }

}
