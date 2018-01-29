package busymachines.effects_test

import busymachines.core._
import busymachines.effects.sync._
import org.scalatest._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Jan 2018
  *
  */
final class TryEffectsTest extends FunSpec with Matchers {
  //prevents atrocious English
  private def test: ItWord = it

  private implicit class TestSyntax[T](value: Try[T]) {
    //short for "run"
    def r: T = value.unsafeGet()
  }

  //--------------------------------------------------------------------------

  private val thr: RuntimeException         = new RuntimeException("runtime_exception")
  private val iae: IllegalArgumentException = new IllegalArgumentException("illegal_argument_exception")

  private val ano: InvalidInputAnomaly = InvalidInputFailure("invalid_input_failure")

  private val none: Option[Int] = Option.empty
  private val some: Option[Int] = Option(42)

  private val failV: Try[Int] = Try.fail(ano)
  private val pureV: Try[Int] = Try.pure(42)

  private val left:  Either[Throwable, Int] = Left(thr)
  private val right: Either[Throwable, Int] = Right(42)

  private val correct:   Result[Int] = Result(42)
  private val incorrect: Result[Int] = Result.fail(ano)

  private val btrue:  Try[Boolean] = Try.pure(true)
  private val bfalse: Try[Boolean] = Try.pure(false)
  private val bfail:  Try[Boolean] = Try.failWeak(iae)

  private val int2str: Int => String = i => i.toString

  private val thr2str: Throwable => String    = thr => thr.getMessage
  private val thr2ano: Throwable => Anomaly   = thr => ForbiddenFailure
  private val thr2thr: Throwable => Throwable = thr => iae

  //---------------------------------------------------------------------------

