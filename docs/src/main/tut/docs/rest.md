---
layout: docs
title: rest
---
[![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-rest-json_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-rest-json_2.12) [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-rest-core_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-rest-core_2.12)
# busymachines-commons-rest

This is the joint documentation for two modules:
* `busymachines-commons-rest-core`
* `busymachines-commonst-rest-json`

## artifacts

* stable: `0.2.0`
* latest: `0.3.0-M2`

```scala
"com.busymachines" %% "busymachines-commons-rest-json" % "0.2.0"
```
You do not need to depend explicitely on core, because rest-json already does.
```scala
"com.busymachines" %% "busymachines-commons-rest-core" % "0.2.0"
```

### Transitive dependencies
- busymachines-commons-core
- busymachines-commons-json
- akka-http 10.0.11
- akka-actor 2.5.8
- akka-stream 2.5.8
- cats-effects 0.8.0
- cats-core 1.0.1
- circe 0.9.0
- akka-http-circe 1.19.0

## Description

The difference between `rest-core` and `rest-json` is that the latter is the reified version of the former. You can immediately start using it by simply inheriting `busymachines.rest.JsonRestAPI`.

You cannot use `rest-core` without defining marshallers for the `Anomaly` trait hierarchy from `core`.

## Examples

Examples of usage are rather verbose so you'll have to check the tests in the [busymachines-commons-rest-json-testkit](rest-testkit`) module.

## Getting a HttpServer up and running

A convenient and pure way—based on `cats.effects.IO`—of binding your server to the network interface. Read the code to trivially spot how to tailor it better to your needs. Fully functioning example:

```scala

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import busymachines.rest._

import scala.concurrent.ExecutionContext

object MainRestPlaygroundApp extends App {

    implicit val as: ActorSystem       = ActorSystem("http-server-test")
    implicit val am: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext  = as.dispatcher

    val httpServer = HttpServer(
      name  = "HttpServerTest",
      route = restAPI.route,
      config = MinimalWebServerConfig(
        host = "0.0.0.0",
        port = 9999
      ) // or.default
    ).startThenCleanUpActorSystem

    httpServer.unsafeRunSync()

}

class HelloWorld extends JsonRestAPI with Directives {
  override protected def routeDefinition: Route = {
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Hello commons!</h1>"))
      }
    }
  }
}

```

Output:
```
HttpServerTest — port bound @ /0.0.0.0:9999
HttpServerTest — will shut down only on shutdown signal of JVM
#### after you kill the JVM ####
HttpServerTest — shutdown hook started — waiting at most '60 seconds' for main thread to finish its work
HttpServerTest — unbinding @ /0.0.0.0:9999
HttpServerTest — closing actor system: my-system
HttpServerTest — main thread finished — shutdown hook ended — shutting down JVM
```

You can customize how messages are logged by providing two functions to the `HttpServer` constructor:

```scala
  val logNormalFunction: HttpServer.LogIO = msg => IO(logger.info(msg))
  val logErrorFunction:  HttpServer.LogIO = msg => IO(logger.info(msg))

  val httpServer1 = HttpServer(
    name        = "PlaygroundServer",
    route       = restAPI.route,
    config      = MinimalWebServerConfig.default,
    logNormalIO = logNormalFunction,
    logErrorIO  = logErrorFunction
  )
```

You can also customize what cleaning is done, and what the shutdownhook of the application is by invoking the `startThenWaitUntilShutdownDoCustomCleanup` instead of the `startThenCleanUpActorSystem`:

```scala
    val httpServer = HttpServer(
      name  = "HttpServerTest",
      route = restAPI.route,
      config = MinimalWebServerConfig(
        host = "0.0.0.0",
        port = 15898
      )
    ).startThenWaitUntilShutdownDoCustomCleanup(
      waitForShutdownIO = ctx => ctx.logNormalIO("shutting down immediately"),
      //N.B. that the port will still be unbound, it's just that the HttpServer won't close the ActorSystem this time
      cleanupIO         = ctx => ctx.logNormalIO("I refuse to clean up after myself")
    )
```

The `ctx` object is of type `HttpServer.Context` and offers access to the logging functions the HttpServer uses, and the IOs for the default implementations of waiting for shutdown, and cleanup. Check scaladoc for more details.

## Integration with `core`

By using of the `RestAPI` (like `JsonRestAPI`) subclasses you get automatic translation of the exceptions defined in [`busymachines-commons-core`](../core) to a specific response code, with a proper presentation for the [[ErrorMessage]].

The mappings between the type of exception/error-message and response codes can be seen in [`busymachines.rest.RestAPI#semanticallyMeaningfulHandler`](./rest-core/src/main/scala/busymachines/rest/RestAPI.scala#126).

You will probably notice that `ForbiddenFailure` is mapped to a `404 NotFound` status code, and `DeniedFailure` is mapped to `403 Forbidden` status code. This is because the status codes in the HTTP method are poorly named to begin with.

This is the copy-pasted partial function from the code linked above:
```scala
  /**
    * Check the scaladoc for each of these failures in case something is not clear,
    * but for convenience that scaladoc has been copied here as well.
    */
    ExceptionHandler {
    /**
      * Meaning:
      *
      * "you cannot find something; it may or may not exist, and I'm not going
      * to tell you anything else"
      */
    case _: NotFoundFailure =>
      failure(StatusCodes.NotFound)

    /**
      * Meaning:
      *
      * "it exists, but you're not even allowed to know about that;
      * so for short, you can't find it".
      */
    case _: ForbiddenFailure =>
      failure(StatusCodes.NotFound)

    /**
      * Meaning:
      *
      * "something is wrong in the way you authorized, you can try again slightly
      * differently"
      */
    case e: UnauthorizedFailure =>
      failure(StatusCodes.Unauthorized, e)

    case e: DeniedFailure =>
      failure(StatusCodes.Forbidden, e)


    /**
      * Obviously, whenever some input data is wrong.
      *
      * This one is probably your best friend, and the one you
      * have to specialize the most for any given problem domain.
      * Otherwise you just wind up with a bunch of nonsense, obtuse
      * errors like:
      * - "the input was wrong"
      * - "gee, thanks, more details, please?"
      * - sometimes you might be tempted to use NotFound, but this
      * might be better suited. For instance, when you are dealing
      * with a "foreign key" situation, and the foreign key is
      * the input of the client. You'd want to be able to tell
      * the user that their input was wrong because something was
      * not found, not simply that it was not found.
      *
      * Therefore, specialize frantically.
      */
    case e: InvalidInputFailure =>
      failure(StatusCodes.BadRequest, e)

    /**
      * Special type of invalid input.
      *
      * E.g. when you're duplicating something that ought to be unique,
      * like ids, emails.
      */
    case e: ConflictFailure =>
      failure(StatusCodes.Conflict, e)

    /**
      * This might be a stretch of an assumption, but usually there's no
      * reason to accumulate messages, except in cases of input validation
      */
    case es: FailureMessages =>
      failures(StatusCodes.BadRequest, es)

    case e: Error =>
      failure(StatusCodes.InternalServerError, e)

    case e: NotImplementedError =>
      failure(StatusCodes.NotImplemented, Error(e))
  }
```
