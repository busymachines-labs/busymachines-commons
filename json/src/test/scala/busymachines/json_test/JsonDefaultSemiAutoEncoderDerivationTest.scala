package busymachines.json_test

import org.scalatest.FlatSpec


/**
  *
  * See the [[Melon]] hierarchy
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 09 Aug 2017
  *
  */
private[json_test] object melonsDefaultSemiAutoEncoders {

  import busymachines.json._
  import busymachines.json.semiauto._

  implicit val tasteEncoder: Encoder[Taste] = deriveEnumerationEncoder[Taste]
  implicit val melonEncoder: ObjectEncoder[Melon] = deriveEncoder[Melon]
}

class JsonDefaultSemiAutoEncoderDerivationTest extends FlatSpec {

  import busymachines.json.syntax._
  import melonsDefaultSemiAutoEncoders._

  //-----------------------------------------------------------------------------------------------

  it should "... fail to compile when there is no defined encoder for a type down in the hierarchy" in {
    assertDoesNotCompile(
      """
        |val winterMelon: WinterMelon = WinterMelon(fuzzy = true, weight = 45)
        |winterMelon.asJson
      """.stripMargin
    )
  }

  //-----------------------------------------------------------------------------------------------

  it should "... be able to serialize case class from hierarchy when it is referred to as its super-type" in {
    val winterMelon: Melon = WinterMelon(fuzzy = true, weight = 45)
    val rawJson = winterMelon.asJson.spaces2

    assertResult(
      """
        |{
        |  "fuzzy" : true,
        |  "weight" : 45,
        |  "_type" : "WinterMelon"
        |}
      """.stripMargin.trim
    )(rawJson)
  }

  //-----------------------------------------------------------------------------------------------

  it should "... be able to serialize case objects of the hierarchy" in {
    val winterMelon: Melon = SmallMelon
    val rawJson = winterMelon.asJson.spaces2
    assertResult(
      """
        |{
        |  "_type" : "SmallMelon"
        |}
      """.stripMargin.trim
    )(rawJson)
  }

  //-----------------------------------------------------------------------------------------------

  it should "... deserialize hierarchies of case objects as enums (i.e. plain strings)" in {
    val taste: List[Taste] = List(SweetTaste, SourTaste)

    val jsonRaw = taste.asJson.spaces2
    assertResult(
      """
        |[
        |  "SweetTaste",
        |  "SourTaste"
        |]
      """.stripMargin.trim
    )(jsonRaw)
  }

  //-----------------------------------------------------------------------------------------------

  it should "... deserialize list of all case classes from the hierarchy" in {
    val winterMelon: Melon = WinterMelon(fuzzy = true, weight = 45)
    val waterMelon: Melon = WaterMelon(seeds = true, weight = 90)
    val smallMelon: Melon = SmallMelon
    val squareMelon: Melon = SquareMelon(weight = 10, tastes = Seq(SourTaste, SweetTaste))
    val melons = List[Melon](winterMelon, waterMelon, smallMelon, squareMelon)

    val jsonRaw = melons.asJson.spaces2
    assertResult(
      """
        |
        |[
        |  {
        |    "fuzzy" : true,
        |    "weight" : 45,
        |    "_type" : "WinterMelon"
        |  },
        |  {
        |    "seeds" : true,
        |    "weight" : 90,
        |    "_type" : "WaterMelon"
        |  },
        |  {
        |    "_type" : "SmallMelon"
        |  },
        |  {
        |    "weight" : 10,
        |    "tastes" : [
        |      "SourTaste",
        |      "SweetTaste"
        |    ],
        |    "_type" : "SquareMelon"
        |  }
        |]
        |
      """.stripMargin.trim
    )(jsonRaw)
  }
  //-----------------------------------------------------------------------------------------------

  //-----------------------------------------------------------------------------------------------

  //-----------------------------------------------------------------------------------------------

  //-----------------------------------------------------------------------------------------------

  //-----------------------------------------------------------------------------------------------
}




