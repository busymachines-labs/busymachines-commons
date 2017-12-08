package busymachines.rest_json_spray_test

import busymachines.core.exceptions._
import busymachines.rest._
import busymachines.rest_json_spray_test.routes_to_test._
import org.scalatest.FlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 07 Sep 2017
  *
  */
private[rest_json_spray_test] class RoutesCompositionTest
    extends FlatSpec with JsonRestAPITest with SomeTestDTOJsonCodec {
  private lazy val combinedAPI: RestAPI = {
    val eh   = new DefaultExceptionHandlerRestAPIForTesting()
    val crud = new CRUDRoutesRestAPIForTesting()
    RestAPI.seal(eh, crud)
  }

  override implicit protected val testedRoute: Route         = combinedAPI.route
  private implicit val cc:                     CallerContext = Contexts.none

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

}
