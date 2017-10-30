package busymachines.json_test.derive

import busymachines.json_test._
import org.scalatest.FlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Oct 2017
  *
  */
class JsonDerivationNestedTypesTest extends FlatSpec {

  import busymachines.json._
  import busymachines.json.syntax._

  val outdoorMelon: OutdoorMelon = OutdoorMelons.WildMelon(
    weight = 42,
    color = OutdoorMelons.Colors.Green
  )

  //-----------------------------------------------------------------------------------------------

  it should "... derive for case classes defined within objects — normal codecs" in {
    implicit val color: Codec[OutdoorMelons.Color] = derive.codec[OutdoorMelons.Color]
    implicit val outdoorMelonCodec: Codec[OutdoorMelon] = derive.codec[OutdoorMelon]

    val stringyJson =
      """
        |{
        |  "weight" : 42,
        |  "color" : {
        |    "_type" : "Green"
        |  },
        |  "_type" : "WildMelon"
        |}
      """.stripMargin.trim

    val json = outdoorMelon.asJson

    assert(stringyJson == json.spaces2NoNulls, "encoder")
    assert(outdoorMelon == stringyJson.unsafeDecodeAs[OutdoorMelon], "decoder")
  }

  //-----------------------------------------------------------------------------------------------

  it should "... derive for case classes defined within objects — enumerationCodec" in {
    implicit val color: Codec[OutdoorMelons.Color] = derive.enumerationCodec[OutdoorMelons.Color]
    implicit val outdoorMelonCodec: Codec[OutdoorMelon] = derive.codec[OutdoorMelon]

    val stringyJson =
      """
        |{
        |  "weight" : 42,
        |  "color" : "Green",
        |  "_type" : "WildMelon"
        |}
      """.stripMargin.trim

    val json = outdoorMelon.asJson

    assert(stringyJson == json.spaces2NoNulls, "encoder")
    assert(outdoorMelon == stringyJson.unsafeDecodeAs[OutdoorMelon], "decoder")
  }

  //-----------------------------------------------------------------------------------------------

}
