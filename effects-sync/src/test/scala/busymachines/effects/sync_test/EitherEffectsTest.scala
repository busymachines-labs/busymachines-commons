package busymachines.effects.sync_test

import busymachines.core._
import busymachines.effects.sync._
import org.scalatest.FunSpec

import cats.syntax.either._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 29 Jan 2018
  *
  */
class EitherEffectsTest extends FunSpec {
  //prevents atrocious English
  private def test: ItWord = it

  //--------------------------------------------------------------------------

  private val thr: RuntimeException         = new RuntimeException("runtime_exception")
  private val iae: IllegalArgumentException = new IllegalArgumentException("illegal_argument_exception")
  private val ano: InvalidInputAnomaly      = InvalidInputFailure("invalid_input_failure")

  private val failAno: Either[Throwable, Int] = Either.left(ano.asThrowable)
  private val failThr: Either[Throwable, Int] = Either.left(thr)
  private val pureV:   Either[Throwable, Int] = Either.right(42)

  //--------------------------------------------------------------------------

  describe("Either — reference ops") {
    describe("asOptionUnsafe") {
      test("left — throwable") {
        assert(failThr.asOptionUnsafe() == None)
      }

      test("left — anomaly") {
        assert(failAno.asOptionUnsafe() == None)
      }

      test("right") {
        assert(pureV.asOptionUnsafe() == Option(42))
      }
    }

    describe("asListUnsafe") {
      test("left — throwable") {
        assert(failThr.asListUnsafe() == List())
      }

      test("left — anomaly") {
        assert(failAno.asListUnsafe() == List())
      }

      test("right") {
        assert(pureV.asListUnsafe() == List(42))
      }
    }

    describe("asTry") {
      test("left — throwable") {
        assert(failThr.asTry(t => ForbiddenFailure) == Try.fail(ForbiddenFailure))
      }

      test("left — anomaly") {
        assert(failAno.asTry(t => ForbiddenFailure) == Try.fail(ForbiddenFailure))
      }

      test("right") {
        assert(failThr.asTry(t => ano) == Try.fail(ano))
      }
    }

    describe("asTryWeak — ev") {
      test("left — throwable") {
        assertThrows[RuntimeException](failThr.asTryWeak.unsafeGet())
      }

      test("left — anomaly") {
        assertThrows[InvalidInputFailure](failAno.asTryWeak.unsafeGet())
      }

      test("right") {
        assert(pureV.asTryWeak.unsafeGet() == 42)
      }
    }

    describe("asTryWeak — transform") {
      test("left — throwable") {
        assertThrows[IllegalArgumentException](failThr.asTryWeak(t => iae).unsafeGet())
      }

      test("left — anomaly") {
        assertThrows[IllegalArgumentException](failAno.asTryWeak(t => iae).unsafeGet())
      }

      test("right") {
        assert(pureV.asTryWeak(t => iae).unsafeGet() == 42)
      }
    }

    describe("asResult") {
      test("left — throwable") {
        assertThrows[InvalidInputFailure](failThr.asResult(t => ano).unsafeGet())
      }

      test("left — anomaly") {
        assertThrows[ForbiddenFailure](failAno.asResult(t => ForbiddenFailure).unsafeGet())
      }

      test("right") {
        assert(pureV.asResult(t => ForbiddenFailure).unsafeGet() == 42)
      }
    }

    describe("asResultWeak") {
      test("left — throwable") {
        assertThrows[CatastrophicError](failThr.asResultWeak.unsafeGet())
      }

      test("left — anomaly") {
        assertThrows[InvalidInputFailure](failAno.asResultWeak.unsafeGet())
      }

      test("right") {
        assert(pureV.asResultWeak.unsafeGet() == 42)
      }
    }

    describe("unsafeGetLeft") {
      test("left") {
        assert(failAno.unsafeGetLeft() == ano)
      }

      test("right") {
        assertThrows[NoSuchElementException](pureV.unsafeGetLeft())
      }
    }

    describe("unsafeGetRight") {
      test("left") {
        assertThrows[NoSuchElementException](failAno.unsafeGetRight())

      }

      test("right") {
        assert(pureV.unsafeGetRight() == 42)
      }
    }

  }

}
