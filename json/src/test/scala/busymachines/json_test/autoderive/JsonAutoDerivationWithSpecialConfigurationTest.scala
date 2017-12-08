package busymachines.json_test.autoderive

import busymachines.json_test._
import org.scalatest.FlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
class JsonAutoDerivationWithSpecialConfigurationTest extends FlatSpec {

  import busymachines.json.syntax._

  /**
    * unfortunately this exclusion is absolutely necessary if you want to use the non-default _type
    * discriminator for sealed hierarchies of classes AND auto-derivation at the same time
    */
  import busymachines.json.{defaultDerivationConfiguration => _, _}
  import busymachines.json.autoderive._

  final implicit val _melonManiaDiscriminatorConfig: Configuration =
    Configuration.default.withDiscriminator("_melonMania")

  it should "... type discriminator should be _melonMania, and case object hierarchy codec should be string only" in {

    /**
      * We explicitly create a codec for the enumeration of case objects, thus, it should not have _melonMania discriminator
      */
    implicit val explicitImplicitTasteCodec: Codec[Taste] = derive.enumerationCodec[Taste]

    val winterMelon: Melon = WinterMelon(fuzzy = true, weight = 45)
    val waterMelon:  Melon = WaterMelon(seeds = true, weight = 90)
    val smallMelon:  Melon = SmallMelon
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
        |    "_melonMania" : "WinterMelon"
        |  },
        |  {
        |    "seeds" : true,
        |    "weight" : 90,
        |    "_melonMania" : "WaterMelon"
        |  },
        |  {
        |    "_melonMania" : "SmallMelon"
        |  },
        |  {
        |    "weight" : 10,
        |    "tastes" : [
        |      "SourTaste",
        |      "SweetTaste"
        |    ],
        |    "_melonMania" : "SquareMelon"
        |  }
        |]
      """.stripMargin.trim
    )(rawJson)

    val read: List[Melon] = rawJson.unsafeDecodeAs[List[Melon]]
    assertResult(melons)(read)
  }

  it should "... type discriminator should be _melonMania, and case object hierarchy serialization should also be affected by this new configuration" in {
    val winterMelon: Melon = WinterMelon(fuzzy = true, weight = 45)
    val waterMelon:  Melon = WaterMelon(seeds = true, weight = 90)
    val smallMelon:  Melon = SmallMelon
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
        |    "_melonMania" : "WinterMelon"
        |  },
        |  {
        |    "seeds" : true,
        |    "weight" : 90,
        |    "_melonMania" : "WaterMelon"
        |  },
        |  {
        |    "_melonMania" : "SmallMelon"
        |  },
        |  {
        |    "weight" : 10,
        |    "tastes" : [
        |      {
        |        "_melonMania" : "SourTaste"
        |      },
        |      {
        |        "_melonMania" : "SweetTaste"
        |      }
        |    ],
        |    "_melonMania" : "SquareMelon"
        |  }
        |]
      """.stripMargin.trim
    )(rawJson)

    val read: List[Melon] = rawJson.unsafeDecodeAs[List[Melon]]
    assertResult(melons)(read)
  }

}
