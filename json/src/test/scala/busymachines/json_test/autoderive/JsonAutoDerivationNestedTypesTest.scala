/**
  * Copyright (c) 2017-2018 BusyMachines
  *
  * See company homepage at: https://www.busymachines.com/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package busymachines.json_test.autoderive

import busymachines.json_test._
import org.scalatest.FlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Oct 2017
  *
  */
class JsonAutoDerivationNestedTypesTest1 extends FlatSpec {

  import busymachines.json._
  import busymachines.json.syntax._
  import busymachines.json.autoderive._

  val outdoorMelon: OutdoorMelon = OutdoorMelons.WildMelon(
    weight = 42,
    color  = OutdoorMelons.Colors.Green,
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
}

final class JsonAutoDerivationNestedTypesTest2 extends FlatSpec {

  import busymachines.json._
  import busymachines.json.syntax._
  import busymachines.json.autoderive._

  val outdoorMelon: OutdoorMelon = OutdoorMelons.WildMelon(
    weight = 42,
    color  = OutdoorMelons.Colors.Green,
  )

  //-----------------------------------------------------------------------------------------------

  //moved here to avoid false positive "implicit not used" warning if put in the scope of the test
  implicit val color: Codec[OutdoorMelons.Color] = codecs.`OutdoorMelons.Color.enumerationCodec`

  it should "... autoderive for case classes defined within objects with explicit enumerationEncoder" in {

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
