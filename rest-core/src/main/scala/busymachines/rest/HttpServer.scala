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
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import cats.implicits._
import cats.effect._

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

/**
  *
  * Used to bind a route to a port so that it can start accepting HTTP requests.
  *
  * It wraps everything in a [[cats.effect.IO]] to better express the side-effects,
  * and make them composable, and reusable, and etc. etc.
  *
  * As you can see each individual step has a default implementation of an
  * effect, and you can substitute your own.
  *
  * Implementation mostly lifted from the standard akka http example, but then IO-ed:
  * {{{
  *   https://doc.akka.io/docs/akka-http/current/scala/http/routing-dsl/index.html#high-level-server-side-api
  * }}}
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 30 Oct 2017
  *
  */
object HttpServer {
  type LogIO = String => IO[Unit]

  private val printlnConsoleOutput: LogIO = msg => IO(println(msg))
  private val sysErrConsoleOutput:  LogIO = msg => IO(System.err.println(msg))

  def apply(
    name:        String,
    route:       Route,
    config:      MinimalWebServerConfig,
    logNormalIO: LogIO = printlnConsoleOutput,
    logErrorIO:  LogIO = sysErrConsoleOutput,
  )(
    implicit
    as: ActorSystem,
    ec: ExecutionContext,
    am: ActorMaterializer,
  ): HttpServer = {
    new HttpServer(
      name        = name,
      route       = route,
      config      = config,
      logNormalIO = reportWithPrependedServerName(name, logNormalIO),
      logErrorIO  = reportWithPrependedServerName(name, logErrorIO),
    )
  }

  private def reportWithPrependedServerName(name: String, reportIO: LogIO): LogIO = { originalMsg =>
    reportIO(s"$name — $originalMsg")
  }

  /**
    * @param logNormalIO
    * Provides back to you the same loggingFunction the enclosing [[HttpServer]] was
    * instantiated with
    *
    * @param logErrorIO
    * idem logNormal
    *
    * @param waitForServerShutdownIO
    * provides an IO which waits for an external SIGKILL of the JVM. It is blocking,
    * when evaluated
    *
    * @param terminateActorSystemIO
    * provides an IO whose effect is closing the ActorSystem with which the enclosing
    * [[HttpServer]] was created
    */
  final case class Context private[HttpServer] (
    logNormalIO:             LogIO,
    logErrorIO:              LogIO,
    waitForServerShutdownIO: IO[Unit],
    terminateActorSystemIO:  IO[Unit],
  )
}

