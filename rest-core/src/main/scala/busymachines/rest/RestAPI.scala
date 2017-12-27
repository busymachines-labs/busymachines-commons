package busymachines.rest

import Directives._
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import busymachines.core.{exceptions => dex}
import busymachines.core._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Aug 2017
  *
  */
trait RestAPI {

  @scala.deprecated("Will be removed in 0.3.0 — Use AnomalyJsonCodec", "0.2.0")
  protected def failureMessageMarshaller: ToEntityMarshaller[dex.FailureMessage]

  @scala.deprecated("Will be removed in 0.3.0 — Use AnomalyJsonCodec", "0.2.0")
  protected def failureMessagesMarshaller: ToEntityMarshaller[dex.FailureMessages]

  protected def anomalyMarshaller: ToEntityMarshaller[Anomaly]

  protected def anomaliesMarshaller: ToEntityMarshaller[Anomalies]

  def route: Route =
    handleExceptions(defaultExceptionHandler)(routeDefinition)

  protected def routeDefinition: Route

  protected def defaultExceptionHandler: ExceptionHandler =
    ExceptionHandler.apply(
      RestAPI.defaultExceptionHandler(
        failureMessageMarshaller,
        failureMessagesMarshaller,
        anomalyMarshaller,
        anomaliesMarshaller,
      )
    )
}

/**
  * Since [[busymachines.core.exceptions.FailureMessages]] extends
  * [[busymachines.core.exceptions.FailureMessage]] the marshaller for the superclass
  * is being used, thus we no longer have the ``messages`` field in the resulting output.
  *
  * thats why we segrated the scopes of the imports with the [[RestAPI.failure]]
  * and [[RestAPI.failures]] objects
  */
object RestAPI {

  import akka.http.scaladsl.marshalling.ToEntityMarshaller

  @scala.deprecated("Will be removed in 0.3.0 — use anomaly", "0.2.0-RC8")
  object failure {

    @scala.deprecated("Will be removed in 0.3.0 — use anomaly", "0.2.0-RC8")
    def apply(statusCode: StatusCode): Route = {
      complete(statusCode)
    }

    @scala.deprecated("Will be removed in 0.3.0 — use anomaly", "0.2.0-RC8")
    def apply(statusCode: StatusCode, f: dex.FailureMessage)(
      implicit fsm:       ToEntityMarshaller[dex.FailureMessage]
    ): Route = {
      complete((statusCode, f))
    }
  }

  @scala.deprecated("Will be removed in 0.3.0 — use anomaly", "0.2.0-RC8")
  object failures {

    @scala.deprecated("Will be removed in 0.3.0 — use anomaly", "0.2.0-RC8")
    def apply(statusCode: StatusCode, fs: dex.FailureMessages)(
      implicit fsm:       ToEntityMarshaller[dex.FailureMessages]
    ): Route =
      complete((statusCode, fs))
  }

  object anomaly {

    def apply(statusCode: StatusCode): Route = {
      complete(statusCode)
    }

    def apply(statusCode: StatusCode, f: Anomaly)(
      implicit am:        ToEntityMarshaller[Anomaly]
    ): Route = {
      complete((statusCode, f))
    }
  }

  object anomalies {

    def apply(statusCode: StatusCode, fs: Anomalies)(
      implicit asm:       ToEntityMarshaller[Anomalies]
    ): Route =
      complete((statusCode, fs))
  }

