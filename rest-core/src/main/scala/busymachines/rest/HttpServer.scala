package busymachines.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import cats._
import cats.implicits._
import cats.effect._

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

/**
  *
  * Used to bind a route to a port so that it can start accepting HTTP requests.
  *
  * It wraps everything in a cats [[IO]] to better express the side-effects,
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
  type ReportIO = String => IO[Unit]

  private val printlnConsoleOutput: ReportIO = msg => IO(println(msg))
  private val sysErrConsoleOutput:  ReportIO = msg => IO(System.err.println(msg))

  def apply(
    name:      String,
    route:     Route,
    config:    MinimalWebServerConfig,
    logNormal: ReportIO = printlnConsoleOutput,
    logError:  ReportIO = sysErrConsoleOutput,
  )(implicit
    as: ActorSystem,
    ec: ExecutionContext,
    am: ActorMaterializer,
  ): HttpServer = {
    new HttpServer(
      name      = name,
      route     = route,
      config    = config,
      logNormal = reportWithPrependedServerName(name, logNormal),
      logError  = reportWithPrependedServerName(name, logError)
    )
  }

  private def reportWithPrependedServerName(name: String, reportIO: ReportIO): ReportIO = { originalMsg =>
    reportIO(s"$name — $originalMsg")
  }

  /**
    *
    * @param logNormal
    * Provides back to you the same loggingFunction the enclosing [[HttpServer]] was
    * instantiated with
    * @param logError
    * idem logNormal
    * @param terminateActorSystemIO
    * provides an IO whose effect is closing the ActorSystem with which the enclosing
    * [[HttpServer]] was created
    */
  final case class CleanUpContext(
    logNormal:              ReportIO,
    logError:               ReportIO,
    terminateActorSystemIO: IO[Unit]
  )
}

final class HttpServer private (
  private val name:      String,
  private val route:     Route,
  private val config:    MinimalWebServerConfig,
  private val logNormal: HttpServer.ReportIO,
  private val logError:  HttpServer.ReportIO,
)(implicit
  private val actorSystem:       ActorSystem,
  private val execContext:       ExecutionContext,
  private val actorMaterializer: ActorMaterializer,
) {

  /**
    * idem to [[startThenDoCustomCleanup]], except that the only cleanup
    * done is terminating the [[actorSystem]]
    */
  def startThenCleanUpActorSystem: IO[Unit] = {
    startThenDoCustomCleanup(ctx => ctx.terminateActorSystemIO)
  }

  /**
    * Starts the server and blocks the current thread until the JVM receives
    * the SIGKILL signal.
    *
    * Therefore be careful how you use this if this is not your usual use-case,
    * most of the time it is, so there is little reason to support other use-cases.
    *
    * After receiving the SIGKILL, the server will gracefully:
    *  - unbind the port
    *  - then do whatever cleanup is specified by the ``cleanup`` function
    *
    * @param cleanup
    *   specify your custom cleanup. N.B. you can just as well your custom
    *   clean-up logic after server has unbinded the port. by composing a new
    *   IO.
    *   the [[HttpServer.CleanUpContext]] parameter offers convenient access to
    *   the log functions you passed to the server, and to an IO whose effect
    *   is to terminate the [[actorSystem]] if you wish do terminate it.
    */
  def startThenDoCustomCleanup(
    cleanup: HttpServer.CleanUpContext => IO[Unit] = ctx => IO.unit
  ): IO[Unit] = {
    val ctx = HttpServer.CleanUpContext(
      logNormal              = logNormal,
      logError               = logError,
      terminateActorSystemIO = terminateActorSystemIO
    )
    for {
      bindAttempt <- step1_bindPortAndHandle.attempt
      result <- bindAttempt match {
                 case Right(binding) =>
                   for {
                     _ <- step2_waitForShutdownSignal
                     _ <- step3_unbind(binding: ServerBinding)
                     _ <- cleanup(ctx)
                   } yield ()

                 case Left(t) =>
                   for {
                     _ <- step1_1_bindErrorRecovery(t)
                     _ <- step2_waitForShutdownSignal
                     _ <- cleanup(ctx)
                   } yield ()
               }
    } yield result
  }

  private def step1_bindPortAndHandle: IO[ServerBinding] = {
    for {
      serverBinding <- {
        IO.fromFuture {
          Eval.later(
            Http().bindAndHandle(
              handler   = route,
              interface = config.host,
              port      = config.port
            )
          )
        }.adaptError {
          //the reason we do this is because akka wraps any exception that might occur, and it obscured the type
          case NonFatal(e) => if (e != null) e.getCause else e
        }
      }
      _ <- logNormal(show"port bound @ $config")
      _ <- logNormal("will shut down only on shutdown signal of JVM")
    } yield serverBinding
  }

  private def step1_1_bindErrorRecovery: PartialFunction[Throwable, IO[Unit]] = PartialFunction[Throwable, IO[Unit]] {
    case e: java.net.BindException =>
      for {
        _ <- logError(show"failed to bind port @ $config — reason: ${e.getLocalizedMessage}")
        _ <- logError(s"waiting for shutdown signal of JVM — please kill me")
      } yield ()

    case NonFatal(e) =>
      for {
        _ <- logError(s"failed to get HTTP up and running for unknown reason: ${e.getMessage}")
        _ <- logError(s"waiting for shutdown signal of JVM — please kill me")
      } yield ()

  }

  private def step2_waitForShutdownSignal: IO[Unit] = {
    IO {
      val mainThread = Thread.currentThread()
      val shutdownThread = new Thread(() => {

        val io = for {
          _ <- logNormal(
                s"shutdown hook started — waiting at most '${config.waitAtMostForCleanup}' for main thread to finish its work"
              )
          //Thread.join does not throw exception when timeout is over
          _ <- IO(mainThread.join(config.waitAtMostForCleanup.toMillis))
          _ <- logNormal("main thread finished — shutdown hook ended — shutting down JVM")
        } yield ()

        io.unsafeRunSync()
      })

      Runtime.getRuntime.addShutdownHook(shutdownThread)
      //this thread becomes alive only when the JVM received a shutdown signal
      while (!shutdownThread.isAlive) {
        Thread.sleep(500)
      }

    }
  }

  private def step3_unbind(binding: ServerBinding): IO[Unit] = {
    for {
      _ <- logNormal(
            show"unbinding @ $config"
          )
      _ <- IO.fromFuture(Eval.later(binding.unbind()))
    } yield ()
  }

  private def terminateActorSystemIO: IO[Unit] = {
    for {
      _ <- logNormal(s"closing actor system: ${actorSystem.name}")
      _ <- IO.fromFuture(Eval.later(actorSystem.terminate()))
    } yield ()
  }
}
