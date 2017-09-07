package busymachines.rest

import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.Materializer
import org.scalatest.FlatSpec
import busymachines.json._
import busymachines.rest.JsonSupport._


import scala.concurrent.duration._
import scala.language.postfixOps

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 06 Sep 2017
  *
  */
trait RestAPITest extends FlatSpec with ScalatestRouteTest {
  implicit protected def testedRoute: Route

  protected def printingTimeoutDuration: FiniteDuration = 2 minutes

  protected[this] var requestRunner: RequestRunner = RequestRunners.normal

  protected[this] def debug(): Unit = {
    requestRunner = RequestRunners.printing
  }

  protected def expectStatus(sc: StatusCode): Unit = {
    assertResult(sc)(response.status)
  }

  protected def get[T](uri: String)(thunk: => T)
    (implicit cc: CallerContext): T = {
    val g = Get(uri)
    requestRunner.runRequest(g)(thunk)
  }

  /**
    * @param encoder
    * I chose to use [[Encoder]] instead of
    * [[akka.http.scaladsl.marshalling.ToRequestMarshaller]] because
    * then the compilation burden on the client side is much smaller.
    * Since derivation has to be resolved only for the [[Encoder]] and
    * not—also—for converting said encoder to the marshaller.
    *
    */
  protected def post[BodyType, R](uri: String, body: BodyType)(thunk: => R)
    (implicit cc: CallerContext, encoder: Encoder[BodyType]): R = {
    val g = Post(uri, body)
    requestRunner.runRequest(g)(thunk)
  }

  protected def patch[BodyType, R](uri: String, body: BodyType)(thunk: => R)
    (implicit cc: CallerContext, encoder: Encoder[BodyType]): R = {
    val g = Patch(uri, body)
    requestRunner.runRequest(g)(thunk)
  }

  protected def put[BodyType, R](uri: String, body: BodyType)(thunk: => R)
    (implicit cc: CallerContext, encoder: Encoder[BodyType]): R = {
    val g = Put(uri, body)
    requestRunner.runRequest(g)(thunk)
  }

  protected def delete[R](uri: String)(thunk: => R)
    (implicit cc: CallerContext): R = {
    val g = Delete(uri)
    requestRunner.runRequest(g)(thunk)
  }

  protected def context[T](cc: CallerContext)(f: CallerContext => T): T = {
    f.apply(cc)
  }

  //========================================================================================================
  //=============  Helper methods used for internal use only. Do not use explicitly in test!! =============
  //========================================================================================================

  private[this] object RequestRunners {

    object normal extends RequestRunner {
      override def runRequest[T](request: HttpRequest)(thunk: => T)(implicit route: Route, mat: Materializer, cc: CallerContext): T = {
        cc(request) ~> route ~> check {
          thunk
        }
      }
    }

    object printing extends RequestRunner {
      override def runRequest[T](request: HttpRequest)(thunk: => T)(implicit route: Route, mat: Materializer, cc: CallerContext): T = {
        //TODO: fixme
        println {
          s"""
             |${request.method.value} ${request.uri}
             |--
             |${request.headers.map(h => s"${h.name()}: ${h.value()}").mkString("\n")}
             |===
          """.stripMargin
        }
        cc(request) ~> route ~> check {
          thunk
        }
      }
    }

  }

  protected[this] object Contexts {

    object none extends CallerContext {
      override def apply(httpRequest: HttpRequest): HttpRequest = httpRequest
    }

  }

}

trait RequestRunner {
  def runRequest[T](request: HttpRequest)(thunk: => T)(implicit route: Route, mat: Materializer, cc: CallerContext): T
}

trait CallerContext {
  def apply(httpRequest: HttpRequest): HttpRequest
}
