# busymachines-commons-rest-core

## artifacts

Current version is `0.2.0-M4`. SBT module id:
`"com.busymachines" %% "busymachines-commons-rest-core" % "0.2.0-M4"`

### Transitive dependencies
- busymachines-commons-core
- akka-http 10.0.10
- akka-actor 2.5.4
- akka-stream 2.5.4

## Description

This library is built on top of `akka-http`. It provides a principled way of defining
rest APIs routes via the single exposed trait `RestAPI`. It is still an abstract library,
you will probably be using one of its concrete variants `JsonRestAPI` from the [`busymachines-commons-rest-json`](../rest-json) module. This module only serves as the common core for the
implementation of these modules.

This is what you roughly get:
 - a semantically meaningful exception handler that maps the exceptions from `core` to HTTP status codes—at the same time providing a consistent way of reporting the information contained in these rich exceptions
 - a simple way of starting to define endpoints in the akka scala dsl
 - out of the box implementations for authentication directives. See the traits in `busymachines.rest.RestAPIAuthentications`
 - potential integration with `JSON` via the `busymachines-commons-rest-json` module

## Examples

Examples of usage are rather verbose so you'll have to check the tests in the [`busymachines-commons-rest-json-testkit`](`./rest-json-testkit`) module.

## Integration with `core`

By using of the `RestAPI` subclasses you get automatic translation of the exceptions defined in [`busymachines-commons-core`](../core) to a specific response code, with a proper presentation for the [[ErrorMessage]].

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
