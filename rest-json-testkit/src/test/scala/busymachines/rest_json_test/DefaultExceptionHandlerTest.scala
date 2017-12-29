package busymachines.rest_json_test

import busymachines.core._
import busymachines.rest._
import busymachines.rest_json_test.routes_to_test._
import org.scalatest.FlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 06 Sep 2017
  *
  */
class DefaultExceptionHandlerTest extends FlatSpec with JsonRestAPITest {
  override implicit val testedRoute: Route                                    = RestAPI.seal(defApi).route
  implicit lazy val context:         CallerContext                            = Contexts.none
  private lazy val defApi:           DefaultExceptionHandlerRestAPIForTesting = new DefaultExceptionHandlerRestAPIForTesting()

  import SomeTestDTOJsonCodec._
  import busymachines.json.AnomalyJsonCodec._

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
      val fm = responseAs[Anomaly]
      assert(fm.id == AnomalyID("1"))
    }
  }

  //===========================================================================

  it should "return 403 for Denied" in {
    get("/denied") {
      expectStatus(StatusCodes.Forbidden)
      val fm = responseAs[Anomaly]
      assert(fm.id == AnomalyID("3"))
    }
  }

  //===========================================================================

  it should "return 400 for InvalidInput" in {
    get("/invalid_input") {
      expectStatus(StatusCodes.BadRequest)
      val fm = responseAs[Anomaly]
      assert(fm.id == AnomalyID("4"))
    }
  }

  //===========================================================================

  it should "return 400 for InvalidInput — multiple failures" in {
    get("/multiple_failures") {
      expectStatus(StatusCodes.BadRequest)
      val fm = responseAs[Anomalies]
      assert(fm.id == AnomalyID("1234"))
      assert(fm.messages.size == 6)
    }
  }

  //===========================================================================

  it should "return 409 for Conflict" in {
    get("/conflict") {
      expectStatus(StatusCodes.Conflict)
      val fm = responseAs[Anomaly]
      assert(fm.id == AnomalyID("5"))
    }
  }

  //===========================================================================

  it should "return 500 for InconsistentStateFailure" in {
    get("/inconsistent_state") {
      expectStatus(StatusCodes.InternalServerError)
      val fm = responseAs[Anomaly]
      assert(fm.id == AnomalyID("IS_0"))
    }
  }

  //===========================================================================

  it should "return 500 for RuntimeException" in {
    get("/runtime_exception") {
      expectStatus(StatusCodes.InternalServerError)
      val fm = responseAs[Anomaly]
      assert(fm.id == AnomalyID("CE_0"))
    }
  }

  //===========================================================================

  it should "return 501 for NotImplemented boxed" in {
    get("/not_implemented_boxed") {
      expectStatus(StatusCodes.NotImplemented)
      val fm = responseAs[Anomaly]
      assert(fm.id == AnomalyID("CE_0"))
    }
  }

  //===========================================================================

  it should "return 501 for NotImplemented" in {
    get("/not_implemented") {
      expectStatus(StatusCodes.NotImplemented)
      val fm = responseAs[Anomaly]
      assert(fm.id == AnomalyID("CE_0"))
    }
  }

  //===========================================================================

}
