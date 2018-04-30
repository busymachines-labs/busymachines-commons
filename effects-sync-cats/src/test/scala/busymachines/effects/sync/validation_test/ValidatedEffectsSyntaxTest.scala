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
package busymachines.effects.sync.validation_test

import busymachines.core._
import busymachines.effects.sync.validated._
import busymachines.effects.sync._
import org.scalatest._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Jan 2018
  *
  */
final class ValidatedEffectsSyntaxTest extends FunSpec {
  //prevents atrocious English
  private def test: ItWord = it

  implicit private class TestSyntax[T](value: Validated[T]) {
    //short for "run"
    def r: T = value.unsafeGet()
    def r(ctor: (Anomaly, List[Anomaly]) => Anomalies): T = value.unsafeGet(ctor)
  }

  //--------------------------------------------------------------------------
  private val thr:   RuntimeException          = new RuntimeException("runtime_exception")
  private val ano:   Anomaly                   = InvalidInputFailure("invalid_input_failure")
  private val anoG:  GenericValidationFailures = GenericValidationFailures(ano)
  private val anoT:  TestValidationFailures    = TestValidationFailures(ano)
  private val anoT2: TestValidationFailures    = TestValidationFailures(ano, List(ano))

  private val none: Option[Int] = Option.empty
  private val some: Option[Int] = Option.pure(42)

  private val success:    Try[Int] = Try(42)
  private val failureThr: Try[Int] = Try.failThr(thr)
  private val failure:    Try[Int] = Try.fail(ano)
  private val failureG:   Try[Int] = Try.fail(anoG)
  private val failureT:   Try[Int] = Try.fail(anoT)
  private val failureT2:  Try[Int] = Try.fail(anoT2)

  private val correct:     Result[Int] = Result(42)
  private val incorrect:   Result[Int] = Result.fail(ano)
  private val incorrectG:  Result[Int] = Result.fail(anoG)
  private val incorrectT:  Result[Int] = Result.fail(anoT)
  private val incorrectT2: Result[Int] = Result.fail(anoT2)

  private val failV: Validated[Int] = Validated.fail(ano)
  private val pureV: Validated[Int] = Validated.pure(42)

  //---------------------------------------------------------------------------

