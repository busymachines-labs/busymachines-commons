/**
  * Copyright (c) 2017-2018 BusyMachines
  *
  * See company homepage at: https://www.busymachines.com/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package busymachines.rest

import Directives._
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import busymachines.core._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Aug 2017
  *
  */
trait RestAPI {

  protected def anomalyMarshaller: ToEntityMarshaller[Anomaly]

  protected def anomaliesMarshaller: ToEntityMarshaller[Anomalies]

  def route: Route =
    handleExceptions(defaultExceptionHandler)(routeDefinition)

  protected def routeDefinition: Route

  protected def defaultExceptionHandler: ExceptionHandler =
    ExceptionHandler.apply(
      RestAPI.defaultExceptionHandler(
        anomalyMarshaller,
        anomaliesMarshaller,
      )
    )
}

/**
  * Since [[busymachines.core.Anomalies]] extends
  * [[busymachines.core.Anomaly]] the marshaller for the superclass
  * is being used, thus we no longer have the ``messages`` field in the resulting output.
  *
  * thats why we segrated the scopes of the imports with the [[RestAPI.anomaly]]
  * and [[RestAPI.anomalies]] objects
  */
object RestAPI {

  import akka.http.scaladsl.marshalling.ToEntityMarshaller

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
    private val r:                              Route,
    protected override val anomalyMarshaller:   ToEntityMarshaller[Anomaly],
    protected override val anomaliesMarshaller: ToEntityMarshaller[Anomalies],
  ) extends RestAPI {
    protected override def routeDefinition: Route = r

    //no point in "handling exceptions again"
    override def route: Route = routeDefinition
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
  )(
    implicit
    routingSettings:  RoutingSettings,
    parserSettings:   ParserSettings = null,
    rejectionHandler: RejectionHandler = RejectionHandler.default,
    exceptionHandler: ExceptionHandler = null
  ): RestAPI = {
    val r           = combine(api, apis: _*)
    val sealedRoute = Route.seal(r.route)
    new ReifiedRestAPI(
      sealedRoute,
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
      api.anomalyMarshaller,
      api.anomaliesMarshaller,
    )
  }

  /**
    * Check the scaladoc for each of these failures in case something is not clear,
    * but for convenience that scaladoc has been copied here as well.
    */
  private def semanticallyMeaningfulHandler(
    implicit
    am:  ToEntityMarshaller[Anomaly],
    asm: ToEntityMarshaller[Anomalies],
  ): ExceptionHandler =
    ExceptionHandler {

      /*
       * This might be a stretch of an assumption, but usually there's no
       * reason to accumulate messages, except in cases of input validation
       */
      case es: Anomalies =>
        anomalies(StatusCodes.BadRequest, es)(asm)

      /*
       * Meaning:
       *
       * "you cannot find something; it may or may not exist, and I'm not going
       * to tell you anything else"
       */
      case _: MeaningfulAnomalies.NotFound =>
        anomaly(StatusCodes.NotFound)

      /*
       * Meaning:
       *
       * "it exists, but you're not even allowed to know about that;
       * so for short, you can't find it".
       */
      case _: MeaningfulAnomalies.Forbidden =>
        anomaly(StatusCodes.NotFound)

      /*
       * Meaning:
       *
       * "something is wrong in the way you authorized, you can try again slightly
       * differently"
       */
      case e: Anomaly with MeaningfulAnomalies.Unauthorized =>
        anomaly(StatusCodes.Unauthorized, e)

      case e: Anomaly with MeaningfulAnomalies.Denied =>
        anomaly(StatusCodes.Forbidden, e)

      /*
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
      case e: Anomaly with MeaningfulAnomalies.InvalidInput =>
        anomaly(StatusCodes.BadRequest, e)

      /*
       * Special type of invalid input.
       *
       * E.g. when you're duplicating something that ought to be unique,
       * like ids, emails.
       */
      case e: Anomaly with MeaningfulAnomalies.Conflict =>
        anomaly(StatusCodes.Conflict, e)

      case e: Catastrophe =>
        anomaly(StatusCodes.InternalServerError, e)

      case e: NotImplementedError =>
        anomaly(StatusCodes.NotImplemented, CatastrophicError(e))
    }

  /*
   * This is a handler for the fabled "Boxed Error" that you get when
   * a future fails with what is marked as an "Error". Unfortunately
   * this applies to NotImplementedErrors, and our Catastrophes,
   * which is really annoying :/
   */
  private def boxedErrorHandler(
    implicit am: ToEntityMarshaller[Anomaly]
  ): ExceptionHandler = ExceptionHandler {
    case e: NotImplementedError =>
      anomaly(StatusCodes.NotImplemented, CatastrophicError(e))

    case e: Catastrophe =>
      anomaly(StatusCodes.InternalServerError, e)

    case e =>
      anomaly(StatusCodes.InternalServerError, CatastrophicError(e))
  }

  def defaultExceptionHandler(
    implicit
    am:  ToEntityMarshaller[Anomaly],
    asm: ToEntityMarshaller[Anomalies],
  ): ExceptionHandler =
    semanticallyMeaningfulHandler(am, asm) orElse ExceptionHandler {
      case e: java.util.concurrent.ExecutionException =>
        boxedErrorHandler.apply(e.getCause)

      case e: Throwable =>
        anomaly(StatusCodes.InternalServerError, CatastrophicError(e))
    }

  def defaultExceptionHandlerNoTerminalCase(
    implicit
    am:  ToEntityMarshaller[Anomaly],
    asm: ToEntityMarshaller[Anomalies],
  ): ExceptionHandler =
    semanticallyMeaningfulHandler(am, asm) orElse ExceptionHandler {
      case e: java.util.concurrent.ExecutionException =>
        boxedErrorHandler.apply(e.getCause)
    }

}
