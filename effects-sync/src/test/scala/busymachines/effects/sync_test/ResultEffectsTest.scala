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

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Jan 2018
  *
  */
final class ResultEffectsTest extends FunSpec {
  //prevents atrocious English
  private def test: ItWord = it

  implicit private class TestSyntax[T](value: Result[T]) {
    //short for "run"
    def r: T = value.unsafeGet()
  }

  //--------------------------------------------------------------------------

  private val thr: RuntimeException    = new RuntimeException("runtime_exception")
  private val ano: InvalidInputAnomaly = InvalidInputFailure("invalid_input_failure")

  private val none: Option[Int] = Option.empty
  private val some: Option[Int] = Option(42)

  private val leftThr: Either[Throwable, Int] = Left(thr)
  private val leftAno: Either[Throwable, Int] = Left(ano.asThrowable)
  private val right:   Either[Throwable, Int] = Right(42)

  private val success: Try[Int] = Try(42)
  private val failure: Try[Int] = Try.fail(ano)

  private val int2str: Int => String = i => i.toString

  private val thr2ano: Throwable => Anomaly = _ => ForbiddenFailure
  private val ano2ano: Anomaly => Anomaly   = _ => ForbiddenFailure
  private val ano2str: Anomaly => String    = thr => thr.message

  private val failV: Result[Int] = Result.fail(ano)
  private val pureV: Result[Int] = Result.pure(42)

  private val btrue:  Result[Boolean] = Result.pure(true)
  private val bfalse: Result[Boolean] = Result.pure(false)
  private val bfail:  Result[Boolean] = Result.fail(DeniedFailure)

  //---------------------------------------------------------------------------

