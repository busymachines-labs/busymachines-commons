package busymachines.rest_test.routes

import akka.http.scaladsl.model.headers._
import busymachines.rest._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 07 Sep 2017
  *
  */
private[rest_test] object BasicAuthenticationContextForTesting extends CallerContext {
  override def apply(httpRequest: HttpRequest): HttpRequest = {
    httpRequest.addHeader(
      Authorization(
        BasicHttpCredentials("username", "password")
      )
    )
  }
}
