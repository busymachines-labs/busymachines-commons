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

import busymachines.json_test.AnarchistMelon
import org.scalatest.{EitherValues, Matchers}
import busymachines.effects.sync._
import busymachines.json._
import org.scalatest.flatspec.AnyFlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
class JsonUtilsTest extends AnyFlatSpec with EitherValues with Matchers {

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
      JsonParsing.parseString(rawJson).unsafeGet()
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
      JsonDecoding.decodeAs[AnarchistMelon](rawJson).unsafeGet()
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
      JsonDecoding.decodeAs[AnarchistMelon](rawJson).unsafeGet()
    }
  }

  //-----------------------------------------------------------------------------------------------

}
