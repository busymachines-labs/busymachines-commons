/**
  * Copyright (c) 2017-2018 BusyMachines
  *
  * See company homepage at: https://www.busymachines.com/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
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
        assert(failThr.asTry(_ => ForbiddenFailure) == Try.fail(ForbiddenFailure))
      }

      test("left — anomaly") {
        assert(failAno.asTry(_ => ForbiddenFailure) == Try.fail(ForbiddenFailure))
      }

      test("right") {
        assert(failThr.asTry(_ => ano) == Try.fail(ano))
      }
    }

    describe("asTryThr — ev") {
      test("left — throwable") {
        assertThrows[RuntimeException](failThr.asTryThr.unsafeGet())
      }

      test("left — anomaly") {
        assertThrows[InvalidInputFailure](failAno.asTryThr.unsafeGet())
      }

      test("right") {
        assert(pureV.asTryThr.unsafeGet() == 42)
      }
    }

    describe("asTryThr — transform") {
      test("left — throwable") {
        assertThrows[IllegalArgumentException](failThr.asTryThr(_ => iae).unsafeGet())
      }

      test("left — anomaly") {
        assertThrows[IllegalArgumentException](failAno.asTryThr(_ => iae).unsafeGet())
      }

      test("right") {
        assert(pureV.asTryThr(_ => iae).unsafeGet() == 42)
      }
    }

    describe("asResult") {
      test("left — throwable") {
        assertThrows[InvalidInputFailure](failThr.asResult(_ => ano).unsafeGet())
      }

      test("left — anomaly") {
        assertThrows[ForbiddenFailure](failAno.asResult(_ => ForbiddenFailure).unsafeGet())
      }

      test("right") {
        assert(pureV.asResult(_ => ForbiddenFailure).unsafeGet() == 42)
      }
    }

    describe("asResultThr") {
      test("left — throwable") {
        assertThrows[CatastrophicError](failThr.asResultThr.unsafeGet())
      }

      test("left — anomaly") {
        assertThrows[InvalidInputFailure](failAno.asResultThr.unsafeGet())
      }

      test("right") {
        assert(pureV.asResultThr.unsafeGet() == 42)
      }
    }

    describe("unsafeGetLeft") {
      test("left") {
        val thr = failAno.unsafeGetLeft()
        assert(thr.isInstanceOf[InvalidInputAnomaly], "invalid anomaly type")
        assert(thr.asInstanceOf[InvalidInputAnomaly] == ano)
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
