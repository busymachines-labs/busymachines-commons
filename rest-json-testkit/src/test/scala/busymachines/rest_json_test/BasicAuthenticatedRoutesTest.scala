package busymachines.rest_json_test

import busymachines.rest._
import busymachines.rest_json_test.routes_to_test._
import org.scalatest.Outcome

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 07 Sep 2017
  *
  */
private[rest_json_test] class BasicAuthenticatedRoutesTest extends ExampleRestAPITestBaseClassWithFixture {

  /**
    * A fixture would have to be more complicated than this to warrant all the hassle.
    * But this is here for illustrative purposes
    *
    */
  override protected def withFixture(test: OneArgTest): Outcome = {
    val authAPI = new BasicAuthenticatedRoutesRestAPIForTesting()
    val r: RestAPI = RestAPI.seal(authAPI)
    this._testedRoute = r.route
    test(r)
  }

  import busymachines.json._
  import busymachines.json.autoderive._
  //  this also works, and gets us faster compilation times:
  //  import SomeTestDTOJsonCodec._

  //===========================================================================

  behavior of "Basic Authentication"

  //===========================================================================

  it should "... return 401 Unauthorized when trying to access route without authentication" in { _ =>
    context(Contexts.none) { implicit cc =>
      get("/basic_authentication") {
        expectStatus(StatusCodes.Unauthorized)
      }
    }
  }

  //===========================================================================

  it should "... return 200 OK when providing proper Basic authentication" in { _ =>
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

  it should "... return 200 OK when trying to access API with optional auth, while not providing it" in { _ =>
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

  it should "... return 200 OK when trying to access API with optional auth, while providing it" in { _ =>
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