final class HttpServer private (
  private val name:        String,
  private val route:       Route,
  private val config:      MinimalWebServerConfig,
  private val logNormalIO: HttpServer.LogIO,
  private val logErrorIO:  HttpServer.LogIO,
)(
  implicit
  private val actorSystem:       ActorSystem,
  private val execContext:       ExecutionContext,
  private val actorMaterializer: ActorMaterializer,
) {

  /**
    * idem to [[startThenWaitUntilShutdownDoCustomCleanup]],
    * except that there are default values for the above's parameters:
    *
    * ``waitForShutdownIO``:
    *   - blocks until the JVM received and external shutdown signal.
    *
    * ``cleanupIO``:
    *   - terminates the [[actorSystem]]
    */
  def startThenCleanUpActorSystem: IO[Unit] = {
    startThenWaitUntilShutdownDoCustomCleanup(
      waitForShutdownIO = _.waitForServerShutdownIO,
      cleanupIO         = _.terminateActorSystemIO,
    )
  }

  /**
    * Starts the server and blocks the current thread as specified by the
    * ``waitForShutdownIO`` parameter, a default IO that waits for SIGKILL is
    * available in the [[HttpServer.Context.waitForServerShutdownIO]] value.
    *
    * After this shutdownIO is run, the server will gracefully:
    *  - unbind the port
    *  - then do whatever cleanup is specified by the ``cleanup`` function
    *
    * @param cleanupIO
    * specify your custom cleanup. N.B. you can just as well your custom
    * clean-up logic after server has unbinded the port. by composing a new
    * IO.
    * the [[HttpServer.Context]] parameter offers convenient access to
    * the log functions you passed to the server, and to an IO whose effect
    * is to terminate the [[actorSystem]] if you wish do terminate it.
    *
    * @param waitForShutdownIO
    *   The IO specifying what exactly the waiting for cleanup is
    *   @see [[waitForExternalSIGKILLSignal]] as an example
    */
  def startThenWaitUntilShutdownDoCustomCleanup(
    waitForShutdownIO: HttpServer.Context => IO[Unit],
    cleanupIO:         HttpServer.Context => IO[Unit],
  ): IO[Unit] = {
    val ctx = HttpServer.Context(
      logNormalIO             = logNormalIO,
      logErrorIO              = logErrorIO,
      terminateActorSystemIO  = terminateActorSystemIO,
      waitForServerShutdownIO = waitForExternalSIGKILLSignal,
    )
    for {
      bindAttempt <- step1_bindPortAndHandle.attempt
      result <- bindAttempt match {
        case Right(binding) =>
          for {
            _ <- waitForShutdownIO(ctx)
            _ <- step3_unbind(binding: ServerBinding)
            _ <- cleanupIO(ctx)
          } yield ()

        case Left(t) =>
          for {
            _ <- step1_1_bindErrorRecovery(t)
            _ <- waitForShutdownIO(ctx)
            _ <- cleanupIO(ctx)
          } yield ()
      }
    } yield result
  }

  private def step1_bindPortAndHandle: IO[ServerBinding] = {
    for {
      serverBinding <- {
        IO.fromFuture {
            IO(
              Http().bindAndHandle(
                handler   = route,
                interface = config.host,
                port      = config.port,
              ),
            )
          }
          .adaptError {
            //the reason we do this is because akka wraps any exception that might occur, and it obscured the type
            case NonFatal(e) => if (e != null) e.getCause else e
          }
      }
      _ <- logNormalIO(show"port bound @ $config")
    } yield serverBinding
  }

  private def step1_1_bindErrorRecovery: PartialFunction[Throwable, IO[Unit]] = {
    case e: java.net.BindException =>
      logErrorIO(show"failed to bind port @ $config — reason: ${e.getLocalizedMessage}") >>
        logErrorIO(s"waiting for shutdown signal of JVM — please kill me")

    case NonFatal(e) =>
      logErrorIO(s"failed to get HTTP up and running for unknown reason: ${e.getMessage}") >>
        logErrorIO(s"waiting for shutdown signal of JVM — please kill me")

  }

  private def step3_unbind(binding: ServerBinding): IO[Unit] = {
    (logNormalIO(show"unbinding @ $config") >>
      IO.fromFuture(IO(binding.unbind()))).map(_ => ())
  }

  private def waitForExternalSIGKILLSignal: IO[Unit] = {
    logNormalIO("will shut down only on shutdown signal of JVM") >> IO {
      val mainThread = Thread.currentThread()
      val shutdownThread = new Thread(
        () => {
          val io = logNormalIO(
            s"shutdown hook started — waiting at most '${config.waitAtMostForCleanup}' for main thread to finish its work",
          ) >>
            IO(mainThread.join(config.waitAtMostForCleanup.toMillis)) >>
            logNormalIO("main thread finished — shutdown hook ended — shutting down JVM")

          io.unsafeRunSync()
        },
      )

      Runtime.getRuntime.addShutdownHook(shutdownThread)
      //this thread becomes alive only when the JVM received a shutdown signal
      while (!shutdownThread.isAlive) {
        Thread.sleep(500)
      }

    }
  }

  private def terminateActorSystemIO: IO[Unit] = {
    logNormalIO(s"closing actor system: ${actorSystem.name}") >>
      IO.fromFuture(IO(actorSystem.terminate()))
  }.map(_ => ())
}
