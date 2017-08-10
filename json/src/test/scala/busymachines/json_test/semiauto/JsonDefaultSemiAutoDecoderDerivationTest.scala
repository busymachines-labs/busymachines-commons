package busymachines.json_test.semiauto

import busymachines.json_test._
import org.scalatest.FlatSpec


/**
  * Here we test [[busymachines.json.Decoder]] derivation
  *
  * See the [[Melon]] hierarchy
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 09 Aug 2017
  *
  */
class JsonDefaultSemiAutoDecoderDerivationTest extends FlatSpec {

  import busymachines.json.syntax._
  import melonsDefaultSemiAutoDecoders._

  //-----------------------------------------------------------------------------------------------

  it should "... be able to deserialize anarchist melon (i.e. not part of any hierarchy)" in {
    val anarchistMelon = AnarchistMelon(noGods = true, noMasters = true, noSuperTypes = true)
    val rawJson =
      """
        |{
        |  "noGods" : true,
        |  "noMasters" : true,
        |  "noSuperTypes" : true
        |}
      """.stripMargin.trim

    val read = rawJson.unsafeDecodeAs[AnarchistMelon]
    assertResult(anarchistMelon)(read)
  }


  //-----------------------------------------------------------------------------------------------

  it should "... fail to compile when there is no explicitly defined decoder for a type down in the hierarchy" in {
    assertDoesNotCompile(
      """
        |val rawJson = "{}"
        |rawJson.unsafeDecodeAs[WinterMelon]
      """.stripMargin
    )
  }

  //-----------------------------------------------------------------------------------------------

  it should "... be able to deserialize case class from hierarchy when it is referred to as its super-type" in {
    val winterMelon: Melon = WinterMelon(fuzzy = true, weight = 45)
    val rawJson =
      """
        |{
        |  "fuzzy" : true,
        |  "weight" : 45,
        |  "_type" : "WinterMelon"
        |}
      """.stripMargin.trim

    val read = rawJson.unsafeDecodeAs[Melon]
    assertResult(winterMelon)(read)
  }

  //-----------------------------------------------------------------------------------------------

  it should "... be able to deserialize case objects of the hierarchy" in {
    val smallMelon: Melon = SmallMelon
    val rawJson =
      """
        |{
        |  "_type" : "SmallMelon"
        |}
      """.stripMargin.trim
    val read = rawJson.unsafeDecodeAs[Melon]
    assertResult(smallMelon)(read)
  }

  //-----------------------------------------------------------------------------------------------

  it should "... deserialize hierarchies of case objects as enums (i.e. plain strings)" in {
    val taste: List[Taste] = List(SweetTaste, SourTaste)
    val rawJson =
      """
        |[
        |  "SweetTaste",
        |  "SourTaste"
        |]
      """.stripMargin.trim

    val read = rawJson.unsafeDecodeAs[List[Taste]]
    assertResult(read)(taste)
  }

  //-----------------------------------------------------------------------------------------------

  it should "... deserialize list of all case classes from the hierarchy" in {
    val winterMelon: Melon = WinterMelon(fuzzy = true, weight = 45)
    val waterMelon: Melon = WaterMelon(seeds = true, weight = 90)
    val smallMelon: Melon = SmallMelon
    val squareMelon: Melon = SquareMelon(weight = 10, tastes = Seq(SourTaste, SweetTaste))
    val melons = List[Melon](winterMelon, waterMelon, smallMelon, squareMelon)

    val rawJson =
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

    val read: List[Melon] = rawJson.unsafeDecodeAs[List[Melon]]
    assertResult(melons)(read)
  }

  //-----------------------------------------------------------------------------------------------

  //-----------------------------------------------------------------------------------------------

  //-----------------------------------------------------------------------------------------------

  //-----------------------------------------------------------------------------------------------

  //-----------------------------------------------------------------------------------------------
}




