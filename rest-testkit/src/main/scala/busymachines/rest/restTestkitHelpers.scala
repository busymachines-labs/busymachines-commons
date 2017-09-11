package busymachines.rest

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.RouteResult
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import akka.http.scaladsl.server.directives.LoggingMagnet
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.{ActorMaterializer, Materializer}
import busymachines.json._
import busymachines.rest.JsonSupport._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 11 Sep 2017
  *
  */


/**
  * The reason why the methods [[RestAPIRequestBuildingSugar#get]], etc.
  * have only the [[CallerContext]] as an implicit parameter is to reduce
  * the amount of implicit resolution that has to be done in the actual
  * test code.
  *
  * Therefore [[Route]], and [[ActorMaterializer]] are resolved now
  * at the method definition. And what is actually unique to the call-site
  * is left to be resolved there.
  *
  * Ideally, you'd want to resolve all at call site, but that puts too much
  * of a compilation burden on your tests. So we sacrifice a bit of test
  * flexibility for compile time.
  *
  */
private[rest] trait RestAPIRequestBuildingSugar {
  this: ScalatestRouteTest =>

  implicit protected def testedRoute: Route

  protected[this] def requestRunner: RequestRunner

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
}

private[rest] object RequestDebugging {
  private val delimiter: String = "==================================================="

  private[rest] def requestLogFun(printingTimeoutDuration: FiniteDuration)(implicit materializer: Materializer, ec: ExecutionContext): HttpRequest => Unit = { req =>
    printRequest(req, printingTimeoutDuration)
  }

  private[rest] def resultLogFun(printingTimeoutDuration: FiniteDuration)(implicit materializer: Materializer, ec: ExecutionContext): RouteResult => Unit = { rez =>
    printResult(rez, printingTimeoutDuration)
  }

  private[rest] def logResultPrintln(printingTimeoutDuration: FiniteDuration)(implicit materializer: Materializer, ec: ExecutionContext) = {
    Directives.logResult(LoggingMagnet(_ => resultLogFun(printingTimeoutDuration)))
  }

  private def printResult(rr: RouteResult, printingTimeoutDuration: FiniteDuration)(implicit mat: Materializer, ec: ExecutionContext): Unit = {
    print(s"\n${responseToString(rr, printingTimeoutDuration)}")
  }

  private[rest] def responseToString(rr: RouteResult, printingTimeoutDuration: FiniteDuration)(implicit mat: Materializer, ec: ExecutionContext): String = {
    rr match {
      case Complete(response) =>
        val responseCode = response.status.intValue().toString
        val headers = response.headers.map(h => s"${h.name()}: ${h.value()}").mkString("\n").trim
        val entity = entityToString(response.entity, printingTimeoutDuration).trim
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

  private def printRequest(request: HttpRequest, printingTimeoutDuration: FiniteDuration)(implicit mat: Materializer, ec: ExecutionContext): Unit = {
    print(s"\n${requestToString(request, printingTimeoutDuration)}")
  }

  private[rest] def requestToString(request: HttpRequest, printingTimeoutDuration: FiniteDuration)(implicit mat: Materializer, ec: ExecutionContext): String = {
    val methodUri = s"${request.method.value} ${request.uri}"
    val headers = request.headers.map(h => s"${h.name()}: ${h.value()}").mkString("\n").trim
    val entity = entityToString(request.entity, printingTimeoutDuration).trim
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

  private def entityToString(entity: HttpEntity, printingTimeoutDuration: FiniteDuration)(implicit mat: Materializer, ec: ExecutionContext): String = {
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

private[rest] trait DefaultRequestRunners {
  this: ScalatestRouteTest =>

  private[rest] object RequestRunners {

    protected def printingTimeoutDuration: FiniteDuration = 2 minutes

    object normal extends RequestRunner {
      override def runRequest[T](request: HttpRequest)(thunk: => T)(implicit route: Route, mat: Materializer, cc: CallerContext): T = {
        cc(request) ~> route ~> check {
          thunk
        }
      }
    }

    object printing extends RequestRunner {
      override def runRequest[T](request: HttpRequest)(thunk: => T)(implicit route: Route, mat: Materializer, cc: CallerContext): T = {
        val loggedRoute = RequestDebugging.logResultPrintln(printingTimeoutDuration)(mat, executor)(route)
        cc(request) ~> logRequest(RequestDebugging.requestLogFun(printingTimeoutDuration)) ~> loggedRoute ~> check {
          response
          thunk
        }
      }
    }

  }

}

private[rest] trait DefaultContexts {

  protected[this] object Contexts {

    object none extends CallerContext {
      override def apply(httpRequest: HttpRequest): HttpRequest = httpRequest
    }

    def withRawHeader(name: String, value: String): CallerContext = new CallerContext {
      override def apply(httpRequest: HttpRequest): HttpRequest = httpRequest.addHeader(RawHeader(name, value))
    }
  }

}

trait RequestRunner {
  def runRequest[T](request: HttpRequest)(thunk: => T)(implicit route: Route, mat: Materializer, cc: CallerContext): T
}

trait CallerContext {
  def apply(httpRequest: HttpRequest): HttpRequest
}