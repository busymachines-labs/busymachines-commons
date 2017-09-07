package busymachines.rest

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.RouteResult
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import akka.http.scaladsl.server.directives.LoggingMagnet
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.Materializer
import org.scalatest.FlatSpec
import busymachines.json._
import busymachines.json.syntax._
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

  protected[this] def debug[T](thunk: => T): T = {
    if (requestRunner eq RequestRunners.printing) {
      fail("... you should not nest debug statements. Fix it.")
    }
    requestRunner = RequestRunners.printing
    val r = thunk
    requestRunner = RequestRunners.normal
    r
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
      private val delimiter: String = "==================================================="

      private lazy val requestLogFun: HttpRequest => Unit = { req => printRequest(req) }
      private lazy val resultLogFun: RouteResult => Unit = { rez => printResult(rez) }
      private lazy val logResultPrintln = Directives.logResult(LoggingMagnet(_ => resultLogFun))

      override def runRequest[T](request: HttpRequest)(thunk: => T)(implicit route: Route, mat: Materializer, cc: CallerContext): T = {
        val loggedRoute = logResultPrintln(route)
        cc(request) ~> logRequest(requestLogFun) ~> loggedRoute ~> check {
          response
          thunk
        }
      }

      private def printResult(rr: RouteResult)(implicit mat: Materializer): Unit = {
        print(s"\n${responseToString(rr)}")
      }

      private def responseToString(rr: RouteResult)(implicit mat: Materializer): String = {
        rr match {
          case Complete(response) =>
            val responseCode = response.status.intValue().toString
            val headers = response.headers.map(h => s"${h.name()}: ${h.value()}").mkString("\n").trim
            val entity = entityToString(response.entity).trim
            val sb = StringBuilder.newBuilder
            sb.append(s"----> Response <----")
            sb.append("\n")
            sb.append(s"Status: $responseCode")
            if (headers.nonEmpty) {
              sb.append(s"\n--\n$headers")
            }
            if (entity.nonEmpty) {
              sb.append(s"\n==\n$entity\n$delimiter")
            }
            sb.mkString

          case Rejected(rejections) =>
            val rejs = rejections.mkString("\n")
            val sb = StringBuilder.newBuilder
            sb.append(s"----> Response <----")
            sb.append(rejs)
            sb.append(s"\n$delimiter\n")
            sb.mkString
        }
      }

      private def printRequest(request: HttpRequest): Unit = {
        print(s"\n${requestToString(request)}")
      }

      private def requestToString(request: HttpRequest)(implicit mat: Materializer): String = {
        val methodUri = s"${request.method.value} ${request.uri}"
        val headers = request.headers.map(h => s"${h.name()}: ${h.value()}").mkString("\n").trim
        val entity = entityToString(request.entity).trim
        val sb = StringBuilder.newBuilder
        sb.append(methodUri)
        if (headers.nonEmpty) {
          sb.append(s"\n--\n$headers--")
        }
        if (entity.nonEmpty) {
          sb.append(s"\n==\n$entity\n")
        }
        sb.mkString
      }

      private def entityToString(entity: HttpEntity)(implicit mat: Materializer): String = {
        if (entity.isKnownEmpty()) {
          ""
        }
        else {
          val x = entity.toStrict(printingTimeoutDuration).map(_.data.decodeString("UTF-8"))
          val entityString = scala.concurrent.Await.result(x, printingTimeoutDuration)
          JsonParsing.parseString(entityString) match {
            case Left(_) => entityString
            case Right(value) => PrettyJson.spaces2NoNulls.pretty(value)
          }
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
