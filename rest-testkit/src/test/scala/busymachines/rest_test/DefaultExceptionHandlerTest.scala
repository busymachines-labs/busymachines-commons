package busymachines.rest_test

import akka.http.scaladsl.server.Route
import busymachines.rest._
import busymachines.rest_test.routes._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 06 Sep 2017
  *
  */
class DefaultExceptionHandlerTest extends RestAPITest {
  override implicit val testedRoute: Route = RestAPI.seal(new DefaultExceptionHandlerRestApi()).route

  behavior of "DefaultExceptionHandler"

  it should "return 404 for NotFound" in {
    debug()
    get("not_found") {
      assert(response.status == StatusCodes.NotFound)
    }

  }
}
