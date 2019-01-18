---
layout: docs
title: duration
---
[![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-duration_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-duration_2.12)

# busymachines-commons-duration

Thin wrapper around `scala.concurrent.duration` and `java.util.concurrent.TimeUnit`.

## artifacts

This module is vanilla scala _*only*_, compiled for scala version `2.12.8`

* stable: `N/A`
* latest: `0.3.0-RC9`

```scala
"com.busymachines" %% "busymachines-commons-duration" % "0.3.0-RC9"
```

### Transitive dependencies
None.

## Description

Check tests for examples. Copy pasted for convenience:

```scala
import busymachines.{duration => d}
import org.scalatest.FunSpec

class DurationTest extends FunSpec {
  private def test: ItWord = it

  describe("duration") {

    test("nanos") {
      assert(d.nanos(1L).toNanos == 1L)
    }

    test("micros") {
      assert(d.micros(1L).toMicros == 1L)
    }

    test("millis") {
      assert(d.millis(1L).toMillis == 1L)
    }

    test("seconds") {
      assert(d.seconds(1L).toSeconds == 1L)
    }

    test("minutes") {
      assert(d.minutes(1L).toMinutes == 1L)
    }

    test("hours") {
      assert(d.hours(1L).toHours == 1L)
    }

    test("days") {
      assert(d.days(1L).toDays == 1L)
    }
  }

}

```
