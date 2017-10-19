package busymachines.rest_test

import busymachines.rest._
import busymachines.rest_test.routes._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 07 Sep 2017
  *
  */
private[rest_test] class CRUDRoutesTest extends ExampleRestAPITestBaseClass {
  private lazy val crudAPI = new CRUDRoutesRestAPIForTesting()
  override implicit val testedRoute: Route = RestAPI.seal(crudAPI).route
  private implicit val cc: CallerContext = Contexts.none

  import busymachines.json._

  //===========================================================================

  behavior of "BasicCRUD operations"

  //===========================================================================

  it should "return 200 OK on GET" in {
    get("/crud") {
      expectStatus(StatusCodes.OK)

      assert {
        responseAs[Seq[SomeTestDTOGet]] == Seq(
          SomeTestDTOGet(
            int = 1,
            string = "one",
            option = None
          ),
          SomeTestDTOGet(
            int = 2,
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

  //===========================================================================

  it should "return 200 OK on PUT" in {
    val p = SomeTestDTOPut(
      "lalala",
      Option(42)
    )
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

  //===========================================================================

  it should "return 200 OK on PATCH" in {
    val p = SomeTestDTOPatch(
      "lalala"
    )
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

  //===========================================================================

  it should "return 204 NoContent on DELETE" in {
    delete("/crud/77") {
      expectStatus(StatusCodes.NoContent)
    }
  }

  //===========================================================================

}
