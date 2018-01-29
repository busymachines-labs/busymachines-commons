package busymachines.effects.sync_test

import busymachines.core._
import busymachines.effects.sync._
import org.scalatest._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Jan 2018
  *
  */
final class OptionEffectsTest extends FunSpec {
  //prevents atrocious English
  private def test: ItWord = it

  private implicit class TestSyntax[T](value: Option[T]) {
    //short for "run"
    def r: T = value.unsafeGet()
  }

  //--------------------------------------------------------------------------

  private val thr: RuntimeException = new RuntimeException("runtime_exception")

  private val ano: InvalidInputAnomaly = InvalidInputFailure("invalid_input_failure")

  private val left:  Either[Throwable, Int] = Left(thr)
  private val right: Either[Throwable, Int] = Right(42)

  private val failure: Try[Int] = Try.fail(ano)
  private val success: Try[Int] = Try.pure(42)

  private val correct:   Result[Int] = Result(42)
  private val incorrect: Result[Int] = Result.fail(ano)

  private val int2str: Int => String = i => i.toString

  private val failV: Option[Int] = Option.empty
  private val pureV: Option[Int] = Option(42)

  private val btrue:  Option[Boolean] = Option.pure(true)
  private val bfalse: Option[Boolean] = Option.pure(false)
  private val bfail:  Option[Boolean] = Option.none

  //---------------------------------------------------------------------------

  describe("Try — companion object syntax") {

    describe("constructors") {
      test("pure") {
        assert(Option.pure(42).unsafeGet() == 42)
        assert(Option.some(42).unsafeGet() == 42)
        assert(Try(42).unsafeGet() == 42)
      }

      test("fail") {
        assertThrows[NoSuchElementException](Option.fail.r)
        assertThrows[NoSuchElementException](Option.none.r)
      }

      test("unit") {
        assert(Option.unit == Option.unit)
      }

      describe("fromTry") {
        test("failure") {
          assert(Option.fromTryUnsafe(failure) == None) //error is surpressed

        }

        test("success") {
          assert(Option.fromTryUnsafe(success).r == 42)
        }
      }

      describe("fromEither") {
        test("left") {
          assert(Option.fromEitherUnsafe(left) == None) //error is surpressed

        }

        test("right") {
          assert(Option.fromEitherUnsafe(right).r == 42)
        }
      }

      describe("fromResult") {
        test("incorrect") {
          assert(Option.fromResultUnsafe(incorrect) == None) //error is surpressed
        }
      }

    } //end constructors

    describe("boolean") {

      describe("cond") {
        test("false") {
          val value = Option.cond(
            false,
            42
          )
          assert(value == None)
        }

        test("true") {
          val value = Option.cond(
            true,
            42
          )
          assert(value.r == 42)
        }
      }

      describe("condWith") {
        test("false — pure") {
          val value = Option.condWith(
            false,
            pureV
          )
          assert(value == None)
        }

        test("true — pure") {
          val value = Option.condWith(
            true,
            pureV
          )
          assert(value.r == 42)
        }

        test("false — fail") {
          val value = Option.condWith(
            false,
            failV
          )
          assert(value == None)
        }

        test("true — fail") {
          val value = Option.condWith(
            true,
            failV
          )
          assert(value == None)
        }
      }

      describe("flatCond") {
        test("false") {
          val value = Option.flatCond(
            bfalse,
            42
          )
          assert(value == None)
        }

        test("true") {
          val value = Option.flatCond(
            btrue,
            42
          )
          assert(value.r == 42)
        }

        test("fail") {
          val value = Option.flatCond(
            bfail,
            42
          )
          assert(value == None)
        }
      }

      describe("flatCondWith") {
        test("false — pure") {
          val value = Option.flatCondWith(
            bfalse,
            pureV
          )
          assert(value == None)
        }

        test("false — fail") {
          val value = Option.flatCondWith(
            bfalse,
            failV
          )
          assert(value == None)
        }

        test("true — pure") {
          val value = Option.flatCondWith(
            btrue,
            pureV
          )
          assert(value.r == 42)
        }

        test("true — fail") {
          val value = Option.flatCondWith(
            btrue,
            failV
          )
          assert(value == None)
        }

        test("fail — pure") {
          val value = Option.flatCondWith(
            bfail,
            pureV
          )
          assert(value == None)
        }

        test("fail — fail") {
          val value = Option.flatCondWith(
            bfail,
            failV
          )
          assert(value == None)
        }
      }

    } //end boolean

    describe("as{Effect}") {

      describe("asTry") {

        test("fail") {
          assertThrows[InvalidInputFailure](
            Option.asTry(failV, ano).get
          )
        }

        test("pure") {
          assert(Option.asTry(pureV, ano) == Try(42))
        }

      }

      describe("asTryWeak") {

        test("fail") {
          assertThrows[RuntimeException](
            Option.asTryWeak(failV, thr).get
          )
        }

        test("pure") {
          assert(Option.asTryWeak(pureV, thr) == Try(42))
        }

      }

      describe("asList") {

        test("fail") {
          assert(Option.asList(failV) == List())
        }

        test("pure") {
          assert(Option.asList(pureV) == List(42))
        }

      }

      describe("asEither") {

        test("fail") {
          assert(Option.asEither(failV, ano.asThrowable) == Left(ano))
        }

        test("pure") {
          assert(Option.asEither(pureV, ano.asThrowable) == right)
        }

      }

      describe("asResult") {

        test("fail") {
          assert(Option.asResult(failV, ano) == incorrect)
        }

        test("pure") {
          assert(Option.asResult(pureV, ano) == correct)
        }

      }

      describe("unsafeGet") {

        test("fail") {
          assertThrows[NoSuchElementException](Option.unsafeGet(failV))
        }

        test("pure") {
          assert(Option.unsafeGet(pureV) == 42)
        }

      }

    } //end as{Effect}

    describe("transformers") {

      describe("morph") {

        test("fail") {
          val value = Option.morph(
            failV,
            int2str,
            ano.message
          )
          assert(value.r == ano.message)
        }

        test("pure") {
          val value = Option.morph(
            pureV,
            int2str,
            ano.message
          )
          assert(value.r == "42")
        }
      }

    } //end transformers

  } //end companion object syntax tests

