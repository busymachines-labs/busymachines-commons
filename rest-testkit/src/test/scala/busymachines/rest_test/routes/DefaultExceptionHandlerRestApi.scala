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
private[rest_test] class DefaultExceptionHandlerRestApi extends RestAPI with Directives {
  protected def routeDefinition: Route =
    path("not_found") {
      complete(notFound("not_found"))
    } ~ path("forbidden") {
      complete(forbidden("forbidden"))
    } ~ path("unauthorized") {
      complete(unauthorized("unauthorized"))
    } ~ path("denied") {
      complete(denied("denied"))
    } ~ path("invalid_input") {
      complete(invalidInput("invalid_input"))
    } ~ path("conflict") {
      complete(conflict("conflict"))
    } ~ path("multiple_failures") {
      complete(failures)
    } ~ path("inconsistent_state") {
      complete(inconsistentState("inconsistent_state"))
    } ~ path("runtime_exception") {
      complete(runtimeException("runtimeException"))
    } ~ path("not_implemented_boxed") {
      complete(notImplementedBoxed("not_implemented_boxed"))
    } ~ path("not_implemented") {
      complete(notImplemented("not_implemented"))
    }

  def notFound(s: String): Future[String] = {
    Future.failed(NotFoundFailure(s, FailureMessage.Parameters("not_found" -> s)))
  }

  def unauthorized(s: String): Future[String] = {
    Future.failed(UnauthorizedFailure(s, FailureMessage.Parameters("unauthorized" -> s)))
  }

  def forbidden(s: String): Future[String] = {
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
        ForbiddenFailure("forbid", FailureMessage.Parameters("three" -> "3")),
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
    Future.fromTry(Try(???))
  }

  def notImplemented(s: String): Future[String] = {
    ???
  }

}
