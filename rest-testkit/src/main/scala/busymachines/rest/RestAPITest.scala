package busymachines.rest

import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.Materializer
import org.scalatest.FlatSpec

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.language.postfixOps

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 06 Sep 2017
  *
  */
trait RestAPITest extends FlatSpec with ScalatestRouteTest {
  implicit def testedRoute: Route

  def printingTimeoutDuration: FiniteDuration = 2 minutes

  var requestRunner: RequestRunner = RequestRunners.normal

  def debug(): Unit = {
    requestRunner = RequestRunners.printing
  }

  def get[T](uri: String)(thunk: => T): T = {
    val g = Get(uri)
    requestRunner.runRequest(g)(thunk)
  }

  //========================================================================================================
  //=============  Helper methods used for internal use only. Do not use explicitly in test!! =============
  //========================================================================================================

  trait RequestRunner {
    def runRequest[T](request: HttpRequest)(thunk: => T)(implicit route: Route, mat: Materializer): T
  }

  private[this] object RequestRunners {

    object normal extends RequestRunner {
      override def runRequest[T](request: HttpRequest)(thunk: => T)(implicit route: Route, mat: Materializer): T = {
        request ~> route ~> check {
          thunk
        }
      }
    }

    object printing extends RequestRunner {
      override def runRequest[T](request: HttpRequest)(thunk: => T)(implicit route: Route, mat: Materializer): T = {
        //TODO: fixme
        println {
          s"""
             |${request.method.value} ${request.uri}
             |--
             |${request.headers.map(h => s"${h.name()}: ${h.value()}").mkString("\n")}
             |===
          """.stripMargin
        }
        request ~> route ~> check {
          thunk
        }
      }
    }

  }

}
