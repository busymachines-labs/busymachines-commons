package busymachines.json_test.autoderive

import busymachines.json_test._
import org.scalatest.FlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Oct 2017
  *
  */
class JsonAutoDerivationNestedTypesTest extends FlatSpec {

  import busymachines.json._
  import busymachines.json.syntax._
  import busymachines.json.autoderive._

  val outdoorMelon: OutdoorMelon = OutdoorMelons.WildMelon(
    weight = 42,
    color = OutdoorMelons.Colors.Green
  )

  //-----------------------------------------------------------------------------------------------

  it should "... autoderive for case classes defined within objects" in {
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

  it should "... autoderive for case classes defined within objects with explicit enumerationEncoder" in {
    implicit val color: Codec[OutdoorMelons.Color] = derive.enumerationCodec[OutdoorMelons.Color]

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