  describe("Try — companion object syntax") {

    describe("constructors") {
      test("pure") {
        assert(Try.pure(42).unsafeGet() == 42)
        assert(Try.success(42).unsafeGet() == 42)
        assert(Try(42).unsafeGet() == 42)
      }

      test("fail") {
        the[InvalidInputFailure] thrownBy (Try.fail(ano).r)
        the[RuntimeException] thrownBy (Try.failWeak(thr).r)

        the[InvalidInputFailure] thrownBy (Try.failure(ano).r)
        the[RuntimeException] thrownBy (Try.failureWeak(thr).r)
      }

      test("unit") {
        assert(Try.unit == Try.unit)
      }

      describe("fromOption") {
        test("none") {
          the[InvalidInputFailure] thrownBy (Try.fromOption(none, ano).r)
        }

        test("some") {
          assert(Try.fromOption(some, ano).r == 42)
        }
      }

      describe("fromOptionWeak") {
        test("none") {
          the[RuntimeException] thrownBy (Try.fromOptionWeak(none, thr).r)
        }

        test("some") {
          assert(Try.fromOptionWeak(some, thr).r == 42)
        }
      }

      describe("fromEither") {
        test("left") {
          the[RuntimeException] thrownBy (Try.fromEither(left).r)
        }

        test("left — transform") {
          the[ForbiddenFailure] thrownBy (Try.fromEither(left, thr2ano).r)
        }

        test("right") {
          assert(Try.fromEither(right).r == 42)
        }

        test("right — transform") {
          assert(Try.fromEither(right, thr2ano).r == 42)
        }
      }

      describe("fromEitherWeak") {
        test("left — transform") {
          the[IllegalArgumentException] thrownBy (Try.fromEitherWeak(left, (t: Throwable) => iae).r)
        }

        test("right") {
          assert(Try.fromEitherWeak(right, (t: Throwable) => iae).r == 42)
        }
      }

      describe("fromResult") {
        test("incorrect") {
          the[InvalidInputFailure] thrownBy (Try.fromResult(incorrect).r)
        }

        test("correct") {
          assert(Try.fromResult(correct).r == 42)
        }
      }

    } //end constructors

    describe("boolean") {

      describe("cond") {
        test("false") {
          val value = Try.cond(
            false,
            42,
            ano
          )
          the[InvalidInputFailure] thrownBy (value.r)
        }

        test("true") {
          val value = Try.cond(
            true,
            42,
            ano
          )
          assert(value.r == 42)
        }
      }

      describe("condWeak") {
        test("false") {
          val value = Try.condWeak(
            false,
            42,
            thr
          )
          the[RuntimeException] thrownBy (value.r)
        }

        test("true") {
          val value = Try.condWeak(
            true,
            42,
            thr
          )
          assert(value.r == 42)
        }
      }

      describe("condWith") {
        test("false — pure") {
          val value = Try.condWith(
            false,
            pureV,
            ano
          )
          the[InvalidInputFailure] thrownBy (value.r)
        }

        test("true — pure") {
          val value = Try.condWith(
            true,
            pureV,
            ano
          )
          assert(value.r == 42)
        }

        test("false — fail") {
          val value = Try.condWith(
            false,
            failV,
            ano
          )
          the[InvalidInputFailure] thrownBy (value.r)
        }

        test("true — fail") {
          val value = Try.condWith(
            true,
            failV,
            ano
          )
          the[InvalidInputFailure] thrownBy (value.r)
        }
      }

      describe("condWithWeak") {
        test("false — pure") {
          val value = Try.condWithWeak(
            false,
            pureV,
            thr
          )
          the[RuntimeException] thrownBy (value.r)
        }

        test("true — pure") {
          val value = Try.condWithWeak(
            true,
            pureV,
            thr
          )
          assert(value.r == 42)
        }

        test("false — fail") {
          val value = Try.condWithWeak(
            false,
            failV,
            thr
          )
          the[RuntimeException] thrownBy (value.r)
        }

        test("true — fail") {
          val value = Try.condWithWeak(
            true,
            failV,
            thr
          )
          the[InvalidInputFailure] thrownBy (value.r)
        }
      }

      describe("flatCond") {
        test("false") {
          val value = Try.flatCond(
            bfalse,
            42,
            ano
          )
          the[InvalidInputFailure] thrownBy (value.r)
        }

        test("true") {
          val value = Try.flatCond(
            btrue,
            42,
            ano
          )
          assert(value.r == 42)
        }

        test("fail") {
          val value = Try.flatCond(
            bfail,
            42,
            ano
          )
          the[IllegalArgumentException] thrownBy (value.r)
        }
      }

      describe("flatCondWeak") {
        test("false") {
          val value = Try.flatCondWeak(
            bfalse,
            42,
            thr
          )
          the[RuntimeException] thrownBy (value.r)
        }

        test("true") {
          val value = Try.flatCondWeak(
            btrue,
            42,
            thr
          )
          assert(value.r == 42)
        }

        test("fail") {
          val value = Try.flatCondWeak(
            bfail,
            42,
            thr
          )
          the[IllegalArgumentException] thrownBy (value.r)
        }
      }

      describe("flatCondWith") {
        test("false — pure") {
          val value = Try.flatCondWith(
            bfalse,
            pureV,
            ano
          )
          the[InvalidInputFailure] thrownBy (value.r)
        }

        test("false — fail") {
          val value = Try.flatCondWith(
            bfalse,
            failV,
            ano
          )
          the[InvalidInputFailure] thrownBy (value.r)
        }

        test("true — pure") {
          val value = Try.flatCondWith(
            btrue,
            pureV,
            ano
          )
          assert(value.r == 42)
        }

        test("true — fail") {
          val value = Try.flatCondWith(
            btrue,
            failV,
            ano
          )
          the[InvalidInputFailure] thrownBy (value.r)
        }

        test("fail — pure") {
          val value = Try.flatCondWith(
            bfail,
            pureV,
            ano
          )
          the[IllegalArgumentException] thrownBy (value.r)
        }

        test("fail — fail") {
          val value = Try.flatCondWith(
            bfail,
            failV,
            ano
          )
          the[IllegalArgumentException] thrownBy (value.r)
        }
      }

      describe("flatCondWithWeak") {
        test("false — pure") {
          val value = Try.flatCondWithWeak(
            bfalse,
            pureV,
            thr
          )
          the[RuntimeException] thrownBy (value.r)
        }

        test("false — fail") {
          val value = Try.flatCondWithWeak(
            bfalse,
            failV,
            thr
          )
          the[RuntimeException] thrownBy (value.r)
        }

        test("true — pure") {
          val value = Try.flatCondWithWeak(
            btrue,
            pureV,
            thr
          )
          assert(value.r == 42)
        }

        test("true — fail") {
          val value = Try.flatCondWithWeak(
            btrue,
            failV,
            thr
          )
          the[InvalidInputFailure] thrownBy (value.r)
        }

        test("fail — pure") {
          val value = Try.flatCondWithWeak(
            bfail,
            pureV,
            thr
          )
          the[IllegalArgumentException] thrownBy (value.r)
        }

        test("fail — fail") {
          val value = Try.flatCondWithWeak(
            bfail,
            failV,
            thr
          )
          the[IllegalArgumentException] thrownBy (value.r)
        }
      }

      describe("failOnTrue") {
        test("false") {
          val value = Try.failOnTrue(
            false,
            ano
          )
          value.r
        }

        test("true") {
          val value = Try.failOnTrue(
            true,
            ano
          )
          the[InvalidInputFailure] thrownBy (value.r)
        }
      }

      describe("failOnTrueWeak") {
        test("false") {
          val value = Try.failOnTrueWeak(
            false,
            thr
          )
          value.r
        }

        test("true") {
          val value = Try.failOnTrueWeak(
            true,
            thr
          )
          the[RuntimeException] thrownBy (value.r)
        }
      }

      describe("failOnFalse") {
        test("false") {
          val value = Try.failOnFalse(
            false,
            ano
          )
          the[InvalidInputFailure] thrownBy (value.r)
        }

        test("true") {
          val value = Try.failOnFalse(
            true,
            ano
          )
          value.r
        }
      }

      describe("failOnFalseWeak") {
        test("false") {
          val value = Try.failOnFalseWeak(
            false,
            thr
          )
          the[RuntimeException] thrownBy (value.r)
        }

        test("true") {
          val value = Try.failOnFalseWeak(
            true,
            thr
          )
          value.r
        }
      }

      describe("flatFailOnTrue") {
        test("false") {
          val value = Try.flatFailOnTrue(
            bfalse,
            ano
          )
          value.r
        }

        test("true") {
          val value = Try.flatFailOnTrue(
            btrue,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("fail") {
          val value = Try.flatFailOnTrue(
            bfail,
            ano
          )
          assertThrows[IllegalArgumentException](value.r)
        }

      }

      describe("flatFailOnTrueWeak") {
        test("false") {
          val value = Try.flatFailOnTrueWeak(
            bfalse,
            thr
          )
          value.r
        }

        test("true") {
          val value = Try.flatFailOnTrueWeak(
            btrue,
            thr
          )
          assertThrows[RuntimeException](value.r)
        }

        test("fail") {
          val value = Try.flatFailOnTrueWeak(
            bfail,
            thr
          )
          assertThrows[IllegalArgumentException](value.r)
        }

      }

      describe("flatFailOnFalse") {
        test("false") {
          val value = Try.flatFailOnFalse(
            bfalse,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("true") {
          val value = Try.flatFailOnFalse(
            btrue,
            ano
          )
          value.r
        }

        test("fail") {
          val value = Try.flatFailOnFalse(
            bfail,
            ano
          )
          assertThrows[IllegalArgumentException](value.r)
        }

      }

      describe("flatFailOnFalseWeak") {
        test("false") {
          val value = Try.flatFailOnFalseWeak(
            bfalse,
            thr
          )
          assertThrows[RuntimeException](value.r)
        }

        test("true") {
          val value = Try.flatFailOnFalseWeak(
            btrue,
            thr
          )
          value.r
        }

        test("fail") {
          val value = Try.flatFailOnFalseWeak(
            bfail,
            thr
          )
          assertThrows[IllegalArgumentException](value.r)
        }

      }

    } //end boolean

    describe("nested") {

      describe("flattenOption") {
        test("pure — none") {
          assertThrows[InvalidInputAnomaly] {
            Try.flattenOption(Try.pure(none), ano).r
          }
        }

        test("pure — some") {
          assert(Try.flattenOption(Try.pure(some), ano).r == 42)
        }

        test("fail") {
          assertThrows[RuntimeException] {
            Try.flattenOption(Try.failureWeak[Option[Int]](thr), ano).r
          }
        }
      }

      describe("flattenOptionWeak") {

        test("pure — none") {
          assertThrows[RuntimeException] {
            Try.flattenOptionWeak(Try.pure(none), thr).r
          }
        }

        test("pure — some") {
          assert(Try.flattenOptionWeak(Try.pure(some), thr).r == 42)
        }

        test("fail") {
          assertThrows[InvalidInputFailure] {
            Try.flattenOptionWeak(Try.failure[Option[Int]](ano), thr).r
          }
        }
      }

      describe("flattenResult") {
        test("incorrect") {
          assertThrows[InvalidInputFailure](
            Try.flattenResult(Try.pure(incorrect)).r
          )
        }

        test("correct") {
          assert(Try.flattenResult(Try.pure(correct)).r == 42)
        }
      }

    } //end nested

    describe("as{Effect}") {

      describe("asOptionUnsafe") {

        test("fail") {
          assertThrows[InvalidInputFailure](
            Try.asOptionUnsafe(failV)
          )
        }

        test("pure") {
          assert(Try.asOptionUnsafe(pureV) == some)
        }

      }

      describe("asListUnsafe") {

        test("fail") {
          assertThrows[InvalidInputFailure](
            Try.asListUnsafe(failV)
          )
        }

        test("pure") {
          assert(Try.asListUnsafe(pureV) == List(42))
        }

      }

      describe("asEither") {

        test("fail") {
          assert(Try.asEither(failV) == Left(ano))
        }

        test("pure") {
          assert(Try.asEither(pureV) == right)
        }

      }

      describe("asResult") {

        test("fail") {
          assert(Try.asResult(failV) == incorrect)
        }

        test("pure") {
          assert(Try.asResult(pureV) == correct)
        }

      }

      describe("unsafeGet") {

        test("fail") {
          assertThrows[InvalidInputFailure](Try.unsafeGet(failV))
        }

        test("pure") {
          assert(Try.unsafeGet(pureV) == 42)
        }

      }

    } //end as{Effect}

    describe("transformers") {

      describe("bimap") {

        test("fail") {
          val value = Try.bimap(
            failV,
            int2str,
            thr2ano
          )

          assertThrows[ForbiddenFailure](value.r)
        }

        test("pure") {
          val value = Try.bimap(
            pureV,
            int2str,
            thr2ano
          )

          assert(value.r == "42")
        }

      }

      describe("bimapWeak") {

        test("fail") {
          val value = Try.bimapWeak(
            failV,
            int2str,
            thr2thr
          )

          assertThrows[IllegalArgumentException](value.r)
        }

        test("pure") {
          val value = Try.bimapWeak(
            pureV,
            int2str,
            thr2thr
          )

          assert(value.r == "42")
        }

      }

      describe("morph") {

        test("fail") {
          val value = Try.morph(
            failV,
            int2str,
            thr2str
          )
          assert(value.r == ano.message)
        }

        test("pure") {
          val value = Try.morph(
            pureV,
            int2str,
            thr2str
          )
          assert(value.r == "42")
        }
      }

      describe("discardContent") {

        test("fail") {
          assertThrows[InvalidInputFailure](Try.discardContent(failV).r)
        }

        test("pure") {
          Try.discardContent(pureV).r
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
          val value = false.condTry(
            42,
            ano
          )
          the[InvalidInputFailure] thrownBy (value.r)
        }

        test("true") {
          val value = true.condTry(
            42,
            ano
          )
          assert(value.r == 42)
        }
      }

      describe("condWeak") {
        test("false") {
          val value =
            false.condTryWeak(
              42,
              thr
            )
          the[RuntimeException] thrownBy (value.r)
        }

        test("true") {
          val value = true.condTryWeak(
            42,
            thr
          )
          assert(value.r == 42)
        }
      }

      describe("condWith") {
        test("false — pure") {
          val value = false.condWithTry(
            pureV,
            ano
          )
          the[InvalidInputFailure] thrownBy (value.r)
        }

        test("true — pure") {
          val value = true.condWithTry(
            pureV,
            ano
          )
          assert(value.r == 42)
        }

        test("false — fail") {
          val value = false.condWithTry(
            failV,
            ano
          )
          the[InvalidInputFailure] thrownBy (value.r)
        }

        test("true — fail") {
          val value = true.condWithTry(
            failV,
            ano
          )
          the[InvalidInputFailure] thrownBy (value.r)
        }
      }

      describe("condWithWeak") {
        test("false — pure") {
          val value = false.condWithTryWeak(
            pureV,
            thr
          )
          the[RuntimeException] thrownBy (value.r)
        }

        test("true — pure") {
          val value = true.condWithTryWeak(
            pureV,
            thr
          )
          assert(value.r == 42)
        }

        test("false — fail") {
          val value =
            false.condWithTryWeak(
              failV,
              thr
            )
          the[RuntimeException] thrownBy (value.r)
        }

        test("true — fail") {
          val value = true.condWithTryWeak(
            failV,
            thr
          )
          the[InvalidInputFailure] thrownBy (value.r)
        }
      }

      describe("flatCond") {
        test("false") {
          val value = bfalse.cond(
            42,
            ano
          )
          the[InvalidInputFailure] thrownBy (value.r)
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
          the[IllegalArgumentException] thrownBy (value.r)
        }
      }

      describe("flatCondWeak") {
        test("false") {
          val value = bfalse.condWeak(
            42,
            thr
          )
          the[RuntimeException] thrownBy (value.r)
        }

        test("true") {
          val value = btrue.condWeak(
            42,
            thr
          )
          assert(value.r == 42)
        }

        test("fail") {
          val value = bfail.condWeak(
            42,
            thr
          )
          the[IllegalArgumentException] thrownBy (value.r)
        }
      }

      describe("flatCondWith") {
        test("false — pure") {
          val value = bfalse.condWith(
            pureV,
            ano
          )
          the[InvalidInputFailure] thrownBy (value.r)
        }

        test("false — fail") {
          val value = bfalse.condWith(
            failV,
            ano
          )
          the[InvalidInputFailure] thrownBy (value.r)
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
          the[InvalidInputFailure] thrownBy (value.r)
        }

        test("fail — pure") {
          val value = bfail.condWith(
            pureV,
            ano
          )
          the[IllegalArgumentException] thrownBy (value.r)
        }

        test("fail — fail") {
          val value = bfail.condWith(
            failV,
            ano
          )
          the[IllegalArgumentException] thrownBy (value.r)
        }
      }

      describe("flatCondWithWeak") {
        test("false — pure") {
          val value = bfalse.condWithWeak(
            pureV,
            thr
          )
          the[RuntimeException] thrownBy (value.r)
        }

        test("false — fail") {
          val value = bfalse.condWithWeak(
            failV,
            thr
          )
          the[RuntimeException] thrownBy (value.r)
        }

        test("true — pure") {
          val value = btrue.condWithWeak(
            pureV,
            thr
          )
          assert(value.r == 42)
        }

        test("true — fail") {
          val value = btrue.condWithWeak(
            failV,
            thr
          )
          the[InvalidInputFailure] thrownBy (value.r)
        }

        test("fail — pure") {
          val value = bfail.condWithWeak(
            pureV,
            thr
          )
          the[IllegalArgumentException] thrownBy (value.r)
        }

        test("fail — fail") {
          val value = bfail.condWithWeak(
            failV,
            thr
          )
          the[IllegalArgumentException] thrownBy (value.r)
        }
      }

      describe("failOnTrue") {
        test("false") {
          val value = false.failOnTrueTry(ano)
          value.r
        }

        test("true") {
          val value = true.failOnTrueTry(ano)
          the[InvalidInputFailure] thrownBy (value.r)
        }
      }

      describe("failOnTrueWeak") {
        test("false") {
          val value = false.failOnTrueTryWeak(thr)
          value.r
        }

        test("true") {
          val value = true.failOnTrueTryWeak(thr)
          the[RuntimeException] thrownBy (value.r)
        }
      }

      describe("failOnFalse") {
        test("false") {
          val value = false.failOnFalseTry(ano)
          the[InvalidInputFailure] thrownBy (value.r)
        }

        test("true") {
          val value = true.failOnFalseTry(ano)
          value.r
        }
      }

      describe("failOnFalseWeak") {
        test("false") {
          val value = false.failOnFalseTryWeak(thr)
          the[RuntimeException] thrownBy (value.r)
        }

        test("true") {
          val value = true.failOnFalseTryWeak(thr)
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
          assertThrows[IllegalArgumentException](value.r)
        }

      }

      describe("flatFailOnTrueWeak") {
        test("false") {
          val value = bfalse.failOnTrueWeak(thr)
          value.r
        }

        test("true") {
          val value = btrue.failOnTrueWeak(thr)
          assertThrows[RuntimeException](value.r)
        }

        test("fail") {
          val value = bfail.failOnTrueWeak(thr)
          assertThrows[IllegalArgumentException](value.r)
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
          assertThrows[IllegalArgumentException](value.r)
        }

      }

      describe("flatFailOnFalseWeak") {
        test("false") {
          val value = bfalse.failOnFalseWeak(thr)
          assertThrows[RuntimeException](value.r)
        }

        test("true") {
          val value = btrue.failOnFalseWeak(thr)
          value.r
        }

        test("fail") {
          val value = bfail.failOnFalseWeak(thr)
          assertThrows[IllegalArgumentException](value.r)
        }

      }

    } //end boolean

    describe("nested") {

      describe("flattenOption") {
        test("pure — none") {
          assertThrows[InvalidInputAnomaly] {
            Try.pure(none).flattenOption(ano).r
          }
        }

        test("pure — some") {
          assert(Try.pure(some).flattenOption(ano).r == 42)
        }

        test("fail") {
          assertThrows[RuntimeException] {
            Try.failureWeak[Option[Int]](thr).flattenOption(ano).r
          }
        }
      }

      describe("flattenOptionWeak") {

        test("pure — none") {
          assertThrows[RuntimeException] {
            Try.pure(none).flattenOptionWeak(thr).r
          }
        }

        test("pure — some") {
          assert(Try.pure(some).flattenOptionWeak(thr).r == 42)
        }

        test("fail") {
          assertThrows[InvalidInputFailure] {
            Try.failure[Option[Int]](ano).flattenOptionWeak(thr).r
          }
        }
      }

      describe("flattenResult") {
        test("incorrect") {
          assertThrows[InvalidInputFailure](
            Try.pure(incorrect).flattenResult.r
          )
        }

        test("correct") {
          assert(Try.pure(correct).flattenResult.r == 42)
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

      describe("asEither") {

        test("fail") {
          assert(failV.asEither == Left(ano))
        }

        test("pure") {
          assert(pureV.asEither == right)
        }

      }

      describe("asResult") {

        test("fail") {
          assert(failV.asResult == incorrect)
        }

        test("pure") {
          assert(pureV.asResult == correct)
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
            thr2ano
          )

          assertThrows[ForbiddenFailure](value.r)
        }

        test("pure") {
          val value = pureV.bimap(
            int2str,
            thr2ano
          )

          assert(value.r == "42")
        }

      }

      describe("bimapWeak") {

        test("fail") {
          val value = failV.bimapWeak(
            int2str,
            thr2thr
          )

          assertThrows[IllegalArgumentException](value.r)
        }

        test("pure") {
          val value = pureV.bimapWeak(
            int2str,
            thr2thr
          )

          assert(value.r == "42")
        }

      }

      describe("morph") {

        test("fail") {
          val value = failV.morph(
            int2str,
            thr2str
          )
          assert(value.r == ano.message)
        }

        test("pure") {
          val value = pureV.morph(
            int2str,
            thr2str
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