  //===========================================================================
  //===========================================================================
  //===========================================================================

  describe("Try — reference syntax") {

    describe("boolean") {

      describe("cond") {
        test("false") {
          val value = false.condOption(42)
          assert(value == None)
        }

        test("true") {
          val value = true.condOption(42)
          assert(value.r == 42)
        }
      }

      describe("condWith") {
        test("false — pure") {
          val value = false.condWithOption(pureV)
          assert(value == None)
        }

        test("true — pure") {
          val value = true.condWithOption(pureV)
          assert(value.r == 42)
        }

        test("false — fail") {
          val value = false.condWithOption(failV)
          assert(value == None)
        }

        test("true — fail") {
          val value = true.condWithOption(failV)
          assert(value == None)
        }
      }

      describe("flatCond") {
        test("false") {
          val value = bfalse.cond(42)
          assert(value == None)
        }

        test("true") {
          val value = btrue.cond(42)
          assert(value.r == 42)
        }

        test("fail") {
          val value = bfail.cond(42)
          assert(value == None)
        }
      }

      describe("flatCondWith") {
        test("false — pure") {
          val value = bfalse.condWith(pureV)
          assert(value == None)
        }

        test("false — fail") {
          val value = bfalse.condWith(failV)
          assert(value == None)
        }

        test("true — pure") {
          val value = btrue.condWith(pureV)
          assert(value.r == 42)
        }

        test("true — fail") {
          val value = btrue.condWith(failV)
          assert(value == None)
        }

        test("fail — pure") {
          val value = bfail.condWith(pureV)
          assert(value == None)
        }

        test("fail — fail") {
          val value = bfail.condWith(failV)
          assert(value == None)
        }
      }

    } //end boolean

    describe("as{Effect}") {

      describe("asTry") {

        test("fail") {
          assertThrows[InvalidInputFailure](
            failV.asTry(ano).get
          )
        }

        test("pure") {
          assert(pureV.asTry(ano) == Try(42))
        }

      }

      describe("asTryWeak") {

        test("fail") {
          assertThrows[RuntimeException](
            failV.asTryWeak(thr).get
          )
        }

        test("pure") {
          assert(pureV.asTryWeak(thr) == Try(42))
        }

      }

      describe("asList") {

        test("fail") {
          assert(failV.asList == List())
        }

        test("pure") {
          assert(pureV.asList == List(42))
        }

      }

      describe("asEither") {

        test("fail") {
          assert(failV.asEither(ano.asThrowable) == Left(ano))
        }

        test("pure") {
          assert(pureV.asEither(ano.asThrowable) == right)
        }

      }

      describe("asResult") {

        test("fail") {
          assert(failV.asResult(ano) == incorrect)
        }

        test("pure") {
          assert(pureV.asResult(ano) == correct)
        }

      }

      describe("unsafeGet") {

        test("fail") {
          assertThrows[NoSuchElementException](failV.unsafeGet())
        }

        test("pure") {
          assert(pureV.unsafeGet() == 42)
        }

      }

    } //end as{Effect}

    describe("transformers") {

      describe("morph") {

        test("fail") {
          val value = failV.morph(
            int2str,
            ano.message
          )
          assert(value.r == ano.message)
        }

        test("pure") {
          val value = pureV.morph(
            int2str,
            ano.message
          )
          assert(value.r == "42")
        }
      }

    } //end transformers

  } //end reference syntax tests

} //end test
