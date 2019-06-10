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
package busymachines.json_test.derive

import busymachines.json_test._
import org.scalatest.flatspec.AnyFlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Oct 2017
  *
  */
final class JsonDerivationNestedTypesTest1 extends AnyFlatSpec {

  import busymachines.json._
  import busymachines.json.syntax._

  val outdoorMelon: OutdoorMelon = OutdoorMelons.WildMelon(
    weight = 42,
    color  = OutdoorMelons.Colors.Green,
  )

  //-----------------------------------------------------------------------------------------------
  //moved outside of the test to avoid false positive of "implicit not used" warning
  implicit val color:             Codec[OutdoorMelons.Color] = codecs.`OutdoorMelons.Color.codec`
  implicit val outdoorMelonCodec: Codec[OutdoorMelon]        = derive.codec[OutdoorMelon]

  it should "... derive for case classes defined within objects — normal codecs" in {

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

final class JsonDerivationNestedTypesTest2 extends AnyFlatSpec {

  import busymachines.json._
  import busymachines.json.syntax._

  val outdoorMelon: OutdoorMelon = OutdoorMelons.WildMelon(
    weight = 42,
    color  = OutdoorMelons.Colors.Green,
  )

  //-----------------------------------------------------------------------------------------------
  //moved outside of the test to avoid false positive of "implicit not used" warning
  implicit val color:             Codec[OutdoorMelons.Color] = codecs.`OutdoorMelons.Color.enumerationCodec`
  implicit val outdoorMelonCodec: Codec[OutdoorMelon]        = derive.codec[OutdoorMelon]

  it should "... derive for case classes defined within objects — enumerationCodec" in {

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
