package busymachines.json

import io.circe.parser._
import busymachines.core.exceptions._
import io.circe.Printer

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
object JsonDecoding {
  def decodeAs[A](json: Json)(implicit decoder: Decoder[A]): JsonDecodingResult[A] = {
    val r: io.circe.Decoder.Result[A] = decoder.decodeJson(json)
    r.left.map(df => JsonDecodingFailure(df.getMessage))
  }

  def decodeAs[A](json: String)(implicit decoder: Decoder[A]): JsonDecodingResult[A] = {
    val je = JsonParsing.parseString(json)
    je.flatMap(json => this.decodeAs(json))
  }

  def unsafeDecodeAs[A](json: Json)(implicit decoder: Decoder[A]): A = {
    this.decodeAs[A](json)(decoder).toTry.get
  }

  def unsafeDecodeAs[A](json: String)(implicit decoder: Decoder[A]): A = {
    JsonDecoding.decodeAs(json).toTry.get
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
  def parseString(input: String): JsonParsingResult = {
    parse(input).left.map(pf => JsonParsingFailure(pf.message))
  }

  def unsafeParseString(input: String): Json = {
    JsonParsing.parseString(input).toTry.get
  }

}

object PrettyJson {
  val noSpacesNoNulls: Printer = Printer.noSpaces.copy(dropNullValues = true)
  val spaces2NoNulls: Printer = Printer.spaces2.copy(dropNullValues = true)
  val spaces4NoNulls: Printer = Printer.spaces4.copy(dropNullValues = true)

  val noSpaces: Printer = Printer.noSpaces
  val spaces2: Printer = Printer.spaces2
  val spaces4: Printer = Printer.spaces4

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
