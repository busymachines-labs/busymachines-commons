# busymachines-commons-rest-json

[![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-rest-json_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-rest-json_2.12)

## artifacts

Current version is `0.2.0-RC8`. SBT module id:
`"com.busymachines" %% "busymachines-commons-rest-json" % "0.2.0-RC8"`

### Transitive dependencies
- busymachines-commons-core
- busymachines-commons-json
- akka-http 10.0.10
- akka-actor 2.5.4
- akka-stream 2.5.4
- cats-effects 0.6.0
- cats-core 1.0.0-RC2
- circe 0.9.0-M3
- akka-http-circe 1.19.0-M3

## Description

This is a reified version of `busymachines-commons-rest-core`. You can immediately start using it by simply inheriting `busymachines.rest.JsonRestAPI`.

## Examples

Examples of usage are rather verbose so you'll have to check the tests in the [`busymachines-commons-rest-json-testkit`](`./rest-json-testkit`) module.

### `WebServerIO`

A convenient, pure way based on `cats.effects.IO` of binding your server to the network interface. Fully functioning example:

```scala

package busymachines.rest.playground

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import busymachines.rest._

import scala.concurrent.ExecutionContext

object MainRestPlaygroundApp extends App {

  implicit val as: ActorSystem = ActorSystem("my-system")
  implicit val am: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = as.dispatcher

  val restAPI = new HelloWorld()

  val httpServer = WebServerIO.`bind->handleRequests->wait to stop->unbind->close actor system`(
    restAPI, MinimalWebServerConfig.default
  )

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
— server online @ http://localhost:9999
— press RETURN to stop...
   #### after you press return: ####
— stopping...
— unbinding address @ localhost:9999
— closing actor system: my-system
```

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
