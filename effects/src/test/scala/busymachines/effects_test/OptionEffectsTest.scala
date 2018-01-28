package busymachines.effects_test

import busymachines.core._
import busymachines.effects._
import org.scalatest._
import busymachines.scalatest.FunSpecAlias

import scala.util.Failure
import scala.util.Try

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Jan 2018
  *
  */
final class OptionEffectsTest extends FunSpecAlias with Matchers {
  private implicit val sc: Scheduler = Scheduler.global

  private val anomaly:   InvalidInputFailure = InvalidInputFailure("invalid_input_failure")
  private val throwable: RuntimeException    = new RuntimeException("runtime_exception")

  private val failedTryThrowable: Try[Int] = Failure(throwable)
  private val failedTryAnomaly:   Try[Int] = Failure(anomaly)
  private val tryInt:             Try[Int] = scala.util.Success(42)

  private val incorrect: Result[Int] = Result.fail(anomaly)
  private val correct:   Result[Int] = Result(42)

  private val none: Option[Int] = Option.empty
  private val some: Option[Int] = Option(42)

  private def sleep(time: Int = 20): Unit = Thread.sleep(time.toLong)

  describe("Option — companion object syntax — conversion to other effects") {
    describe("Option.asList") {
      test("some") {
        val opt = Option(42)
        assert(Option.asList(opt) == List(42))
      }

      test("none") {
        val opt = none
        assert(Option.asList(opt) == List.empty[Int])
      }
    }

    describe("Option.asTry") {
      test("some") {
        val opt = some
        assert(Option.asTry(opt, anomaly) == Try(42))
      }

      test("none") {
        val opt = none
        the[InvalidInputFailure] thrownBy {
          Option.asTry(opt, anomaly).get
        }
      }
    }

    describe("Option.asTryWeak") {
      test("some") {
        val opt = some
        assert(Option.asTryWeak(opt, anomaly) == Try(42))
      }

      test("none") {
        val opt = none
        the[InvalidInputFailure] thrownBy {
          Option.asTryWeak(opt, anomaly).get
        }
      }
    }

    describe("Option.asResult") {
      test("some") {
        val opt = some
        assert(Option.asResult(opt, anomaly).unsafeGet == 42)
      }

      test("none") {
        val opt = none
        the[InvalidInputFailure] thrownBy {
          Option.asResult(opt, anomaly).unsafeGet
        }
      }
    }

    describe("Option.asIO") {
      test("some") {
        val opt = some
        assert(Option.asIO(opt, anomaly).unsafeRunSync() == 42)
      }

      test("none") {
        val opt = none
        the[InvalidInputFailure] thrownBy {
          Option.asIO(opt, anomaly).unsafeRunSync()
        }
      }
    }

    describe("Option.asTask") {
      test("some") {
        val opt = some
        assert(Option.asTask(opt, anomaly).syncUnsafeGet() == 42)
      }

      test("none") {
        val opt = none
        the[InvalidInputFailure] thrownBy {
          Option.asTask(opt, anomaly).syncUnsafeGet()
        }
      }
    }

    describe("Option.asFuture") {
      test("some") {
        val opt = some
        assert(Option.asFuture(opt, anomaly).syncUnsafeGet() == 42)
      }

      test("none") {
        val opt = none
        the[InvalidInputFailure] thrownBy {
          Option.asFuture(opt, anomaly).syncUnsafeGet()
        }
      }
    }

  }

  describe("Option — infix object syntax — conversion to other effects") {
    describe(".asList") {
      test("some") {
        val opt = Option(42)
        assert(opt.asList == List(42))
      }

      test("none") {
        val opt = none
        assert(opt.asList == List.empty[Int])
      }
    }

    describe(".asTry") {
      test("some") {
        val opt = some
        assert(opt.asTry(anomaly) == Try(42))
      }

      test("none") {
        val opt = none
        the[InvalidInputFailure] thrownBy {
          opt.asTry(anomaly).get
        }
      }
    }

    describe(".asTryWeak") {
      test("some") {
        val opt = some
        assert(opt.asTryWeak(anomaly) == Try(42))
      }

      test("none") {
        val opt = none
        the[InvalidInputFailure] thrownBy {
          Option.asTryWeak(opt, anomaly).get
        }
      }
    }

    describe(".asResult") {
      test("some") {
        val opt = some
        assert(opt.asResult(anomaly).unsafeGet == 42)
      }

      test("none") {
        val opt = none
        the[InvalidInputFailure] thrownBy {
          opt.asResult(anomaly).unsafeGet
        }
      }
    }

    describe(".asIO") {
      test("some") {
        val opt = some
        assert(opt.asIO(anomaly).unsafeRunSync() == 42)
      }

      test("none") {
        val opt = none
        the[InvalidInputFailure] thrownBy {
          opt.asIO(anomaly).unsafeRunSync()
        }
      }
    }

    describe(".asTask") {
      test("some") {
        val opt = some
        assert(opt.asTask(anomaly).syncUnsafeGet() == 42)
      }

      test("none") {
        val opt = none
        the[InvalidInputFailure] thrownBy {
          opt.asTask(anomaly).syncUnsafeGet()
        }
      }
    }

    describe("Option.asFuture") {
      test("some") {
        val opt = some
        assert(opt.asFuture(anomaly).syncUnsafeGet() == 42)
      }

      test("none") {
        val opt = none
        the[InvalidInputFailure] thrownBy {
          opt.asFuture(anomaly).syncUnsafeGet()
        }
      }
    }

  }

}