  private class ReifiedRestAPI(
    private val r:                                    Route,
    override protected val failureMessageMarshaller:  ToEntityMarshaller[dex.FailureMessage],
    override protected val failureMessagesMarshaller: ToEntityMarshaller[dex.FailureMessages],
    override protected val anomalyMarshaller:         ToEntityMarshaller[Anomaly],
    override protected val anomaliesMarshaller:       ToEntityMarshaller[Anomalies],
  ) extends RestAPI {
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
  def seal(
    api:  RestAPI,
    apis: RestAPI*
  )(implicit
    routingSettings:  RoutingSettings,
    parserSettings:   ParserSettings = null,
    rejectionHandler: RejectionHandler = RejectionHandler.default,
    exceptionHandler: ExceptionHandler = null): RestAPI = {
    val r           = combine(api, apis: _*)
    val sealedRoute = Route.seal(r.route)
    new ReifiedRestAPI(
      sealedRoute,
      api.failureMessageMarshaller,
      api.failureMessageMarshaller,
      api.anomalyMarshaller,
      api.anomaliesMarshaller,
    )
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

    new ReifiedRestAPI(
      newRoute,
      api.failureMessageMarshaller,
      api.failureMessageMarshaller,
      api.anomalyMarshaller,
      api.anomaliesMarshaller,
    )
  }

  /**
    * Check the scaladoc for each of these failures in case something is not clear,
    * but for convenience that scaladoc has been copied here as well.
    */
  private def semanticallyMeaningfulHandler(
    implicit fm: ToEntityMarshaller[dex.FailureMessage],
    fsm:         ToEntityMarshaller[dex.FailureMessages],
    am:          ToEntityMarshaller[Anomaly],
    asm:         ToEntityMarshaller[Anomalies],
  ): ExceptionHandler =
    ExceptionHandler {

      /**
        * Meaning:
        *
        * "you cannot find something; it may or may not exist, and I'm not going
        * to tell you anything else"
        */
      case _: dex.SemanticFailures.NotFound =>
        failure(StatusCodes.NotFound)

      case _: MeaningfulAnomalies.NotFound =>
        anomaly(StatusCodes.NotFound)

      /**
        * Meaning:
        *
        * "it exists, but you're not even allowed to know about that;
        * so for short, you can't find it".
        */
      case _: dex.SemanticFailures.Forbidden =>
        failure(StatusCodes.NotFound)

      case _: MeaningfulAnomalies.Forbidden =>
        anomaly(StatusCodes.NotFound)

      /**
        * Meaning:
        *
        * "something is wrong in the way you authorized, you can try again slightly
        * differently"
        */
      case e: dex.FailureMessage with dex.SemanticFailures.Unauthorized =>
        failure(StatusCodes.Unauthorized, e)

      case e: Anomaly with MeaningfulAnomalies.Unauthorized =>
        anomaly(StatusCodes.Unauthorized, e)

      case e: dex.FailureMessage with dex.SemanticFailures.Denied =>
        failure(StatusCodes.Forbidden, e)

      case e: Anomaly with MeaningfulAnomalies.Denied =>
        anomaly(StatusCodes.Forbidden, e)

      /**
        * Obviously, whenever some input data is wrong.
        *
        * This one is probably your best friend, and the one you
        * have to specialize the most for any given problem domain.
        * Otherwise you just wind up with a bunch of nonsense, obtuse
        * errors like:
        * - "the input was wrong"
        * - "gee, thanks, more details, please?"
        * - sometimes you might be tempted to use NotFound, but this
        * might be better suited. For instance, when you are dealing
        * with a "foreign key" situation, and the foreign key is
        * the input of the client. You'd want to be able to tell
        * the user that their input was wrong because something was
        * not found, not simply that it was not found.
        *
        * Therefore, specialize frantically.
        */
      case e: dex.FailureMessage with dex.SemanticFailures.InvalidInput =>
        failure(StatusCodes.BadRequest, e)

      case e: Anomaly with MeaningfulAnomalies.InvalidInput =>
        anomaly(StatusCodes.BadRequest, e)

      /**
        * Special type of invalid input.
        *
        * E.g. when you're duplicating something that ought to be unique,
        * like ids, emails.
        */
      case e: dex.FailureMessage with dex.SemanticFailures.Conflict =>
        failure(StatusCodes.Conflict, e)

      case e: Anomaly with MeaningfulAnomalies.Conflict =>
        anomaly(StatusCodes.Conflict, e)

      /**
        * This might be a stretch of an assumption, but usually there's no
        * reason to accumulate messages, except in cases of input validation
        */
      case es: dex.FailureMessages =>
        failures(StatusCodes.BadRequest, es)(fsm)

      case es: Anomalies =>
        anomalies(StatusCodes.BadRequest, es)(asm)

      case e: dex.ErrorMessage =>
        failure(StatusCodes.InternalServerError, e)

      case e: Catastrophe =>
        anomaly(StatusCodes.InternalServerError, e)

      case e: NotImplementedError =>
        anomaly(StatusCodes.NotImplemented, CatastrophicError(e))
    }

  /**
    * This is a handler for the fabled "Boxed Error" that you get when
    * a future fails with what is marked as an "Error". Unfortunately
    * this applies to NotImplementedErrors, which is really annoying :/
    */
  private def boxedErrorHandler(
    implicit am: ToEntityMarshaller[Anomaly]
  ): ExceptionHandler = ExceptionHandler {
    case e: NotImplementedError =>
      anomaly(StatusCodes.NotImplemented, CatastrophicError(e))

    case e =>
      anomaly(StatusCodes.InternalServerError, CatastrophicError(e))
  }

  def defaultExceptionHandler(
    implicit fm: ToEntityMarshaller[dex.FailureMessage],
    fsm:         ToEntityMarshaller[dex.FailureMessages],
    am:          ToEntityMarshaller[Anomaly],
    asm:         ToEntityMarshaller[Anomalies],
  ): ExceptionHandler =
    semanticallyMeaningfulHandler(fm, fsm, am, asm) orElse ExceptionHandler {
      case e: java.util.concurrent.ExecutionException =>
        boxedErrorHandler.apply(e.getCause)

      case e: Throwable =>
        anomaly(StatusCodes.InternalServerError, CatastrophicError(e))
    }

}
