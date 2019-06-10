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
  * Here we test [[busymachines.json.Codec]] derivation
  *
  * See the [[Melon]] hierarchy
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
class JsonDefaultSemiAutoCodecDerivationTest extends AnyFlatSpec {

  import busymachines.json.syntax._
  import melonsDefaultSemiAutoCodecs._

  //-----------------------------------------------------------------------------------------------

  it should "... be able to serialize/deserialize anarchist melon (i.e. not part of any hierarchy)" in {
    val anarchistMelon = AnarchistMelon(noGods = true, noMasters = true, noSuperTypes = true)
    val asJson         = anarchistMelon.asJson.spaces2
    val read           = asJson.unsafeDecodeAs[AnarchistMelon]
    assertResult(anarchistMelon)(read)
  }

  //-----------------------------------------------------------------------------------------------

  it should "... fail to compile when there is no explicitly defined codec for a type down in the hierarchy" in {
    withClue("... decoding part") {
      assertDoesNotCompile(
        """
          |val rawJson = "{}"
          |rawJson.unsafeDecodeAs[WinterMelon]
        """.stripMargin,
      )
    }

    withClue("... encoding part") {
      assertDoesNotCompile(
        """
          |val winterMelon: WinterMelon = WinterMelon(fuzzy = true, weight = 45)
          |winterMelon.asJson
        """.stripMargin,
      )
    }

  }

  //-----------------------------------------------------------------------------------------------

  it should "... be able to serialize/deserialize a case class from hierarchy when it is referred to as its super-type" in {
    val winterMelon: Melon = WinterMelon(fuzzy = true, weight = 45)
    val rawJson = winterMelon.asJson.spaces2
    val read    = rawJson.unsafeDecodeAs[Melon]
    assertResult(winterMelon)(read)
  }

  //-----------------------------------------------------------------------------------------------

  it should "... be able to deserialize case objects of the hierarchy" in {
    val smallMelon: Melon = SmallMelon
    val rawJson = smallMelon.asJson.spaces2
    val read    = rawJson.unsafeDecodeAs[Melon]
    assertResult(smallMelon)(read)
  }

  //-----------------------------------------------------------------------------------------------

  it should "... deserialize hierarchies of case objects as enums (i.e. plain strings)" in {
    val taste: List[Taste] = List(SweetTaste, SourTaste)
    val rawJson = taste.asJson.spaces2
    val read    = rawJson.unsafeDecodeAs[List[Taste]]
    assertResult(read)(taste)
  }

  //-----------------------------------------------------------------------------------------------

  it should "... deserialize list of all case classes from the hierarchy" in {
    val winterMelon: Melon = WinterMelon(fuzzy = true, weight = 45)
    val waterMelon:  Melon = WaterMelon(seeds = true, weight = 90)
    val smallMelon:  Melon = SmallMelon
    val squareMelon: Melon = SquareMelon(weight = 10, tastes = Seq(SourTaste, SweetTaste))
    val melons = List[Melon](winterMelon, waterMelon, smallMelon, squareMelon)

    val rawJson = melons.asJson.spaces2
    val read: List[Melon] = rawJson.unsafeDecodeAs[List[Melon]]
    assertResult(melons)(read)
  }

  //-----------------------------------------------------------------------------------------------

}
