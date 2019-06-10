package busymachines.duration_test

import busymachines.duration.FiniteDuration
import busymachines.{duration => d}
import org.scalatest.funspec.AnyFunSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 01 Feb 2018
  *
  */
class DurationTest extends AnyFunSpec {
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

    test("FinisteDuration alias") {
      assert(FiniteDuration.apply(1L, d.TimeUnits.Nanos).toNanos == 1L)
    }
  }

}
