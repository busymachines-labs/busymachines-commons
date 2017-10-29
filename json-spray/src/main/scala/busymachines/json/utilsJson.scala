package busymachines.json

import busymachines.core.exceptions._
import spray.json.{CompactPrinter, JsonPrinter, PrettyPrinter}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
object JsonDecoding {
  def unsafeDecodeAs[A](json: Json)(implicit decoder: Decoder[A]): A = {
    decoder.read(json)
  }

  def unsafeDecodeAs[A](json: String)(implicit decoder: Decoder[A]): A = {
    decoder.read(JsonParsing.unsafeParseString(json))
  }
}

final case class JsonDecodingFailure(msg: String) extends InvalidInputFailure(msg) {
  override def id: FailureID = JsonFailureIDs.JsonDecodingFailureID
}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  */
object JsonParsing {

  def unsafeParseString(input: String): Json = {
    spray.json.pimpString(input).parseJson
  }

}

object PrettyJson {
  val noSpacesNoNulls: JsonPrinter = CompactPrinter
  val spaces2NoNulls: JsonPrinter = PrettyPrinter

  val noSpaces: JsonPrinter = noSpacesNoNulls
  val spaces2: JsonPrinter = spaces2NoNulls

}

final case class JsonParsingFailure(msg: String) extends InvalidInputFailure(msg) {
  override def id: FailureID = JsonFailureIDs.JsonParsingFailureID
}

/**
  *
  */
object JsonFailureIDs {

  case object JsonParsingFailureID extends FailureID {
    override def name: String = "json_01"
  }

  case object JsonDecodingFailureID extends FailureID {
    override def name: String = "json_02"
  }

}
