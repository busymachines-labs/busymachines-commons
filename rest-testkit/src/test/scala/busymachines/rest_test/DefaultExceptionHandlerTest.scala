package busymachines.rest_test

import akka.http.scaladsl.server.Route
import busymachines.core.exceptions._
import busymachines.rest._
import busymachines.rest_test.routes._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 06 Sep 2017
  *
  */
class DefaultExceptionHandlerTest extends RestAPITest with JsonSupport {
  private lazy val defApi = new DefaultExceptionHandlerRestAPIForTesting()
  override implicit val testedRoute: Route = Route.seal(defApi.route)
  implicit val context: CallerContext = Contexts.none

  import busymachines.json.FailureMessageJsonCodec._
  import busymachines.json.auto._

  behavior of "DefaultExceptionHandler"

  //===========================================================================

  it should "return 404 for unknown route" in {
    get("/this_does_not_exist") {
      assert(response.status == StatusCodes.NotFound)
    }
  }

  //===========================================================================

  it should "return 200 for normal_route" in {
    get("/normal_route") {
      assert(response.status == StatusCodes.OK)
      assert(
        responseAs[SomeTestDTOGet] == SomeTestDTOGet(
          42,
          "fortyTwo",
          None
        )
      )
    }
  }

  //===========================================================================

  it should "return 404 for NotFoundFailure" in {
    get("/not_found") {
      assert(response.status == StatusCodes.NotFound)
    }
  }

  //===========================================================================

  it should "return 404 for NoAccessFailure " in {
    get("/no_access") {
      assert(response.status == StatusCodes.NotFound)
    }
  }

  //===========================================================================

  it should "return 401 for Unauthorized" in {
    get("/unauthorized") {
      assert(response.status == StatusCodes.Unauthorized)
      val fm = responseAs[FailureMessage]
      assert(fm.id == FailureID("1"))
    }
  }

  //===========================================================================

  it should "return 403 for Denied" in {
    get("/denied") {
      assert(response.status == StatusCodes.Forbidden)
      val fm = responseAs[FailureMessage]
      assert(fm.id == FailureID("3"))
    }
  }

  //===========================================================================

  it should "return 400 for InvalidInput" in {
    get("/invalid_input") {
      assert(response.status == StatusCodes.BadRequest)
      val fm = responseAs[FailureMessage]
      assert(fm.id == FailureID("4"))
    }
  }

  //===========================================================================

  it should "return 409 for Conflict" in {
    get("/conflict") {
      assert(response.status == StatusCodes.Conflict)
      val fm = responseAs[FailureMessage]
      assert(fm.id == FailureID("5"))
    }
  }

  //===========================================================================

  it should "return 500 for InconsistentStateFailure" in {
    get("/inconsistent_state") {
      assert(response.status == StatusCodes.InternalServerError)
      val fm = responseAs[FailureMessage]
      assert(fm.id == FailureID("is_state"))
    }
  }

  //===========================================================================

  it should "return 500 for RuntimeException" in {
    get("/runtime_exception") {
      assert(response.status == StatusCodes.InternalServerError)
      val fm = responseAs[FailureMessage]
      assert(fm.id == FailureID("error"))
    }
  }

  //===========================================================================

  it should "return 501 for NotImplemented boxed" in {
    get("/not_implemented_boxed") {
      assert(response.status == StatusCodes.NotImplemented)
      val fm = responseAs[FailureMessage]
      assert(fm.id == FailureID("error"))
    }
  }

  //===========================================================================

  it should "return 501 for NotImplemented" in {
    get("/not_implemented") {
      assert(response.status == StatusCodes.NotImplemented)
      val fm = responseAs[FailureMessage]
      assert(fm.id == FailureID("error"))
    }
  }

  //===========================================================================


}
