# busymachines-commons-rest-json-testkit

[![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-rest-json-testkit_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-rest-json-testkit_2.12)

## artifacts

* stable: `0.2.0`
* latest: `0.3.0-M2`

`"com.busymachines" %% "busymachines-commons-rest-json-testkit" % "0.2.0" % test`

N.B. that this is a testing library, and you should only depend on it in test. Because otherwise you wind up with scalatest and akka http testing libraries on your runtime classpath.

### Transitive dependencies
- busymachines-commons-core
- busymachines-commons-rest-core
- busymachines-commons-rest-json
- akka-http 10.0.11
- akka-actor 2.5.8
- akka-stream 2.5.8
- cats 1.0.1
- circe 0.9.0
- akka-http-circe 1.19.0
- akka-http-testkit 10.0.11
- scalatest 3.0.4

## Description

The testing companion of [`busymachines-commons-rest-json`]. And it is fully useable with no abstract stuff like its parent `busymachines-commons-rest-core`.

Check out the tests of this module for full running examples.

## Debugging

The coolest thing in the testing DSL is the `debug` method that you use roughly like:

```scala
it should "... return 200 OK when providing proper Basic authentication" in { _ =>

  context(BasicAuthenticationContextForTesting) { implicit cc =>
    debug {
      get("/authentication") {
        expectStatus(StatusCodes.OK)

        assert {
          responseAs[SomeTestDTOGet] ==
            SomeTestDTOGet(int = 42, string = "dXNlcm5hbWU6cGFzc3dvcmQ=", None)
        }
      }

      get("/this/route/does/not/exist") {
        expectStatus(StatusCodes.NotFound)
      }
    }
  }
}
```

By running the above, it will print the request/responses of each request within the `debug` block:
```
GET /authentication
--
Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=--
----> Response <----
Status: 200
==
{
  "int" : 42,
  "string" : "dXNlcm5hbWU6cGFzc3dvcmQ="
}
===================================================

GET /this/route/does/not/exist
--
Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=--
----> Response <----
Status: 404
==
The requested resource could not be found.
===================================================
```
This is really helpful when:
 - you have system tests with tens of requests, but only want to inspect one single one, instead of being flooded with a bunch of spam output from a logger
 - you want to get a quick example of a request pasted somewhere to a fellow developer

## Bird's eye view

The final results looks roughly like:
```scala
private[rest_json_test] object AuthenticationsForTest {
  private[rest_json_test] lazy val basic = CallerContexts.basic("username", "password")
  private[rest_json_test] lazy val bearer = CallerContexts.bearer("D2926169E98AAA4C6B40C8C7AF7F4122946DDFA4E499908C")
}

private[rest_json_test] class BasicAuthenticatedRoutesTest extends extends FlatSpec with JsonRestAPITest {

    override implicit protected def testedRoute: Route = {
      val authAPI = new BasicAuthenticatedRoutesRestAPIForTesting()
      val r: RestAPI = RestAPI.seal(authAPI)
      r.route
    }

    import busymachines.json._
    //  this also works, and gets us faster compilation times:
    //  import SomeTestDTOJsonCodec._

    //===========================================================================

    behavior of "Basic Authentication"

    //===========================================================================

    it should "... return 401 Unauthorized when trying to access route without authentication" in { _ =>
      context(Contexts.none) { implicit cc =>
        get("/basic_authentication") {
          expectStatus(StatusCodes.Unauthorized)
        }
      }
    }

    //===========================================================================

    it should "... return 200 OK when providing proper Basic authentication" in { _ =>
      context(AuthenticationsForTest.basic) { implicit cc =>
        get("/basic_authentication") {
          expectStatus(StatusCodes.OK)

          assert {
            responseAs[SomeTestDTOGet] ==
              SomeTestDTOGet(int = 42, string = "dXNlcm5hbWU6cGFzc3dvcmQ=", None)
          }
        }
      }
    }

    //===========================================================================

    it should "... return 200 OK when trying to access API with optional auth, while not providing it" in { _ =>
      context(Contexts.none) { implicit cc =>
        get("/basic_opt_authentication") {
          expectStatus(StatusCodes.OK)

          assert {
            responseAs[SomeTestDTOGet] ==
              SomeTestDTOGet(int = 42, string = "it's optional!", None)
          }
        }
      }
    }

    //===========================================================================

    it should "... return 200 OK when trying to access API with optional auth, while providing it" in { _ =>
      context(AuthenticationsForTest.basic) { implicit cc =>
        get("/basic_opt_authentication") {
          expectStatus(StatusCodes.OK)

          assert {
            responseAs[SomeTestDTOGet] ==
              SomeTestDTOGet(int = 42, string = "dXNlcm5hbWU6cGFzc3dvcmQ=", None)
          }
        }
      }
    }

    //===========================================================================
  }

```
