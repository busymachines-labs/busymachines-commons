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
private[rest_json_test] class BearerAuthenticatedRoutesTest extends FlatSpec with JsonRestAPITest {

  private lazy val bearerAPI = new BearerAuthenticatedRoutesRestAPIForTesting()
  override implicit val testedRoute: Route = RestAPI.seal(bearerAPI).route

  import SomeTestDTOJsonCodec._

  //===========================================================================

  behavior of "Bearer Authentication"

  //===========================================================================

  it should "... return 401 Unauthorized when trying to access route without authentication" in {
    context(Contexts.none) { implicit cc =>
      get("/bearer_authentication") {
        expectStatus(StatusCodes.Unauthorized)
      }
    }
  }

  //===========================================================================

  it should "... return 200 OK when providing proper Basic authentication" in {
    context(AuthenticationsForTest.bearer) { implicit cc =>
      get("/bearer_authentication") {
        expectStatus(StatusCodes.OK)

        assert {
          responseAs[SomeTestDTOGet] ==
            SomeTestDTOGet(int = 42, string = "D2926169E98AAA4C6B40C8C7AF7F4122946DDFA4E499908C", None)
        }
      }
    }
  }

  //===========================================================================

  it should "... return 200 OK when trying to access API with optional auth, while not providing it" in {
    context(Contexts.none) { implicit cc =>
      get("/bearer_opt_authentication") {
        expectStatus(StatusCodes.OK)

        assert {
          responseAs[SomeTestDTOGet] ==
            SomeTestDTOGet(int = 42, string = "it's optional!", None)
        }
      }
    }
  }

  //===========================================================================

  it should "... return 200 OK when trying to access API with optional auth, while providing it" in {
    context(AuthenticationsForTest.bearer) { implicit cc =>
      get("/bearer_opt_authentication") {
        expectStatus(StatusCodes.OK)

        assert {
          responseAs[SomeTestDTOGet] ==
            SomeTestDTOGet(int = 42, string = "D2926169E98AAA4C6B40C8C7AF7F4122946DDFA4E499908C", None)
        }
      }
    }
  }

  //===========================================================================
}
