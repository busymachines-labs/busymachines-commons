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
class BasicAuthenticatedRoutesTest extends FlatSpec with JsonRestAPITest {
  override implicit protected lazy val testedRoute: Route = {
    val authAPI = new BasicAuthenticatedRoutesRestAPIForTesting()
    RestAPI.seal(authAPI).route
  }

  import SomeTestDTOJsonCodec._

  //===========================================================================

  behavior of "Basic Authentication"

  //===========================================================================

  it should "... return 401 Unauthorized when trying to access route without authentication" in {
    context(Contexts.none) { implicit cc =>
      get("/basic_authentication") {
        expectStatus(StatusCodes.Unauthorized)
      }
    }
  }

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

  //===========================================================================

  it should "... return 200 OK when trying to access API with optional auth, while not providing it" in {
    context(Contexts.none) { implicit cc =>
      get("/basic_opt_authentication") {
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
    context(AuthenticationsForTest.basic) { implicit cc =>
      get("/basic_opt_authentication") {
        expectStatus(StatusCodes.OK)

        assert {
          responseAs[SomeTestDTOGet] ==
            SomeTestDTOGet(int = 42, string = "dXNlcm5hbWU6cGFzc3dvcmQ=", None)
        }
      }
    }
  }

  //===========================================================================
}
