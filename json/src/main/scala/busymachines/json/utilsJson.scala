package busymachines.json

import io.circe.parser._
import busymachines.core.exceptions._

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
    //FIXME: clean up this mess once we drop scala 2.11 cross-compilation and we can do right biased for
    val je = JsonParsing.parseString(json)
    je.right.flatMap(json => this.decodeAs(json))
  }

  def unsafeDecodeAs[A](json: Json)(implicit decoder: Decoder[A]): A = {
    //FIXME: clean up this mess once we drop scala 2.11 cross-compilation and we can do .toTry.get
    this.decodeAs[A](json)(decoder) match {
      case Right(j) => j
      case Left(t) => throw t
    }
  }

  def unsafeDecodeAs[A](json: String)(implicit decoder: Decoder[A]): A = {
    //FIXME: clean up this mess once we drop scala 2.11 cross-compilation and we can do .toTry.get
    JsonDecoding.decodeAs(json) match {
      case Right(j) => j
      case Left(t) => throw t
    }
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

  //FIXME: once we no longer cross-compile to scala 2.11 replace this with .toTry.get
  def unsafeParseString(input: String): Json = {
    JsonParsing.parseString(input) match {
      case Right(j) => j
      case Left(t) => throw t
    }
  }

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
