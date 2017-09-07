package busymachines.rest_test

import busymachines.rest._
import busymachines.rest_test.routes._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 07 Sep 2017
  *
  */
private[rest_test] class AuthenticatedRoutesTest extends RestAPITest with JsonSupport {
  private lazy val authAPI = new AuthenticatedRoutesRestAPIForTesting()
  override implicit val testedRoute: Route = Route.seal(authAPI.route)

  import busymachines.json.auto._

  //===========================================================================

  behavior of "Authentications"

  //===========================================================================

  it should "... return 401 Unauthorized when trying to access route without authe" in {
    context(Contexts.none) { implicit cc =>
      get("/authentication") {
        expectStatus(StatusCodes.Unauthorized)
      }
    }
  }

  //===========================================================================

  it should "... return 200 OK when providing proper Basic authentication" in {
    context(BasicAuthenticationContextForTesting) { implicit cc =>
      get("/authentication") {
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
      get("/opt_authentication") {
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
    context(BasicAuthenticationContextForTesting) { implicit cc =>
      get("/opt_authentication") {
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
