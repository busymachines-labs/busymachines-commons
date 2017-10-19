package busymachines.json_test.auto

import busymachines.json_test._

import org.scalatest.FlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
class JsonDefaultAutoSemiAutoInteractionTest extends FlatSpec {

  import busymachines.json._
  import busymachines.json.syntax._

  it should "... auto should use the explicit codec for sub-hierarchies of Melon" in {
    implicit val explicitImplicitTasteCodec: Codec[Taste] = semiauto.deriveEnumerationCodec[Taste]

    val winterMelon: Melon = WinterMelon(fuzzy = true, weight = 45)
    val waterMelon: Melon = WaterMelon(seeds = true, weight = 90)
    val smallMelon: Melon = SmallMelon
    val squareMelon: Melon = SquareMelon(weight = 10, tastes = Seq(SourTaste, SweetTaste))
    val melons = List[Melon](winterMelon, waterMelon, smallMelon, squareMelon)

    val rawJson = melons.asJson.spaces2

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
      """.stripMargin.trim,
      "... raw json should have been obtained using the explicitImplicitTasteCodec"
    )(rawJson)

    val read: List[Melon] = rawJson.unsafeDecodeAs[List[Melon]]
    assertResult(melons)(read)
  }

}