  describe("Validated — companion object syntax") {

    describe("constructors") {
      test("pure") {
        assert(Validated.pure(42).unsafeGet() == 42)
      }

      test("fail") {
        assertThrows[GenericValidationFailures](Validated.fail(ano).r)
      }

      test("fail — nel") {
        val t = intercept[GenericValidationFailures](Validated.fail(cats.data.NonEmptyList.of(ano, ano)).r)
        assert(t.messages.length == 2)
      }

      test("fail — ano") {
        assertThrows[GenericValidationFailures](Validated.fail(ano).r)
      }

      test("unit") {
        assert(Validated.unit == Validated.unit)
      }

      describe("fromOptionAno") {
        test("none") {
          val value: Validated[Int] = Validated.fromOptionAno(none, ano)
          assertThrows[GenericValidationFailures](value.r)
        }

        test("some") {
          val value: Validated[Int] = Validated.fromOptionAno(some, ano)
          assert(value.r == 42)
        }
      }

      describe("fromTryAno") {
        test("failure — thr") {
          val value: Validated[Int] = Validated.fromTryAno(failureThr)
          val t = intercept[GenericValidationFailures](value.r)
          assert(t.bad == CatastrophicError(thr))
        }

        test("failure — anomaly") {
          val value: Validated[Int] = Validated.fromTryAno(failure)
          val t = intercept[GenericValidationFailures](value.r)
          assert(t.bad == ano)
        }

        test("failure — anomalies") {
          val value: Validated[Int] = Validated.fromTryAno(failureT2)
          val t = intercept[GenericValidationFailures](value.r)
          assert(t.messages.length == 2)
        }

        test("success") {
          val value: Validated[Int] = Validated.fromTryAno(success)
          assert(value.r == 42)
        }
      }

      describe("fromResult") {
        test("incorrect — anomaly") {
          val value: Validated[Int] = Validated.fromResult(incorrect)
          val t = intercept[GenericValidationFailures](value.r)
          assert(t.bad == ano)
        }

        test("incorrect — anomalies") {
          val value: Validated[Int] = Validated.fromResult(incorrectT2)
          val t = intercept[GenericValidationFailures](value.r)
          assert(t.messages.length == 2)
        }

        test("success") {
          val value: Validated[Int] = Validated.fromResult(correct)
          assert(value.r == 42)
        }
      }

    } //end constructors

    describe("boolean") {

      describe("condAno") {
        test("false") {
          val value: Validated[Int] = Validated.condAno(
            false,
            42,
            ano
          )
          assertThrows[GenericValidationFailures](value.r)
        }

        test("true") {
          val value: Validated[Int] = Validated.condAno(
            true,
            42,
            ano
          )
          assert(value.r == 42)
        }
      }

      describe("condWith") {
        test("false — pure") {
          val value = Validated.condWith(
            false,
            pureV,
            ano
          )
          assertThrows[GenericValidationFailures](value.r)
        }

        test("true — pure") {
          val value = Validated.condWith(
            true,
            pureV,
            ano
          )
          assert(value.r == 42)
        }

        test("false — fail") {
          val value = Validated.condWith(
            false,
            failV,
            ano
          )
          assertThrows[GenericValidationFailures](value.r)
        }

        test("true — fail") {
          val value = Validated.condWith(
            true,
            failV,
            ano
          )
          assertThrows[GenericValidationFailures](value.r)
        }
      }

      describe("invalidOnTrue") {
        test("false") {
          val value = Validated.invalidOnTrue(
            false,
            ano
          )
          value.r
        }

        test("true") {
          val value = Validated.invalidOnTrue(
            true,
            ano
          )
          assertThrows[GenericValidationFailures](value.r)
        }
      }

      describe("invalidOnFalse") {
        test("false") {
          val value = Validated.invalidOnFalse(
            false,
            ano
          )
          assertThrows[GenericValidationFailures](value.r)
        }

        test("true") {
          val value = Validated.invalidOnFalse(
            true,
            ano
          )
          value.r
        }
      }

    } //end boolean

    describe("as{Effect}") {

      describe("asOptionUnsafe") {

        test("fail") {
          assertThrows[GenericValidationFailures](
            Validated.asOptionUnsafe(failV)
          )
        }

        test("fail — ano") {
          assertThrows[TestValidationFailures](
            Validated.asOptionUnsafe(failV, TestValidationFailures)
          )
        }

        test("pure") {
          assert(Validated.asOptionUnsafe(pureV) == some)
        }

        test("pure — ano") {
          assert(Validated.asOptionUnsafe(pureV, TestValidationFailures) == some)
        }

      }

      describe("asListUnsafe") {

        test("fail") {
          assertThrows[GenericValidationFailures](
            Validated.asListUnsafe(failV)
          )
        }

        test("fail — ano") {
          assertThrows[TestValidationFailures](
            Validated.asListUnsafe(failV, TestValidationFailures)
          )
        }

        test("pure") {
          assert(Validated.asListUnsafe(pureV) == List(42))
        }

        test("pure — ano") {
          assert(Validated.asListUnsafe(pureV, TestValidationFailures) == List(42))
        }

      }

      describe("asTry") {

        test("fail") {
          assert(Validated.asTry(failV) == failureG)
        }

        test("fail — ano") {
          assert(Validated.asTry(failV, TestValidationFailures) == failureT)
        }

        test("pure") {
          assert(Validated.asTry(pureV) == success)
        }

        test("pure — ano") {
          assert(Validated.asTry(pureV, TestValidationFailures) == success)
        }
      }

      describe("asResult") {

        test("fail") {
          assert(Validated.asResult(failV) == incorrectG)
        }

        test("fail — ano") {
          assert(Validated.asResult(failV, TestValidationFailures) == incorrectT)
        }

        test("pure") {
          assert(Validated.asResult(pureV) == correct)
        }

        test("pure — ano") {
          assert(Validated.asResult(pureV, TestValidationFailures) == correct)
        }

      }

      describe("unsafeGet") {

        test("fail") {
          val t = intercept[GenericValidationFailures](Validated.unsafeGet(failV))
          assert(t == anoG)
          assert(t.id.name == GenericValidationFailuresID.name) //hack for coverage
        }

        test("fail — ano") {
          val t = intercept[TestValidationFailures](Validated.unsafeGet(failV, TestValidationFailures))
          assert(t == anoT)
          assert(t.id.name == TVFsID.name) //hack for coverage
        }

        test("pure") {
          assert(Validated.unsafeGet(pureV) == 42)
        }

        test("pure — ano") {
          assert(Validated.unsafeGet(pureV, TestValidationFailures) == 42)
        }

      }

    } //end as{Effect}

    describe("transformers") {

      describe("discardContent") {
        test("fail") {
          assertThrows[GenericValidationFailures](Validated.discardContent(failV).r)
        }

        test("pure") {
          assert(Validated.discardContent(pureV) == Validated.unit)
        }
      }

    } //end transformers

    describe("traversals") {

      describe("Validated.traverse") {

        test("empty list") {
          val input:    Seq[Int] = List()
          val expected: Seq[Int] = List()

          val result = Validated.traverse(input)(Validated.pure)

          assert(result.r == expected)
        }

        test("non empty list — all valid") {
          val input: Seq[Int] = (1 to 100).toList
          val expected = input.map(_.toString)

          val result: Validated[Seq[String]] =
            Validated.traverse(input)(i => Validated.pure(i.toString))
          assert(expected == result.r)
        }

        test("non empty list — ten invalid") {
          val input: Seq[Int] = (1 to 100).toList

          val result: Validated[Seq[String]] = Validated.traverse(input) { i =>
            if (i % 10 == 0)
              Validated.fail(ano)
            else
              Validated.pure(i.toString)
          }
          val t = intercept[TestValidationFailures](result.r(TestValidationFailures))
          assert(t.messages.length == 10)
        }

      }

      describe("Validated.traverse_") {

        test("empty list") {
          val input: Seq[Int] = List()

          val result = Validated.traverse_(input)(Validated.pure)

          assert(result == Validated.unit)
        }

        test("non empty list — all valid") {
          val input: Seq[Int] = (1 to 100).toList

          val result: Validated[Unit] =
            Validated.traverse_(input)(i => Validated.pure(i.toString))
          assert(result == Validated.unit)
        }

        test("non empty list — ten invalid") {
          val input: Seq[Int] = (1 to 100).toList

          val result: Validated[Unit] = Validated.traverse_(input) { i =>
            if (i % 10 == 0)
              Validated.fail(ano)
            else
              Validated.pure(i.toString)
          }
          val t = intercept[TestValidationFailures](result.r(TestValidationFailures))
          assert(t.messages.length == 10)
        }

      }

      describe("Validated.sequence") {

        test("empty list") {
          val input:    Seq[Validated[Int]] = List()
          val expected: Seq[Int]            = List()

          val result = Validated.sequence(input)

          assert(result.r == expected)
        }

        test("non empty list — all valid") {
          val input: Seq[Int] = (1 to 100).toList
          val expected = input.map(_.toString)

          val result: Validated[Seq[String]] =
            Validated.sequence(input.map(i => Validated.pure(i.toString)))
          assert(expected == result.r)
        }

        test("non empty list — all valid — alternate syntax") {
          val expected = List(42, 42)

          val result: Validated[List[Int]] =
            Validated.sequence(pureV, pureV)
          assert(expected == result.r)
        }

        test("non empty list — ten invalid") {
          val input: Seq[Int] = (1 to 100).toList

          val result: Validated[Seq[String]] = Validated.sequence {
            input.map { i =>
              if (i % 10 == 0)
                Validated.fail(ano)
              else
                Validated.pure(i.toString)
            }
          }
          val t = intercept[TestValidationFailures](result.r(TestValidationFailures))
          assert(t.messages.length == 10)
        }

      }

      describe("Validated.sequence_") {

        test("empty list") {
          val input: Seq[Int] = List()

          val result = Validated.sequence_(input.map(Validated.pure))

          assert(result == Validated.unit)
        }

        test("non empty list — all valid") {
          val input: Seq[Int] = (1 to 100).toList

          val result: Validated[Unit] =
            Validated.sequence_(input.map(i => Validated.pure(i.toString)))
          assert(result == Validated.unit)
        }

        test("non empty list — ten invalid") {
          val input: Seq[Int] = (1 to 100).toList

          val result: Validated[Unit] = Validated.sequence_ {
            input.map { i =>
              if (i % 10 == 0)
                Validated.fail(ano)
              else
                Validated.pure(i.toString)
            }
          }
          val t = intercept[TestValidationFailures](result.r(TestValidationFailures))
          assert(t.messages.length == 10)
        }

      }

    } // end traversals

  } //end companion object syntax tests

