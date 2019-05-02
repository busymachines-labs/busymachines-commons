/**
  * Copyright (c) 2017-2018 BusyMachines
  *
  * See company homepage at: https://www.busymachines.com/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package busymachines.rest

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.RouteResult
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import akka.http.scaladsl.server.directives.LoggingMagnet
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.Materializer

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * The reason why the methods [[RestAPIRequestBuildingSugar#get]], etc.
  * have only the [[CallerContext]] as an implicit parameter is to reduce
  * the amount of implicit resolution that has to be done in the actual
  * test code.
  *
  * Therefore [[Route]], and [[akka.stream.ActorMaterializer]] are resolved now
  * at the method definition. And what is actually unique to the call-site
  * is left to be resolved there.
  *
  * Ideally, you'd want to resolve all at call site, but that puts too much
  * of a compilation burden on your tests. So we sacrifice a bit of test
  * flexibility for compile time.
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 11 Sep 2017
  *
  */
private[rest] trait RestAPIRequestBuildingSugar {
  this: ScalatestRouteTest =>

  implicit protected def testedRoute: Route

  protected[this] def requestRunner: RequestRunner

  protected def get[T](uri: String)(thunk: => T)(implicit cc: CallerContext): T = {
    val g = Get(uri)
    requestRunner.runRequest(g)(thunk)
  }

  protected def post[BodyType, R](uri: String, body: BodyType)(thunk: => R)(
    implicit
    cc:      CallerContext,
    encoder: ToEntityMarshaller[BodyType],
  ): R = {
    val g = Post(uri, body)
    requestRunner.runRequest(g)(thunk)
  }

  protected def postRaw[R](
    uri:         String,
    contentType: ContentType.NonBinary = ContentTypes.`application/json`,
  )(raw:         String)(thunk: => R)(implicit cc: CallerContext): R = {
    val g = Post(uri).withEntity(contentType, raw)
    requestRunner.runRequest(g)(thunk)
  }

  protected def patch[BodyType, R](uri: String, body: BodyType)(thunk: => R)(
    implicit
    cc:      CallerContext,
    encoder: ToEntityMarshaller[BodyType],
  ): R = {
    val g = Patch(uri, body)
    requestRunner.runRequest(g)(thunk)
  }

  protected def patchRaw[R](
    uri:         String,
    contentType: ContentType.NonBinary = ContentTypes.`application/json`,
  )(raw:         String)(thunk: => R)(implicit cc: CallerContext): R = {
    val g = Patch(uri).withEntity(contentType, raw)
    requestRunner.runRequest(g)(thunk)
  }

  protected def put[BodyType, R](uri: String, body: BodyType)(thunk: => R)(
    implicit cc:                      CallerContext,
    encoder:                          ToEntityMarshaller[BodyType],
  ): R = {
    val g = Put(uri, body)
    requestRunner.runRequest(g)(thunk)
  }

  protected def putRaw[R](
    uri:         String,
    contentType: ContentType.NonBinary = ContentTypes.`application/json`,
  )(raw:         String)(thunk: => R)(implicit cc: CallerContext): R = {
    val g = Put(uri).withEntity(contentType, raw)
    requestRunner.runRequest(g)(thunk)
  }

  protected def delete[R](uri: String)(thunk: => R)(implicit cc: CallerContext): R = {
    val g = Delete(uri)
    requestRunner.runRequest(g)(thunk)
  }

  protected def context[T](cc: CallerContext)(f: CallerContext => T): T = {
    f.apply(cc)
  }
}

//=============================================================================
//============================= Request Debugging =============================
//=============================================================================

