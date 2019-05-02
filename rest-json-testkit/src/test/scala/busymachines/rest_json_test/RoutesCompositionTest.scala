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
package busymachines.rest_json_test

import busymachines.core._
import busymachines.rest._
import busymachines.rest_json_test.routes_to_test._
import org.scalatest.FlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 07 Sep 2017
  *
  */
class RoutesCompositionTest extends FlatSpec with JsonRestAPITest {
  private lazy val combinedAPI: RestAPI = {
    val eh   = new DefaultExceptionHandlerRestAPIForTesting()
    val crud = new CRUDRoutesRestAPIForTesting()
    val au   = new BasicAuthenticatedRoutesRestAPIForTesting()
    RestAPI.seal(eh, crud, au)
  }

  implicit override protected val testedRoute: Route         = combinedAPI.route
  implicit private val cc:                     CallerContext = Contexts.none

  import SomeTestDTOJsonCodec._
  import busymachines.json.AnomalyJsonCodec._

  //===========================================================================

  behavior of "DefaultExceptionHandler"

  //===========================================================================

  it should "return 400 for InvalidInput" in {
    get("/invalid_input") {
      expectStatus(StatusCodes.BadRequest)
      val fm = responseAs[Anomaly]
      assert(fm.id == AnomalyID("4"))
    }
  }

  //===========================================================================

  behavior of "BasicCRUD operations"

  //===========================================================================

  it should "return 200 OK on GET" in {
    get("/crud") {
      expectStatus(StatusCodes.OK)

      val r = responseAs[Seq[SomeTestDTOGet]]
      assert(
        r == Seq(
          SomeTestDTOGet(
            int    = 1,
            string = "one",
            option = None,
          ),
          SomeTestDTOGet(
            int    = 2,
            string = "two",
            option = None,
          ),
        ),
      )
    }
  }

  //===========================================================================

  behavior of "Authentications"

  //===========================================================================

  it should "... return 200 OK when providing proper Basic authentication" in {
    context(AuthenticationsForTest.basic) { implicit cc =>
      get("/basic_authentication") {
        expectStatus(StatusCodes.OK)

        assert {
          responseAs[SomeTestDTOGet] ==
            SomeTestDTOGet(int = 42, string = "dXNlcm5hbWU6cGFzc3dvcmQ=", None)
        }
      }
    }
  }
}