  //===========================================================================
  //===========================================================================
  //===========================================================================

  describe("Validated — reference syntax") {

    describe("as{Effect}") {

      describe("asOptionUnsafe") {

        test("fail") {
          assertThrows[GenericValidationFailures](
            failV.asOptionUnsafe()
          )
        }

        test("fail — ano") {
          assertThrows[TestValidationFailures](
            failV.asOptionUnsafe(TestValidationFailures)
          )
        }

        test("pure") {
          assert(pureV.asOptionUnsafe() == some)
        }

        test("pure — ano") {
          assert(pureV.asOptionUnsafe(TestValidationFailures) == some)
        }

      }

      describe("asListUnsafe") {

        test("fail") {
          assertThrows[GenericValidationFailures](
            failV.asListUnsafe()
          )
        }

        test("fail — ano") {
          assertThrows[TestValidationFailures](
            failV.asListUnsafe(TestValidationFailures)
          )
        }

        test("pure") {
          assert(pureV.asListUnsafe() == List(42))
        }

        test("pure — ano") {
          assert(pureV.asListUnsafe(TestValidationFailures) == List(42))
        }
      }

      describe("asTry") {

        test("fail") {
          assert(failV.asTry == failureG)
        }

        test("fail — ano") {
          assert(failV.asTry(TestValidationFailures) == failureT)
        }

        test("pure") {
          assert(pureV.asTry == success)
        }

        test("pure — ano") {
          assert(pureV.asTry(TestValidationFailures) == success)
        }

      }

      describe("asResult") {

        test("fail") {
          assert(failV.asResult == incorrectG)
        }

        test("fail — ano") {
          assert(failV.asResult(TestValidationFailures) == incorrectT)
        }

        test("pure") {
          assert(pureV.asResult == correct)
        }

        test("pure — ano") {
          assert(pureV.asResult(TestValidationFailures) == correct)
        }

      }

      describe("unsafeGet") {

        test("fail") {
          assertThrows[GenericValidationFailures](failV.unsafeGet())
        }

        test("fail — ano") {
          assertThrows[TestValidationFailures](failV.unsafeGet(TestValidationFailures))
        }

        test("pure") {
          assert(pureV.unsafeGet() == 42)
        }

        test("pure — ano") {
          assert(pureV.unsafeGet(TestValidationFailures) == 42)
        }

      }

    } //end as{Effect}

    describe("boolean") {

      describe("cond") {
        test("false") {
          val value: Validated[Int] = false.cond(
            42,
            ano
          )
          assertThrows[GenericValidationFailures](value.r)
        }

        test("true") {
          val value = true.cond(
            42,
            ano
          )
          assert(value.r == 42)
        }
      }

      describe("condWith") {
        test("false — pure") {
          val value = false.condWith(
            pureV,
            ano
          )
          assertThrows[GenericValidationFailures](value.r)
        }

        test("true — pure") {
          val value = true.condWith(
            pureV,
            ano
          )
          assert(value.r == 42)
        }

        test("false — fail") {
          val value = false.condWith(
            failV,
            ano
          )
          assertThrows[GenericValidationFailures](value.r)
        }

        test("true — fail") {
          val value = true.condWith(
            failV,
            ano
          )
          assertThrows[GenericValidationFailures](value.r)
        }
      }

      describe("invalidOnTrue") {
        test("false") {
          val value = false.invalidOnTrue(ano)
          value.r
        }

        test("true") {
          val value = true.invalidOnTrue(ano)
          assertThrows[GenericValidationFailures](value.r)
        }
      }

      describe("invalidOnFalse") {
        test("false") {
          val value = false.invalidOnFalse(ano)
          assertThrows[GenericValidationFailures](value.r)
        }

        test("true") {
          val value = true.invalidOnFalse(ano)
          value.r
        }
      }

    } //end boolean

    describe("transformers") {

      describe("discardContent") {
        test("fail") {
          assertThrows[GenericValidationFailures](failV.discardContent.r)
        }

        test("pure") {
          assert(pureV.discardContent == Validated.unit)
        }
      }

    } //end transformers

  } //end reference syntax tests

