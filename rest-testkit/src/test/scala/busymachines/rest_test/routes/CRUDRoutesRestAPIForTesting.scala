package busymachines.rest_test.routes

import busymachines.rest._

import scala.concurrent.Future

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 07 Sep 2017
  *
  */
private[rest_test] class CRUDRoutesRestAPIForTesting extends RestAPI with Directives {

  import busymachines.rest.JsonSupport._
  import busymachines.json.auto._

  override protected def routeDefinition: Route =
    pathPrefix("crud") {
      pathEndOrSingleSlash {
        post {
          entity(as[SomeTestDTOPost]) { p =>
            val response = SomeTestDTOGet(
              int = 42,
              string = p.string,
              option = p.option
            )
            complete(StatusCodes.Created, Future.successful(response))
          }
        } ~ get {
          val response = Seq(
            SomeTestDTOGet(
              int = 1,
              string = "one",
              option = None
            ),
            SomeTestDTOGet(
              int = 2,
              string = "two",
              option = None
            )
          )

          complete(Future.successful(response))
        }
      } ~ path(IntNumber) { id =>
        get {
          val response = SomeTestDTOGet(
            int = id,
            string = "wabbalubbadubdub",
            option = Option(42)
          )
          complete(Future.successful(response))
        } ~ put {
          entity(as[SomeTestDTOPut]) { p =>
            val response = SomeTestDTOGet(
              int = id,
              string = p.string,
              option = p.option
            )
            complete(Future.successful(response))
          }
        } ~ patch {
          entity(as[SomeTestDTOPatch]) { p =>
            val response = SomeTestDTOGet(
              int = id,
              string = p.string,
              option = None
            )
            complete(Future.successful(response))
          }
        } ~ delete {
          complete(StatusCodes.NoContent, Future.successful(()))
        }
      }
    }
}