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

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.effect.IO
import org.scalatest.FlatSpec

import scala.concurrent.ExecutionContext

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 22 Dec 2017
  *
  */
class HttpServerTest extends FlatSpec {

  private lazy val restAPI: RestAPI = new JsonRestAPI with Directives {
    override protected def routeDefinition: Route = {
      path("hello") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Hello commons!</h1>"))
        }
      }
    }
  }

  behavior of "HttpServer"

  it should "... correctly bind port, and cleanup in default case" in {
    implicit val as: ActorSystem       = ActorSystem("http-server-test")
    implicit val am: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext  = as.dispatcher

    val httpServer = HttpServer(
      name  = "HttpServerTest",
      route = restAPI.route,
      config = MinimalWebServerConfig(
        host = "0.0.0.0",
        port = 15898,
      ),
    ).startThenWaitUntilShutdownDoCustomCleanup(
      waitForShutdownIO = ctx => ctx.logNormalIO("shutting down immediately as part of test"),
      cleanupIO         = ctx => ctx.terminateActorSystemIO,
    )

    val terminationFuture = IO.fromFuture(IO(as.whenTerminated))

    httpServer.unsafeRunSync()

    terminationFuture.unsafeRunSync()
    succeed
  }

  //unfortunately, this can never be run as a test because it requires a shutdown of the JVM
  ignore should "... wait until JVM shutdown to do cleanup — code example" in {
    implicit val as: ActorSystem       = ActorSystem("http-server-test")
    implicit val am: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext  = as.dispatcher

    val httpServer = HttpServer(
      name  = "HttpServerTest",
      route = restAPI.route,
      config = MinimalWebServerConfig(
        host = "0.0.0.0",
        port = 15898,
      ),
    ).startThenCleanUpActorSystem

    val terminationFuture = IO.fromFuture(IO(as.whenTerminated))

    httpServer.unsafeRunSync()

    terminationFuture.unsafeRunSync()
    succeed
  }
}
