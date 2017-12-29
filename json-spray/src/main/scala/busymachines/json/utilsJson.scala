package busymachines.json

import busymachines.core._
import spray.json.{CompactPrinter, JsonPrinter, PrettyPrinter}

import scala.util.Try
import scala.util.control.NonFatal

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
object JsonDecoding {
  private def unsafeRecover[A](a: => A): A = {
    Try(a).recoverWith {
      case e: JsonParsingFailure => scala.util.Failure(e)
      case NonFatal(e) => scala.util.Failure(JsonDecodingFailure(e.getMessage))
    }.get
  }

  def unsafeDecodeAs[A](json: Json)(implicit decoder: ValueDecoder[A]): A = {
    unsafeRecover(decoder.read(json))
  }

  def unsafeDecodeAs[A](json: String)(implicit decoder: ValueDecoder[A]): A = {
    unsafeRecover(decoder.read(JsonParsing.unsafeParseString(json)))
  }
}

final case class JsonDecodingFailure(msg: String) extends InvalidInputFailure(msg) {
  override def id: AnomalyID = JsonAnomalyIDs.JsonDecodingAnomalyID
}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  */
object JsonParsing {

  def unsafeParseString(input: String): Json = {
    Try[Json](spray.json.JsonParser(input)).recoverWith {
      case NonFatal(e) =>
        scala.util.Failure(busymachines.json.JsonParsingFailure(e.getMessage))
    }.get
  }

}

object PrettyJson {
  val noSpacesNoNulls: JsonPrinter = CompactPrinter
  val spaces2NoNulls:  JsonPrinter = PrettyPrinter

  val noSpaces: JsonPrinter = noSpacesNoNulls
  val spaces2:  JsonPrinter = spaces2NoNulls

}

final case class JsonParsingFailure(msg: String) extends InvalidInputFailure(msg) {
  override def id: AnomalyID = JsonAnomalyIDs.JsonParsingAnomalyID
}

/**
  *
  */
object JsonAnomalyIDs {

  case object JsonParsingAnomalyID extends AnomalyID {
    override def name: String = "json_01"
  }

  case object JsonDecodingAnomalyID extends AnomalyID {
    override def name: String = "json_02"
  }

}
