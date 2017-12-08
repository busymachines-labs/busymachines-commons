package busymachines.rest_json_test

import busymachines.core.exceptions._
import busymachines.rest._
import busymachines.rest_json_test.routes_to_test._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 07 Sep 2017
  *
  */
private[rest_json_test] class RoutesCompositionTest extends ExampleRestAPITestBaseClass {
  private lazy val combinedAPI: RestAPI = {
    val eh   = new DefaultExceptionHandlerRestAPIForTesting()
    val crud = new CRUDRoutesRestAPIForTesting()
    val au   = new BasicAuthenticatedRoutesRestAPIForTesting()
    RestAPI.seal(eh, crud, au)
  }

  override implicit protected val testedRoute: Route         = combinedAPI.route
  private implicit val cc:                     CallerContext = Contexts.none

  import SomeTestDTOJsonCodec._
  import busymachines.json.FailureMessageJsonCodec._

  //===========================================================================

  behavior of "DefaultExceptionHandler"

  //===========================================================================

  it should "return 400 for InvalidInput" in {
    get("/invalid_input") {
      expectStatus(StatusCodes.BadRequest)
      val fm = responseAs[FailureMessage]
      assert(fm.id == FailureID("4"))
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
            option = None
          ),
          SomeTestDTOGet(
            int    = 2,
            string = "two",
            option = None
          )
        )
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
