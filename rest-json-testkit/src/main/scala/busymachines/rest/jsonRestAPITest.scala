package busymachines.rest

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Assertions, Suite}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 19 Oct 2017
  *
  */
trait JsonRestAPITest extends RestAPITest with JsonRequestRunners with jsonrest.JsonSupport {
  this: Suite with Assertions =>
  debug()
}

private[rest] trait JsonRequestRunners extends DefaultRequestRunners{
  this: ScalatestRouteTest =>
  import busymachines.json._

  override protected def transformEntityString(entityString: String): String = {
    JsonParsing.parseString(entityString) match {
      case Left(_) => entityString
      case Right(value) => PrettyJson.spaces2NoNulls.pretty(value)
    }
  }
}
