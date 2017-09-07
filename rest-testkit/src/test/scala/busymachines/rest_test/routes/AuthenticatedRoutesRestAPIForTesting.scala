package busymachines.rest_test.routes

import busymachines.rest._

import scala.concurrent.Future

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 07 Sep 2017
  *
  */
private[rest_test] class AuthenticatedRoutesRestAPIForTesting extends RestAPI with Directives with JsonSupport
  with RestAPIAuthentications.Basic {

  import busymachines.json.auto._

  override protected def routeDefinition: Route = {
    pathPrefix("authentication") {
      authentication { basicAuth: String =>
        pathEndOrSingleSlash {
          get {
            complete(
              StatusCodes.OK,
              Future.successful(SomeTestDTOGet(int = 42, string = basicAuth, None)))

          }
        }
      }
    } ~ pathPrefix("opt_authentication") {
      optionalAuthentication { basicAuth: Option[String] =>
        pathEndOrSingleSlash {
          get {
            complete(
              StatusCodes.OK,
              Future.successful(SomeTestDTOGet(int = 42, string = basicAuth.getOrElse("it's optional!"), None)))
          }
        }
      }
    }
  }
}