  //===========================================================================
  //===========================================================================
  //===========================================================================

  describe("Validated — other effects asValidated") {

    describe("reference") {
      describe("Option") {
        test("none") {
          assertThrows[GenericValidationFailures](none.asValidated(ano).r)
        }

        test("some") {
          assert(some.asValidated(ano).r == 42)
        }
      } //end Option

      describe("Try") {
        test("failure") {
          assertThrows[GenericValidationFailures](failure.asValidated.r)
        }

        test("success") {
          assert(success.asValidated.r == 42)
        }
      } //end Option

      describe("Result") {
        test("incorrect") {
          assertThrows[GenericValidationFailures](incorrect.asValidated.r)
        }

        test("correct") {
          assert(correct.asValidated.r == 42)
        }
      } //end Option
    }

    describe("companion") {
      describe("Option") {
        test("none") {
          assertThrows[GenericValidationFailures](Option.asValidated(none, ano).r)
        }

        test("some") {
          assert(Option.asValidated(some, ano).r == 42)
        }
      } //end Option

      describe("Try") {
        test("failure") {
          assertThrows[GenericValidationFailures](Try.asValidated(failure).r)
        }

        test("success") {
          assert(Try.asValidated(success).r == 42)
        }
      } //end Option

      describe("Result") {
        test("incorrect") {
          assertThrows[GenericValidationFailures](Result.asValidated(incorrect).r)
        }

        test("correct") {
          assert(Result.asValidated(correct).r == 42)
        }
      } //end Option
    }

  }

} //end test
