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
package busymachines.rest_json_test.routes_to_test

import busymachines.core._
import busymachines.rest._

import scala.concurrent.Future
import scala.util.Try

/**
  *
  * You have several options of bringing in your json. Ordered from slowest compile time to fastest:
  * ==========================
  * 1:
  * {{{
  *   busymachines.json._
  * }}}
  *
  * ==========================
  * 2:
  * {{{
  *   import SomeTestDTOJsonCodec._
  * }}}
  * ==========================
  * 3:
  * {{{
  *   class DefaultExceptionHandlerRestAPIForTesting ... with SomeTestDTOJsonCodec
  * }}}
  * ==========================
  *
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 07 Sep 2017
  *
  */
private[rest_json_test] class DefaultExceptionHandlerRestAPIForTesting
    extends JsonRestAPI with Directives with SomeTestDTOJsonCodec {

  //  Alternantively, if you remove SomeTestDTOJsonCodec mixing
  //  import busymachines.json._

  //  Alternatively, if none of the above:
  //  import SomeTestDTOJsonCodec._

  protected def routeDefinition: Route = {
    pathPrefix("not_found") {
      pathEndOrSingleSlash {
        get {
          complete(notFound("not_found"))
        }
      }
    } ~ pathPrefix("no_access") {
      pathEndOrSingleSlash {
        get {
          complete(noAccess("no_access"))
        }
      }
    } ~ pathPrefix("unauthorized") {
      pathEndOrSingleSlash {
        get {
          complete(unauthorized("unauthorized"))
        }
      }
    } ~ pathPrefix("denied") {
      pathEndOrSingleSlash {
        get {
          complete(denied("denied"))
        }
      }

    } ~ pathPrefix("invalid_input") {
      pathEndOrSingleSlash {
        get {
          complete(invalidInput("invalid_input"))
        }
      }
    } ~ pathPrefix("conflict") {
      pathEndOrSingleSlash {
        get {
          complete(conflict("conflict"))
        }
      }
    } ~ pathPrefix("multiple_failures") {
      pathEndOrSingleSlash {
        get {
          complete(failures)
        }
      }
    } ~ pathPrefix("inconsistent_state") {
      pathEndOrSingleSlash {
        get {
          complete(inconsistentState("inconsistent_state"))
        }
      }

    } ~ pathPrefix("runtime_exception") {
      pathEndOrSingleSlash {
        get {
          complete(runtimeException("runtimeException"))
        }
      }

    } ~ pathPrefix("not_implemented_boxed") {
      pathEndOrSingleSlash {
        get {
          complete(notImplementedBoxed("not_implemented_boxed"))
        }
      }

    } ~ pathPrefix("not_implemented") {
      pathEndOrSingleSlash {
        get {
          complete(notImplemented("not_implemented"))
        }
      }
    } ~ pathPrefix("normal_route") {
      pathEndOrSingleSlash {
        get {
          complete(
            (StatusCodes.OK, Future.successful {
              SomeTestDTOGet(
                42,
                "fortyTwo",
                None
              )
            })
          )
        }
      }
    }
  }

  def notFound(s: String): Future[String] = {
    Future.failed(NotFoundFailure(s, Anomaly.Parameters("not_found" -> s)))
  }

  def unauthorized(s: String): Future[String] = {
    Future.failed(UnauthorizedFailure(s, Anomaly.Parameters("unauthorized" -> s)))
  }

  def noAccess(s: String): Future[String] = {
    Future.failed(ForbiddenFailure(s, Anomaly.Parameters("forbidden" -> s)))
  }

  def denied(s: String): Future[String] = {
    Future.failed(DeniedFailure(s, Anomaly.Parameters("denied" -> s)))
  }

  def invalidInput(s: String): Future[String] = {
    Future.failed(InvalidInputFailure(s, Anomaly.Parameters("invalidInput" -> s)))
  }

  def conflict(s: String): Future[String] = {
    Future.failed(ConflictFailure(s, Anomaly.Parameters("conflict" -> s)))
  }

  def failures: Future[String] = {
    Future.failed {
      AnomalousFailures(
        AnomalyID("1234"),
        "a lot of failures",
        NotFoundFailure("notFound",    Anomaly.Parameters("one"   -> "1")),
        UnauthorizedFailure("unauth",  Anomaly.Parameters("two"   -> "2")),
        ForbiddenFailure("no_access",  Anomaly.Parameters("three" -> "3")),
        DeniedFailure("denied",        Anomaly.Parameters("four"  -> "4")),
        InvalidInputFailure("invalid", Anomaly.Parameters("five"  -> "5")),
        ConflictFailure("conflict",    Anomaly.Parameters("six"   -> "6")),
      )
    }
  }

  def inconsistentState(s: String): Future[String] = {
    Future.failed(InconsistentStateError(s))
  }

  def runtimeException(s: String): Future[String] = {
    Future.failed(new RuntimeException(s))
  }

  def notImplementedBoxed(s: String): Future[String] = {
    Future.fromTry(Try(throw new NotImplementedError(s)))
  }

  def notImplemented(s: String): Future[String] = {
    throw new NotImplementedError(s)
  }

}
