package busymachines.rest_test.routes

import busymachines.rest._

import scala.concurrent.Future

/**
  *
  * You have several options of bringing in your json. Ordered from slowest compile time to fastest:
  * ==========================
  * 1:
  * {{{
  *   busymachines.json._
  *   class AuthenticatedRoutesRestAPIForTesting ... with JsonSupport
  * }}}
  *
  * ==========================
  * 2:
  * {{{
  *   import SomeTestDTOJsonCodec._ //this already extends JsonSupport
  * }}}
  * ==========================
  * 3:
  * {{{
  *   class AuthenticatedRoutesRestAPIForTesting ... with SomeTestDTOJsonCodec
  * }}}
  * ==========================
  *
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 07 Sep 2017
  *
  */
private[rest_test] class AuthenticatedRoutesRestAPIForTesting extends JsonRestAPI with Directives with SomeTestDTOJsonCodec
  with RestAPIAuthentications.Basic {

  //  Alternantively, if you remove SomeTestDTOJsonCodec mixing
  //  import busymachines.rest.JsonSupport._
  //  import busymachines.json._

  //Alternatively, if none of the above:
  //import SomeTestDTOJsonCodec._

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
