package busymachines.rest_json_test.routes_to_test

import akka.http.scaladsl.model.headers._
import busymachines.rest._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 07 Sep 2017
  *
  */
private[rest_json_test] object BasicAuthenticationContextForTesting extends CallerContext {
  override def apply(httpRequest: HttpRequest): HttpRequest = {
    httpRequest.addHeader(
      Authorization(
        BasicHttpCredentials("username", "password")
      )
    )
  }
}
