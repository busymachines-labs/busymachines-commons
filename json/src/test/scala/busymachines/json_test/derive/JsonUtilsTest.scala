package busymachines.json_test.derive

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

    JsonParsing.parseString(rawJson).right.value
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
      throw JsonParsing.parseString(rawJson).left.value
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

  import melonsDefaultSemiAutoDecoders._

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

    val am = JsonDecoding.decodeAs[AnarchistMelon](rawJson).right.value
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
      throw JsonDecoding.decodeAs[AnarchistMelon](rawJson).left.value
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
      throw JsonDecoding.decodeAs[AnarchistMelon](rawJson).left.value
    }
  }

  //-----------------------------------------------------------------------------------------------

}
