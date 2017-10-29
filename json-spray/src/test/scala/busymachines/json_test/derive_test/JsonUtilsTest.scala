package busymachines.json_test.derive_test

import busymachines.json.{JsonDecoding, JsonDecodingFailure, JsonParsing, JsonParsingFailure}
import busymachines.json_test.AnarchistMelon
import org.scalatest.{EitherValues, FlatSpec, Matchers}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
class JsonUtilsTest extends FlatSpec with EitherValues with Matchers {
  import MelonsJsonCodec._

  behavior of "JsonParsing.safe"

  //-----------------------------------------------------------------------------------------------

  it should ".... parse correct json" in {
    val rawJson =
      """
        |{
        |  "noGods" : true,
        |  "noMasters" : true,
        |  "noSuperTypes" : true
        |}
      """.stripMargin

    JsonParsing.unsafeParseString(rawJson)
  }

  //-----------------------------------------------------------------------------------------------

  it should ".... fail on incorrect json" in {
    val rawJson =
      """
        |{
        |  "noGods" : true
        |  "noMasters" : true,
        |  "noSuperTypes" : true
        |}
      """.stripMargin

    an[JsonParsingFailure] shouldBe thrownBy {
      JsonParsing.unsafeParseString(rawJson)
    }
  }

  //-----------------------------------------------------------------------------------------------

  behavior of "JsonParsing.unsafe"

  //-----------------------------------------------------------------------------------------------

  it should ".... parse correct json" in {
    val rawJson =
      """
        |{
        |  "noGods" : true,
        |  "noMasters" : true,
        |  "noSuperTypes" : true
        |}
      """.stripMargin

    JsonParsing.unsafeParseString(rawJson)
  }

  //-----------------------------------------------------------------------------------------------

  it should ".... throw exception on incorrect json" in {
    val rawJson =
      """
        |{
        |  "noGods" : true
        |  "noMasters" : true,
        |  "noSuperTypes" : true
        |}
      """.stripMargin

    an[JsonParsingFailure] shouldBe thrownBy {
      JsonParsing.unsafeParseString(rawJson)
    }
  }

  //-----------------------------------------------------------------------------------------------

  behavior of "JsonDecoding.safe"

  //-----------------------------------------------------------------------------------------------

  it should "... correctly decode when JSON, and representation are correct" in {
    val rawJson =
      """
        |{
        |  "noGods" : true,
        |  "noMasters" : true,
        |  "noSuperTypes" : true
        |}
      """.stripMargin

    val am = JsonDecoding.unsafeDecodeAs[AnarchistMelon](rawJson)
    assertResult(AnarchistMelon(noGods = true, noMasters = true, noSuperTypes = true))(am)
  }

  //-----------------------------------------------------------------------------------------------

  it should "... fail with parsing error when JSON has syntax errors" in {
    val rawJson =
      """
        |{
        |  "noGods" : true
        |  "noMasters" : true,
        |  "noSuperTypes" : true
        |}
      """.stripMargin

    an[JsonParsingFailure] shouldBe thrownBy {
      JsonDecoding.unsafeDecodeAs[AnarchistMelon](rawJson)
    }
  }

  //-----------------------------------------------------------------------------------------------

  it should "... fail with decoding error when JSON is syntactically correct, but encoding is wrong" in {
    val rawJson =
      """
        |{
        |  "noMasters" : true,
        |  "noSuperTypes" : true
        |}
      """.stripMargin

    the[JsonDecodingFailure] thrownBy {
      JsonDecoding.unsafeDecodeAs[AnarchistMelon](rawJson)
    }
  }

  //-----------------------------------------------------------------------------------------------

}
