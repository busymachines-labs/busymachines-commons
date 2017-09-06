package busymachines.rest

import Directives._
import busymachines.core.exceptions._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Aug 2017
  *
  */
trait RestAPI {

  def route: Route =
    handleExceptions(exceptionHandler)(routeDefinition)

  protected def routeDefinition: Route

  def exceptionHandler: ExceptionHandler =
    ExceptionHandler.apply(RestAPI.defaultExceptionHandler)
}

/**
  * Since [[FailureMessages]] extends [[FailureMessage]]
  * the marshaller for the superclass is being used,
  * thus we no longer have the ``messages`` field in the resulting JSON.
  *
  * thats why we segrated the scopes of the imports with the [[RestAPI.failure]]
  * and [[RestAPI.failures]] objects
  */
object RestAPI {

  import akka.http.scaladsl.marshalling.ToEntityMarshaller

  object failure {

    import busymachines.json.FailureMessageJsonCodec._
    import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

    private implicit val failureMessageMarshaller: ToEntityMarshaller[FailureMessage] =
      marshaller[FailureMessage]

    def apply(statusCode: StatusCode): Route = {
      complete(statusCode)
    }

    def apply(statusCode: StatusCode, f: FailureMessage): Route = {
      complete(statusCode, f)
    }
  }

  object failures {

    import busymachines.json.FailureMessageJsonCodec._
    import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

    private implicit val failureMessagesMarshaller: ToEntityMarshaller[FailureMessages] =
      marshaller[FailureMessages]

    def apply(statusCode: StatusCode, fs: FailureMessages): Route =
      complete(statusCode, fs)
  }

  private class ReifiedRestAPI(private val r: Route) extends RestAPI {
    override protected def routeDefinition: Route = r

    //no point in "handling exceptions again"
    override def route: Route = r
  }

  /**
    * !!!! IMPORTANT !!!!
    * Should be used only once per application
    * !!!! IMPORTANT !!!!
    *
    * Rejection handlers need to be added in only once per entire route
    * tree. that's why you should do [[handleRejections]] once per your
    * disparate route trees. Otherwise all your requests will be rejected.
    *
    * {{{
    *   val api1: RestAPI = ...
    *   val api2: RestAPI = ...
    *
    *   //works:
    *   import Directives._
    *   val finalRoute = handleRejections(RejectionHandler.default){
    *     api1.route ~ api2.route
    *   }
    *
    *   //--------------------
    *
    *   //will fail on every request:
    *   import Directives._
    *   val finalRoute =
    *     handleRejections(RejectionHandler.default)(api1.route) ~
    *     handleRejections(RejectionHandler.default)(api2.route)
    *
    * }}}
    *
    * @return
    * A new [[RestAPI]] with all the routes combined,
    * and working with the given [[RejectionHandler]].
    *
    */
  def combineWithRejectionHandler(rejectionHandler: RejectionHandler = RejectionHandler.default)(api: RestAPI, apis: RestAPI*): RestAPI = {
    val r = combine(api, apis: _ *)
    new ReifiedRestAPI(handleRejections(rejectionHandler)(r.route))
  }

  /**
    * Convenience method that combines all [[RestAPI]]s into one.
    */
  def combine(api: RestAPI, apis: RestAPI*): RestAPI = {
    val newRoute: Route =
      if (apis.isEmpty)
        api.route
      else {
        apis.foldLeft(api.route) { (combinedRoute, rapi) =>
          combinedRoute ~ rapi.route
        }
      }

    new ReifiedRestAPI(newRoute)
  }

  private val semanticallyMeaningfulHandler: ExceptionHandler = ExceptionHandler {
    case _: NotFoundFailure =>
      failure(StatusCodes.NotFound)

    case _: ForbiddenFailure =>
      failure(StatusCodes.NotFound)

    case e: UnauthorizedFailure =>
      failure(StatusCodes.Unauthorized, e)

    case e: DeniedFailure =>
      failure(StatusCodes.Forbidden, e)

    case e: InvalidInputFailure =>
      failure(StatusCodes.BadRequest, e)

    case e: ConflictFailure =>
      failure(StatusCodes.Conflict, e)

    case es: FailureMessages =>
      failures(StatusCodes.BadRequest, es)

    case e: Error =>
      failure(StatusCodes.InternalServerError, e)

    case e: NotImplementedError =>
      failure(StatusCodes.NotImplemented, Error(e))
  }

  /**
    * This is a handler for the fabled "Boxed Error" that you get when
    * a future fails with what is marked as an "Error". Unfortunately
    * this applies to NotImplementedErrors, which is really annoying :/
    */
  private val boxedErrorHandler: ExceptionHandler = ExceptionHandler {
    case e: NotImplementedError =>
      failure(StatusCodes.NotImplemented, Error(e))

    case e =>
      failure(StatusCodes.InternalServerError, Error(e))
  }

  val defaultExceptionHandler: ExceptionHandler =
    semanticallyMeaningfulHandler orElse ExceptionHandler {
      case e: java.util.concurrent.ExecutionException =>
        boxedErrorHandler(e.getCause)

      case e: Throwable =>
        failure(StatusCodes.InternalServerError, Error(e))
    }

}