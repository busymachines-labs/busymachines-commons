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

import busymachines.rest._
import busymachines.rest_json_test.routes_to_test._
import org.scalatest.FlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 07 Sep 2017
  *
  */
class CRUDRoutesTest extends FlatSpec with JsonRestAPITest {
  private lazy val crudAPI:          CRUDRoutesRestAPIForTesting = new CRUDRoutesRestAPIForTesting()
  implicit override val testedRoute: Route                       = RestAPI.seal(crudAPI).route
  implicit private val cc:           CallerContext               = Contexts.none

  import SomeTestDTOJsonCodec._

  //===========================================================================

  behavior of "BasicCRUD operations"

  //===========================================================================

  it should "return 200 OK on GET" in {
    get("/crud") {
      expectStatus(StatusCodes.OK)

      assert {
        responseAs[Seq[SomeTestDTOGet]] == Seq(
          SomeTestDTOGet(
            int    = 1,
            string = "one",
            option = None
          ),
          SomeTestDTOGet(
            int    = 2,
            string = "two",
            option = None
          )
        )
      }
    }
  }

  //===========================================================================

  it should "return 200 OK on GET by ID" in {
    get("/crud/55") {
      expectStatus(StatusCodes.OK)

      assert {
        responseAs[SomeTestDTOGet] ==
          SomeTestDTOGet(55, "wabbalubbadubdub", Option(42))
      }
    }
  }

  //===========================================================================

  it should "return 201 Created on POST" in {
    val p = SomeTestDTOPost(
      "lalala",
      None
    )

    withClue("... typed post") {
      post("/crud", p) {
        assert {
          responseAs[SomeTestDTOGet] ==
            SomeTestDTOGet(
              42,
              "lalala",
              None
            )
        }
      }
    }

    withClue("... raw post") {
      postRaw("/crud")(
        """
          |{
          |  "string" : "lalala"
          |}
        """.stripMargin
      ) {
        assert {
          responseAs[SomeTestDTOGet] ==
            SomeTestDTOGet(
              42,
              "lalala",
              None
            )
        }
      }
    }

  }

  //===========================================================================

  it should "return 200 OK on PUT" in {
    val p = SomeTestDTOPut(
      string = "lalala",
      option = Option(42)
    )

    withClue("... typed PUT") {
      put("/crud/77", p) {
        expectStatus(StatusCodes.OK)

        assert {
          responseAs[SomeTestDTOGet] ==
            SomeTestDTOGet(
              77,
              "lalala",
              Option(42)
            )
        }
      }
    }

    withClue("... raw PUT") {
      putRaw("/crud/77")(
        """
          |{
          |  "string" : "lalala",
          |  "option" : "42"
          |}
        """.stripMargin
      ) {
        expectStatus(StatusCodes.OK)

        assert {
          responseAs[SomeTestDTOGet] ==
            SomeTestDTOGet(
              77,
              "lalala",
              Option(42)
            )
        }
      }
    }
  }

  //===========================================================================

  it should "return 200 OK on PATCH" in {
    val p = SomeTestDTOPatch(
      "lalala"
    )

    withClue("... typed PATCH") {
      patch("/crud/77", p) {
        expectStatus(StatusCodes.OK)

        assert {
          responseAs[SomeTestDTOGet] ==
            SomeTestDTOGet(
              77,
              "lalala",
              None
            )
        }
      }
    }

    withClue("... raw PATCH") {
      patchRaw("/crud/77")(
        """
          |{
          |  "string" : "lalala"
          |}
        """.stripMargin
      ) {
        expectStatus(StatusCodes.OK)

        assert {
          responseAs[SomeTestDTOGet] ==
            SomeTestDTOGet(
              77,
              "lalala",
              None
            )
        }
      }
    }
  }

  //===========================================================================

  it should "return 204 NoContent on DELETE" in {
    delete("/crud/77") {
      expectStatus(StatusCodes.NoContent)
    }
  }

  //===========================================================================

}
