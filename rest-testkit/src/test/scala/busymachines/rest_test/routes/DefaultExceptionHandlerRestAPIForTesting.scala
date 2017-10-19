package busymachines.rest_test.routes

import busymachines.core.exceptions._
import busymachines.rest._

import scala.concurrent.Future
import scala.util.Try

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 06 Sep 2017
  *
  */
private[rest_test] class DefaultExceptionHandlerRestAPIForTesting extends RestAPI with Directives {

  import busymachines.rest.JsonSupport._
  import busymachines.json._

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
            StatusCodes.OK,
            Future.successful {
              SomeTestDTOGet(
                42,
                "fortyTwo",
                None
              )
            }
          )
        }
      }
    }
  }

  def notFound(s: String): Future[String] = {
    Future.failed(NotFoundFailure(s, FailureMessage.Parameters("not_found" -> s)))
  }

  def unauthorized(s: String): Future[String] = {
    Future.failed(UnauthorizedFailure(s, FailureMessage.Parameters("unauthorized" -> s)))
  }

  def noAccess(s: String): Future[String] = {
    Future.failed(ForbiddenFailure(s, FailureMessage.Parameters("forbidden" -> s)))
  }

  def denied(s: String): Future[String] = {
    Future.failed(DeniedFailure(s, FailureMessage.Parameters("denied" -> s)))
  }

  def invalidInput(s: String): Future[String] = {
    Future.failed(InvalidInputFailure(s, FailureMessage.Parameters("invalidInput" -> s)))
  }

  def conflict(s: String): Future[String] = {
    Future.failed(ConflictFailure(s, FailureMessage.Parameters("conflict" -> s)))
  }

  def failures: Future[String] = {
    Future.failed {
      Failures(
        FailureID("1234"),
        "a lot of failures",
        NotFoundFailure("notFound", FailureMessage.Parameters("one" -> "1")),
        UnauthorizedFailure("unauth", FailureMessage.Parameters("two" -> "2")),
        ForbiddenFailure("no_access", FailureMessage.Parameters("three" -> "3")),
        DeniedFailure("denied", FailureMessage.Parameters("four" -> "4")),
        InvalidInputFailure("invalid", FailureMessage.Parameters("five" -> "5")),
        ConflictFailure("conflict", FailureMessage.Parameters("six" -> "6")),
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
    Future.fromTry(Try(throw new NotImplementedError("boxed")))
  }

  def notImplemented(s: String): Future[String] = {
    throw new NotImplementedError("not boxed")
  }

}
