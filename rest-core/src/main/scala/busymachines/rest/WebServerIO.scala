package busymachines.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import cats.effect.IO

import scala.concurrent.ExecutionContext
import scala.io.StdIn
import scala.util.control.NonFatal

/**
  *
  * Used to bind a route to a port so that it can start accepting HTTP requests.
  *
  * It wraps everything in a cats [[IO]] to better express the side-effects,
  * and make them composeable, and reusable, and etc. etc. This is plainly evident
  * from the way that the two `bind -> ...` methods are written :)
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
object WebServerIO {

  import cats._
  import implicits._


  /**
    * Straight forward method that returns an [[IO]] that will create
    * all the side-effects described once executed
    */
  def `bind->handleRequests->wait for stop->unbind`(
    route: Route,
    config: MinimalWebServerConfig
  )(implicit as: ActorSystem, ec: ExecutionContext, am: ActorMaterializer): IO[Unit] = {
    defaultBindAndHandleIO(route, config).attempt.flatMap {

      //in case it fails, then we don't really need to do any cleanup, just print things better
      case Left(e: java.net.BindException) => IO {
        println("\n")
        System.err.println(
          s"— failed to bind port @ ${config.show} — port already used, or host unreacheable — reason: ${e.getMessage}"
        )
      }

      case Left(NonFatal(e)) => IO {
        println("\n")
        System.err.println(
          s"— failed to bind port @ ${config.show} — unknown reason: ${e.getMessage}"
        )
        e.printStackTrace()
        println("\n")
      }


      //if binding succeeds, then we need to move on to the next steps
      case Right(sb) =>
        for {
          _ <- defaultStopIO(config)
          _ <- defaultUnbindIO(sb)
        } yield ()
    }

  }

  def `bind->handleRequests->wait for stop->unbind`(
    restAPI: RestAPI,
    config: MinimalWebServerConfig
  )(implicit as: ActorSystem, ec: ExecutionContext, am: ActorMaterializer): IO[Unit] = {
    this.`bind->handleRequests->wait for stop->unbind`(restAPI.route, config)
  }

  /**
    * Convenience method that composes [[`bind->handleRequests->wait for stop->unbind`]], and
    * then also shuts down your [[ActorSystem]]
    */
  def `bind->handleRequests->wait to stop->unbind->close actor system`(
    route: Route,
    config: MinimalWebServerConfig
  )(implicit as: ActorSystem, ec: ExecutionContext, am: ActorMaterializer): IO[Unit] = for {
    _ <- `bind->handleRequests->wait for stop->unbind`(route, config)
    _ <- defaultTerminateActorSystemIO
  } yield ()

  def `bind->handleRequests->wait to stop->unbind->close actor system`(
    restAPI: RestAPI,
    config: MinimalWebServerConfig
  )(implicit as: ActorSystem, ec: ExecutionContext, am: ActorMaterializer): IO[Unit] = {
    this.`bind->handleRequests->wait to stop->unbind->close actor system`(restAPI.route, config)
  }

  //===========================================================================
  //============ Default implementations of distinct IO operations ============
  //===========================================================================

  def defaultBindAndHandleIO(
    route: Route,
    config: MinimalWebServerConfig
  )(implicit as: ActorSystem, ec: ExecutionContext, am: ActorMaterializer): IO[ServerBinding] = {
    for {
      serverBinding <- IO.fromFuture {
        Eval.later(
          Http().bindAndHandle(
            handler = route,
            interface = config.host,
            port = config.port
          )
        )
      }.adaptError {
        //the reason we do this is because akka wrap sany exception that might occur, and it obscured the type
        case NonFatal(e) => if (e != null) e.getCause else e
      }
      _ <- IO(println(s"\n— server online @ http://${config.show}"))
    } yield serverBinding
  }

  def defaultStopIO(config: MinimalWebServerConfig): IO[Unit] = IO.eval(
    Eval.later {
      println(s"— press RETURN to stop...")
      StdIn.readLine() //basically this expects a return keyboard input
      println("— stopping...")
    }
  )

  def defaultUnbindIO(serverBinding: ServerBinding)(implicit ec: ExecutionContext): IO[Unit] = {
    for {
      _ <- IO(println(s"— unbinding address @ ${serverBinding.localAddress.getHostName}:${serverBinding.localAddress.getPort}"))
      _ <- IO.fromFuture(Eval.later(serverBinding.unbind()))
    } yield ()
  }

  def defaultTerminateActorSystemIO(implicit as: ActorSystem, ec: ExecutionContext): IO[Unit] = {
    for {
      _ <- IO(println(s"— closing actor system: ${as.name}"))
      _ <- IO.fromFuture(Eval.later(as.terminate()))
    } yield ()

  }

}


