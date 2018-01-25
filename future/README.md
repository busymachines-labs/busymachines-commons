# busymachines-commons-future

[![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-future_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-future_2.12)



## artifacts

* stable: `N/A`
* latest: `0.3.0-M2`

`"com.busymachines" %% "busymachines-commons-future" % "0.3.0-M2"`

### Transitive dependencies
- busymachines-commons-result
- cats-effects 0.8.0
- cats-core 1.0.1

## How it works

Provides convenient `scala.concurrent.Future` related syntax for 99% of your use cases under one single `import busymachines.future`.

Documentation still under construction. You can glance over this thing:

```scala
package busymachines.playground

import busymachines.future._
import busymachines.result._

object FutureMain extends App {
  implicit val ex: ExecutionContext =
    ExecutionContext.global

  var x: Int = 42

  val noSideEffects = for {
    _ <- false.effectOnTrue(
          Future {
            x = 11
          }
        )
    _ <- true.effectOnFalse(
          Future {
            x = 12
          }
        )
  } yield ()

  noSideEffects.syncAwaitReady()
  if (x != 42) throw new RuntimeException(s"effect was run, damn, new value = $x, suspend the future!")

  //================= suspend in IO =====================

  var i = 0

  def f(s: String): Future[Int] =
    Future {
      Thread.sleep(1000)
      println(
        s"\n-------- $s FUTURE: prev=$i ------ \n"
      )
      i = i + 1
      i
    }

  val io = f("IO").asIO

  println(
    "----- waiting for future -----"
  )
  println {
    f("impure").syncUnsafeGet()
  }

  println(
    "----- NO SIDE-EFFECTS FROM IO UNTIL NOW -----"
  )
  println {
    io.unsafeRunSync()
  }

  //===========================================================================
  //===========================================================================
  //===========================================================================

  val result = Result("string result!")
  val either: Either[RuntimeException, String] = Left(new RuntimeException("sdfsdgs"))
  //ensure that there is no ambiguity @ compilation even though these are methods on different implicit ops
  result.asFuture
  either.asFuture
}

```