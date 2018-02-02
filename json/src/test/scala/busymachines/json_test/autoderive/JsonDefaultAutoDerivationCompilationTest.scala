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
  * Here we test [[busymachines.json.autoderive]] derivation at compile time,
  * mostly to show what imports are required, and which are not.
  *
  * See the [[Melon]] hierarchy
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
class JsonDefaultAutoDerivationCompilationTest extends FlatSpec {

  //-----------------------------------------------------------------------------------------------

  it should "... compile when having the correct imports" in {
    assertCompiles {
      """
        |import busymachines.json._
        |import busymachines.json.autoderive._
        |
        |import busymachines.json.syntax._
        |
        |val anarchistMelon = AnarchistMelon(noGods = true, noMasters = true, noSuperTypes = true)
        |val asJson = anarchistMelon.asJson.spaces2
        |val read = asJson.unsafeDecodeAs[AnarchistMelon]
        |
      """.stripMargin
    }
  }

  //-----------------------------------------------------------------------------------------------

  it should "... compile when importing everything — autoderive._ is imported together with derive._ together with syntax" in {
    assertCompiles {
      """
        |import busymachines.json._
        |import busymachines.json.autoderive._
        |import busymachines.json.syntax._
        |import busymachines.json.derive._
        |
        |val anarchistMelon = AnarchistMelon(noGods = true, noMasters = true, noSuperTypes = true)
        |val asJson = anarchistMelon.asJson.spaces2
        |val read = asJson.unsafeDecodeAs[AnarchistMelon]
        |
        |//I really put this line here so that the -unused-imports compiler flag doesn't complain for this test
        |val leaveMeAloneUnusedImports = encoder[AnarchistMelon]
        |
      """.stripMargin
    }
  }

  //-----------------------------------------------------------------------------------------------

}