  describe("Result — companion object syntax") {

    describe("constructors") {
      test("pure") {
        assert(Result.pure(42).unsafeGet() == 42)
        assert(Result.correct(42).unsafeGet() == 42)
        assert(Try(42).unsafeGet() == 42)
      }

      test("fail") {
        assertThrows[InvalidInputFailure](Result.fail(ano).r)

        assertThrows[InvalidInputFailure](Result.incorrect(ano).r)
      }

      test("failThr") {
        assertThrows[CatastrophicError](Result.failThr(thr).r)
        assertThrows[InvalidInputFailure](Result.failThr(ano.asThrowable).r)

        assertThrows[CatastrophicError](Result.incorrectThr(thr).r)
        assertThrows[InvalidInputFailure](Result.incorrectThr(ano.asThrowable).r)
      }

      test("apply") {
        assert(Result(42).unsafeGet() == 42)

        assertThrows[InvalidInputFailure](Result(throw ano.asThrowable).unsafeGet() == 42)
        assertThrows[CatastrophicError](Result(throw thr).unsafeGet() == 42)
      }

      test("unit") {
        assert(Result.unit == Result.unit)
      }

      describe("fromOption") {
        test("none") {
          assertThrows[InvalidInputFailure](Result.fromOption(none, ano).r)
        }

        test("some") {
          assert(Result.fromOption(some, ano).r == 42)
        }
      }

      describe("fromEither") {
        test("leftThr") {
          assertThrows[CatastrophicError](Result.fromEitherThr(leftThr).r)
        }

        test("leftAno") {
          assertThrows[InvalidInputFailure](Result.fromEitherThr(leftAno).r)
        }

        test("left — transform") {
          assertThrows[ForbiddenFailure](Result.fromEither(leftThr, thr2ano).r)
        }

        test("right") {
          assert(Result.fromEitherThr(right).r == 42)
        }

        test("right — transform") {
          assert(Result.fromEither(right, thr2ano).r == 42)
        }
      }

      describe("fromTry") {
        test("failure — anomaly") {
          assertThrows[InvalidInputFailure](Result.fromTry(failure).r)
        }

        test("failure — exception") {
          assertThrows[CatastrophicError](Result.fromTry(Try.failureThr(thr)).r)
        }

        test("success") {
          assert(Result.fromTry(success).r == 42)
        }

      }

    } //end constructors

    describe("boolean") {

      describe("cond") {
        test("false") {
          val value = Result.cond(
            false,
            42,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("true") {
          val value = Result.cond(
            true,
            42,
            ano
          )
          assert(value.r == 42)
        }
      }

      describe("condWith") {
        test("false — pure") {
          val value = Result.condWith(
            false,
            pureV,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("true — pure") {
          val value = Result.condWith(
            true,
            pureV,
            ano
          )
          assert(value.r == 42)
        }

        test("false — fail") {
          val value = Result.condWith(
            false,
            failV,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("true — fail") {
          val value = Result.condWith(
            true,
            failV,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }
      }

      describe("flatCond") {
        test("false") {
          val value = Result.flatCond(
            bfalse,
            42,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("true") {
          val value = Result.flatCond(
            btrue,
            42,
            ano
          )
          assert(value.r == 42)
        }

        test("fail") {
          val value = Result.flatCond(
            bfail,
            42,
            ano
          )
          assertThrows[DeniedFailure](value.r)
        }
      }

      describe("flatCondWith") {
        test("false — pure") {
          val value = Result.flatCondWith(
            bfalse,
            pureV,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("false — fail") {
          val value = Result.flatCondWith(
            bfalse,
            failV,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("true — pure") {
          val value = Result.flatCondWith(
            btrue,
            pureV,
            ano
          )
          assert(value.r == 42)
        }

        test("true — fail") {
          val value = Result.flatCondWith(
            btrue,
            failV,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("fail — pure") {
          val value = Result.flatCondWith(
            bfail,
            pureV,
            ano
          )
          assertThrows[DeniedFailure](value.r)
        }

        test("fail — fail") {
          val value = Result.flatCondWith(
            bfail,
            failV,
            ano
          )
          assertThrows[DeniedFailure](value.r)
        }
      }

      describe("failOnTrue") {
        test("false") {
          val value = Result.failOnTrue(
            false,
            ano
          )
          value.r
        }

        test("true") {
          val value = Result.failOnTrue(
            true,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }
      }

      describe("failOnFalse") {
        test("false") {
          val value = Result.failOnFalse(
            false,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("true") {
          val value = Result.failOnFalse(
            true,
            ano
          )
          value.r
        }
      }

      describe("flatFailOnTrue") {
        test("false") {
          val value = Result.flatFailOnTrue(
            bfalse,
            ano
          )
          value.r
        }

        test("true") {
          val value = Result.flatFailOnTrue(
            btrue,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("fail") {
          val value = Result.flatFailOnTrue(
            bfail,
            ano
          )
          assertThrows[DeniedFailure](value.r)
        }

      }

      describe("flatFailOnFalse") {
        test("false") {
          val value = Result.flatFailOnFalse(
            bfalse,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("true") {
          val value = Result.flatFailOnFalse(
            btrue,
            ano
          )
          value.r
        }

        test("fail") {
          val value = Result.flatFailOnFalse(
            bfail,
            ano
          )
          assertThrows[DeniedFailure](value.r)
        }

      }

    } //end boolean

    describe("nested") {

      describe("unpack") {
        test("pure — none") {
          assertThrows[InvalidInputAnomaly] {
            Result.unpackOption(Result.pure(none), ano).r
          }
        }

        test("pure — some") {
          assert(Result.unpackOption(Result.pure(some), ano).r == 42)
        }

        test("fail") {
          assertThrows[DeniedFailure] {
            Result.unpackOption(Result.fail[Option[Int]](DeniedFailure), ano).r
          }
        }
      }

    } //end nested

    describe("as{Effect}") {

      describe("asOptionUnsafe") {

        test("fail") {
          assertThrows[InvalidInputFailure](
            Result.asOptionUnsafe(failV)
          )
        }

        test("pure") {
          assert(Result.asOptionUnsafe(pureV) == some)
        }

      }

      describe("asListUnsafe") {

        test("fail") {
          assertThrows[InvalidInputFailure](
            Result.asListUnsafe(failV)
          )
        }

        test("pure") {
          assert(Result.asListUnsafe(pureV) == List(42))
        }

      }

      describe("asTry") {

        test("fail") {
          assert(Result.asTry(failV) == failure)
        }

        test("pure") {
          assert(Result.asTry(pureV) == success)
        }

      }

      describe("unsafeGet") {

        test("fail") {
          assertThrows[InvalidInputFailure](Result.unsafeGet(failV))
        }

        test("pure") {
          assert(Result.unsafeGet(pureV) == 42)
        }

      }

    } //end as{Effect}

    describe("transformers") {

      describe("bimap") {

        test("fail") {
          val value = Result.bimap(
            failV,
            int2str,
            ano2ano
          )

          assertThrows[ForbiddenFailure](value.r)
        }

        test("pure") {
          val value = Result.bimap(
            pureV,
            int2str,
            ano2ano
          )

          assert(value.r == "42")
        }

      }

      describe("morph") {

        test("fail") {
          val value = Result.morph(
            failV,
            int2str,
            ano2str
          )
          assert(value.r == ano.message)
        }

        test("pure") {
          val value = Result.morph(
            pureV,
            int2str,
            ano2str
          )
          assert(value.r == "42")
        }
      }

      describe("recover") {

        test("fail — isDefined") {
          val value = Result.fail[Int](DeniedFailure).recover {
            case _: DeniedFailure => 42
          }
          assert(value.r == 42)
        }

        test("fail — is not Defined") {
          val value = Result.fail[Int](DeniedFailure).recover {
            case _: InvalidInputFailure => 42
          }
          assertThrows[DeniedFailure](value.r)
        }

        test("pure — N/A") {
          val value = Result[Int](42).recover {
            case _: InvalidInputFailure => 42
          }
          assert(value.r == 42)
        }

      }

      describe("recoverWith") {

        test("fail — isDefined — pure") {
          val value = Result.fail[Int](DeniedFailure).recoverWith {
            case _: DeniedFailure => Result(42)
          }
          assert(value.r == 42)
        }

        test("fail — isDefined — fail") {
          val value = Result.fail[Int](DeniedFailure).recoverWith {
            case _: DeniedFailure => Result.fail(InvalidInputFailure)
          }
          assertThrows[InvalidInputFailure](value.r)
        }

        test("fail — is not Defined — fail ") {
          val value = Result.fail[Int](DeniedFailure).recoverWith {
            case _: InvalidInputFailure => Result.fail(InvalidInputFailure)
          }
          assertThrows[DeniedFailure](value.r)
        }

        test("fail — is not Defined — pure ") {
          val value = Result.fail[Int](DeniedFailure).recoverWith {
            case _: InvalidInputFailure => Result(42)
          }
          assertThrows[DeniedFailure](value.r)
        }

        test("pure — N/A — pure") {
          val value = Result[Int](42).recoverWith {
            case _: InvalidInputFailure => Result(11)
          }
          assert(value.r == 42)
        }
      }

      describe("discardContent") {

        test("fail") {
          assertThrows[InvalidInputFailure](Result.discardContent(failV).r)
        }

        test("pure") {
          Result.discardContent(pureV).r
        }
      }

    } //end transformers

    describe("traversals") {

      describe("Result.traverse") {

        test("empty list") {
          val input:    Seq[Int] = List()
          val expected: Seq[Int] = List()

          var sideEffect: Int = 0

          val result = Result.traverse(input) { _ =>
            Result {
              sideEffect = 42
            }
          }

          assert(result.r == expected)
          assert(sideEffect == 0, "nothing should have happened")
        }

        test("non empty list") {
          val input: Seq[Int] = (1 to 100).toList
          val expected = input.map(_.toString)

          val result: Result[Seq[String]] = Result.traverse(input) { i =>
            Result.pure(i.toString)
          }
          assert(expected == result.r)
        }

      }

      describe("Result.traverse_") {

        test("empty list") {
          val input: Seq[Int] = List()

          var sideEffect: Int = 0

          val result = Result.traverse_(input) { _ =>
            Result {
              sideEffect = 42
            }
          }

          assert(result == Result.unit)
          assert(sideEffect == 0, "nothing should have happened")
        }

        test("non empty list") {
          val input: Seq[Int] = (1 to 100).toList

          val result: Result[Unit] = Result.traverse_(input) { i =>
            Result.pure(i.toString)
          }
          assert(Result.unit == result)
        }

      }

      describe("Result.sequence") {

        test("empty list") {
          val input:    Seq[Result[Int]] = List()
          val expected: Seq[Int]         = List()

          val result = Result.sequence(input)
          assert(result.r == expected)
        }

        test("non empty list") {
          val nrs = (1 to 100).toList
          val input: Seq[Result[Int]] = (1 to 100).toList.map(Result.pure)
          val expected = nrs.map(_.toString)

          val result: Result[Seq[String]] = Result.sequence {
            input map { tr =>
              tr.map(i => i.toString)
            }
          }
          assert(expected == result.r)
        }

      }

      describe("Result.sequence_") {
        test("empty list") {
          val input: Seq[Result[Int]] = List()

          val result = Result.sequence_(input)
          assert(result == Result.unit)
        }

        test("non empty list") {
          val input: Seq[Result[Int]] = (1 to 100).toList.map(Result.pure)

          val result: Result[Unit] = Result.sequence_ {
            input map { tr =>
              tr.map(i => i.toString)
            }
          }
          assert(Result.unit == result)
        }
      }

    } // end traversals

  } //end companion object syntax tests

  //===========================================================================
  //===========================================================================
  //===========================================================================

  describe("Try — reference syntax") {

    describe("boolean") {

      describe("cond") {
        test("false") {
          val value = false.condResult(
            42,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("true") {
          val value = true.condResult(
            42,
            ano
          )
          assert(value.r == 42)
        }
      }

      describe("condWith") {
        test("false — pure") {
          val value = false.condWithResult(
            pureV,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("true — pure") {
          val value = true.condWithResult(
            pureV,
            ano
          )
          assert(value.r == 42)
        }

        test("false — fail") {
          val value = false.condWithResult(
            failV,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("true — fail") {
          val value = true.condWithResult(
            failV,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }
      }

      describe("flatCond") {
        test("false") {
          val value = bfalse.cond(
            42,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("true") {
          val value = btrue.cond(
            42,
            ano
          )
          assert(value.r == 42)
        }

        test("fail") {
          val value = bfail.cond(
            42,
            ano
          )
          assertThrows[DeniedFailure](value.r)
        }
      }

      describe("flatCondWith") {
        test("false — pure") {
          val value = bfalse.condWith(
            pureV,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("false — fail") {
          val value = bfalse.condWith(
            failV,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("true — pure") {
          val value = btrue.condWith(
            pureV,
            ano
          )
          assert(value.r == 42)
        }

        test("true — fail") {
          val value = btrue.condWith(
            failV,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("fail — pure") {
          val value = bfail.condWith(
            pureV,
            ano
          )
          assertThrows[DeniedFailure](value.r)
        }

        test("fail — fail") {
          val value = bfail.condWith(
            failV,
            ano
          )
          assertThrows[DeniedFailure](value.r)
        }
      }

      describe("failOnTrue") {
        test("false") {
          val value = false.failOnTrueResult(ano)
          value.r
        }

        test("true") {
          val value = true.failOnTrueResult(ano)
          assertThrows[InvalidInputFailure](value.r)
        }
      }

      describe("failOnFalse") {
        test("false") {
          val value = false.failOnFalseResult(ano)
          assertThrows[InvalidInputFailure](value.r)
        }

        test("true") {
          val value = true.failOnFalseResult(ano)
          value.r
        }
      }

      describe("flatFailOnTrue") {
        test("false") {
          val value = bfalse.failOnTrue(ano)
          value.r
        }

        test("true") {
          val value = btrue.failOnTrue(ano)
          assertThrows[InvalidInputFailure](value.r)
        }

        test("fail") {
          val value = bfail.failOnTrue(ano)
          assertThrows[DeniedFailure](value.r)
        }

      }

      describe("flatFailOnFalse") {
        test("false") {
          val value = bfalse.failOnFalse(ano)
          assertThrows[InvalidInputFailure](value.r)
        }

        test("true") {
          val value = btrue.failOnFalse(ano)
          value.r
        }

        test("fail") {
          val value = bfail.failOnFalse(ano)
          assertThrows[DeniedFailure](value.r)
        }

      }

    } //end boolean

    describe("nested") {

      describe("unpack") {
        test("pure — none") {
          assertThrows[InvalidInputAnomaly] {
            Result.pure(none).unpack(ano).r
          }
        }

        test("pure — some") {
          assert(Result.pure(some).unpack(ano).r == 42)
        }

        test("fail") {
          assertThrows[DeniedFailure] {
            Result.fail[Option[Int]](DeniedFailure).unpack(ano).r
          }
        }
      }

    } //end nested

    describe("as{Effect}") {

      describe("asOptionUnsafe") {

        test("fail") {
          assertThrows[InvalidInputFailure](
            failV.asOptionUnsafe()
          )
        }

        test("pure") {
          assert(pureV.asOptionUnsafe() == some)
        }

      }

      describe("asListUnsafe") {

        test("fail") {
          assertThrows[InvalidInputFailure](
            failV.asListUnsafe()
          )
        }

        test("pure") {
          assert(pureV.asListUnsafe() == List(42))
        }

      }

      describe("asTry") {

        test("fail") {
          assert(failV.asTry == failure)
        }

        test("pure") {
          assert(pureV.asTry == success)
        }

      }

      describe("unsafeGet") {

        test("fail") {
          assertThrows[InvalidInputFailure](failV.unsafeGet())
        }

        test("pure") {
          assert(pureV.unsafeGet() == 42)
        }

      }

    } //end as{Effect}

    describe("transformers") {

      describe("bimap") {

        test("fail") {
          val value = failV.bimap(
            int2str,
            ano2ano
          )

          assertThrows[ForbiddenFailure](value.r)
        }

        test("pure") {
          val value = pureV.bimap(
            int2str,
            ano2ano
          )

          assert(value.r == "42")
        }

      }

      describe("morph") {

        test("fail") {
          val value = failV.morph(
            int2str,
            ano2str
          )
          assert(value.r == ano.message)
        }

        test("pure") {
          val value = pureV.morph(
            int2str,
            ano2str
          )
          assert(value.r == "42")
        }
      }

      describe("discardContent") {

        test("fail") {
          assertThrows[InvalidInputFailure](failV.discardContent.r)
        }

        test("pure") {
          pureV.discardContent.r
        }
      }

    } //end transformers

  } //end reference syntax tests

} //end test
