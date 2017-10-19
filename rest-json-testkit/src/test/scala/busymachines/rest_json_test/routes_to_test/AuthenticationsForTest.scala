package busymachines.rest_json_test.routes_to_test

import akka.http.scaladsl.model.headers._
import busymachines.rest._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 07 Sep 2017
  *
  */
private[rest_json_test] object AuthenticationsForTest {
  private[rest_json_test] lazy val basic = CallerContexts.basic("username", "password")
  private[rest_json_test] lazy val bearer = CallerContexts.bearer("D2926169E98AAA4C6B40C8C7AF7F4122946DDFA4E499908C")
}