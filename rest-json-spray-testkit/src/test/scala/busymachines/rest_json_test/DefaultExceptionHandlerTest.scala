package busymachines.rest_json_test

import busymachines.core.exceptions._
import busymachines.rest._
import busymachines.rest_json_test.routes_to_test._
import org.scalatest.FlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 06 Sep 2017
  *
  */
private[rest_json_test] class DefaultExceptionHandlerTest extends FlatSpec with JsonRestAPITest {
  private lazy val defApi = new DefaultExceptionHandlerRestAPIForTesting()
  override implicit val testedRoute: Route = RestAPI.seal(defApi).route
  implicit val context: CallerContext = Contexts.none

  import SomeTestDTOJsonCodec._
  import busymachines.json.FailureMessageJsonCodec._

  behavior of "DefaultExceptionHandler"

  //===========================================================================

  it should "return 404 for unknown route" in {
    get("/this_does_not_exist") {
      expectStatus(StatusCodes.NotFound)
    }
  }

  //===========================================================================

  it should "return 200 for normal_route" in {
    get("/normal_route") {
      expectStatus(StatusCodes.OK)
      assert {
        responseAs[SomeTestDTOGet] ==
          SomeTestDTOGet(
            42,
            "fortyTwo",
            None
          )
      }
    }
  }

  //===========================================================================

  it should "return 404 for NotFoundFailure" in {
    get("/not_found") {
      expectStatus(StatusCodes.NotFound)
    }
  }

  //===========================================================================

  it should "return 404 for NoAccessFailure " in {
    get("/no_access") {
      expectStatus(StatusCodes.NotFound)
    }
  }

  //===========================================================================

  it should "return 401 for Unauthorized" in {
    get("/unauthorized") {
      expectStatus(StatusCodes.Unauthorized)
      val fm = responseAs[FailureMessage]
      assert(fm.id == FailureID("1"))
    }
  }

  //===========================================================================

  it should "return 403 for Denied" in {
    get("/denied") {
      expectStatus(StatusCodes.Forbidden)
      val fm = responseAs[FailureMessage]
      assert(fm.id == FailureID("3"))
    }
  }

  //===========================================================================

  it should "return 400 for InvalidInput" in {
    get("/invalid_input") {
      expectStatus(StatusCodes.BadRequest)
      val fm = responseAs[FailureMessage]
      assert(fm.id == FailureID("4"))
    }
  }

  //===========================================================================

  it should "return 409 for Conflict" in {
    get("/conflict") {
      expectStatus(StatusCodes.Conflict)
      val fm = responseAs[FailureMessage]
      assert(fm.id == FailureID("5"))
    }
  }

  //===========================================================================

  it should "return 500 for InconsistentStateFailure" in {
    get("/inconsistent_state") {
      expectStatus(StatusCodes.InternalServerError)
      val fm = responseAs[FailureMessage]
      assert(fm.id == FailureID("is_state"))
    }
  }

  //===========================================================================

  it should "return 500 for RuntimeException" in {
    get("/runtime_exception") {
      expectStatus(StatusCodes.InternalServerError)
      val fm = responseAs[FailureMessage]
      assert(fm.id == FailureID("error"))
    }
  }

  //===========================================================================

  it should "return 501 for NotImplemented boxed" in {
    get("/not_implemented_boxed") {
      expectStatus(StatusCodes.NotImplemented)
      val fm = responseAs[FailureMessage]
      assert(fm.id == FailureID("error"))
    }
  }

  //===========================================================================

  it should "return 501 for NotImplemented" in {
    get("/not_implemented") {
      expectStatus(StatusCodes.NotImplemented)
      val fm = responseAs[FailureMessage]
      assert(fm.id == FailureID("error"))
    }
  }

  //===========================================================================

}