private[rest] object RequestDebugging {
  private val delimiter: String = "==================================================="

  private[rest] def requestLogFun(
    printingTimeoutDuration: FiniteDuration,
  )(transformEntity:         String => String)(
    implicit
    materializer: Materializer,
    ec:           ExecutionContext,
  ): HttpRequest => Unit = { req =>
    printRequest(req, printingTimeoutDuration)(transformEntity)
  }

  private[rest] def resultLogFun(
    printingTimeoutDuration: FiniteDuration,
  )(
    transformEntity:       String => String,
  )(implicit materializer: Materializer, ec: ExecutionContext): RouteResult => Unit = { rez =>
    printResult(rez, printingTimeoutDuration)(transformEntity)
  }

  private[rest] def logResultPrintln(
    printingTimeoutDuration: FiniteDuration,
  )(transformEntity:         String => String)(implicit materializer: Materializer, ec: ExecutionContext) = {
    Directives.logResult(LoggingMagnet(_ => resultLogFun(printingTimeoutDuration)(transformEntity)))
  }

  private def printResult(
    rr:                      RouteResult,
    printingTimeoutDuration: FiniteDuration,
  )(transformEntity:         String => String)(implicit mat: Materializer, ec: ExecutionContext): Unit = {
    print(s"\n${responseToString(rr, printingTimeoutDuration)(transformEntity)}")
  }

  private[rest] def responseToString(
    rr:                      RouteResult,
    printingTimeoutDuration: FiniteDuration,
  )(
    transformEntity: String => String,
  )(
    implicit
    mat: Materializer,
    ec:  ExecutionContext,
  ): String = {
    rr match {
      case Complete(response) =>
        val responseCode = response.status.intValue().toString
        val headers      = response.headers.map(h => s"${h.name()}: ${h.value()}").mkString("\n").trim
        val entity       = entityToString(response.entity, printingTimeoutDuration)(transformEntity).trim
        val sb           = StringBuilder.newBuilder
        sb.append(s"----> Response <----")
        sb.append("\n")
        sb.append(s"Status: $responseCode")
        if (headers.nonEmpty) {
          sb.append(s"\n--\n$headers")
        }
        if (entity.nonEmpty) {
          sb.append(s"\n==\n$entity\n$delimiter\n")
        }
        sb.mkString

      case Rejected(rejections) =>
        val rejs = rejections.mkString("\n")
        val sb   = StringBuilder.newBuilder
        sb.append(s"----> Response <----")
        sb.append(rejs)
        sb.append(s"\n$delimiter\n")
        sb.mkString
    }
  }

  private def printRequest(
    request:                 HttpRequest,
    printingTimeoutDuration: FiniteDuration,
  )(transformEntity:         String => String)(implicit mat: Materializer, ec: ExecutionContext): Unit = {
    print(s"\n${requestToString(request, printingTimeoutDuration)(transformEntity)}")
  }

  private[rest] def requestToString(
    request:                 HttpRequest,
    printingTimeoutDuration: FiniteDuration,
  )(transformEntity:         String => String)(implicit mat: Materializer, ec: ExecutionContext): String = {
    val methodUri = s"${request.method.value} ${request.uri}"
    val headers   = request.headers.map(h => s"${h.name()}: ${h.value()}").mkString("\n").trim
    val entity    = entityToString(request.entity, printingTimeoutDuration)(transformEntity).trim
    val sb        = StringBuilder.newBuilder
    sb.append(methodUri)
    if (headers.nonEmpty) {
      sb.append(s"\n--\n$headers--")
    }
    if (entity.nonEmpty) {
      sb.append(s"\n==\n$entity\n")
    }
    sb.mkString
  }

  private def entityToString(
    entity:                  HttpEntity,
    printingTimeoutDuration: FiniteDuration,
  )(
    transformEntity: String => String,
  )(implicit mat:    Materializer, ec: ExecutionContext): String = {
    if (entity.isKnownEmpty()) {
      ""
    }
    else {
      val x            = entity.toStrict(printingTimeoutDuration).map(_.data.decodeString("UTF-8"))
      val entityString = scala.concurrent.Await.result(x, printingTimeoutDuration)
      transformEntity(entityString)
    }

  }
}

//=============================================================================
//============================= Request Runners ===============================
//=============================================================================

trait RequestRunner {
  def runRequest[T](request: HttpRequest)(thunk: => T)(implicit route: Route, mat: Materializer, cc: CallerContext): T
}

private[rest] trait DefaultRequestRunners {
  this: ScalatestRouteTest =>

  /**
    * Used to do transformations on the entity in the body of the content in
    * case it is printed. Function is never used outside the [[RequestRunners.printing]] context
    *
    * Examples of usage: pretty printing the content.
    */
  protected def transformEntityString(entity: String): String = entity

  private[rest] object RequestRunners {

    protected def printingTimeoutDuration: FiniteDuration = 2 minutes

    object normal extends RequestRunner {
      override def runRequest[T](
        request: HttpRequest,
      )(thunk:   => T)(implicit route: Route, mat: Materializer, cc: CallerContext): T = {
        cc(request) ~> route ~> check {
          thunk
        }
      }
    }

    object printing extends RequestRunner {
      override def runRequest[T](
        request: HttpRequest,
      )(thunk:   => T)(implicit route: Route, mat: Materializer, cc: CallerContext): T = {
        val loggedRoute =
          RequestDebugging.logResultPrintln(printingTimeoutDuration)(transformEntityString)(mat, executor)(route)
        cc(request) ~> logRequest(RequestDebugging.requestLogFun(printingTimeoutDuration)(transformEntityString)) ~> loggedRoute ~> check {
          response
          thunk
        }
      }
    }

  }

}

//=============================================================================
//============================= Caller Contexts ===============================
//=============================================================================

private[rest] trait ProvidedContexts {

  protected[this] object Contexts extends CallerContexts

}

object CallerContext {
  def apply(fun: HttpRequest => HttpRequest): CallerContext = (httpRequest: HttpRequest) => fun.apply(httpRequest)
}

trait CallerContext {
  def apply(httpRequest: HttpRequest): HttpRequest
}

object CallerContexts extends CallerContexts

trait CallerContexts {

  object none extends CallerContext {
    override def apply(httpRequest: HttpRequest): HttpRequest = httpRequest
  }

  def withRawHeader(name: String, value: String): CallerContext = CallerContext { httpRequest =>
    httpRequest.addHeader(RawHeader(name, value))
  }

  def basic(username: String, password: String): CallerContext = CallerContext { httpRequest =>
    httpRequest.addHeader(
      Authorization(
        BasicHttpCredentials(username, password),
      ),
    )
  }

  def bearer(token: String): CallerContext = CallerContext { httpRequest =>
    httpRequest.addHeader(
      Authorization(
        OAuth2BearerToken(token),
      ),
    )
  }
}
