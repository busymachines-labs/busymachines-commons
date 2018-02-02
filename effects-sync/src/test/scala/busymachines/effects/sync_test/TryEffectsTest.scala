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
final class TryEffectsTest extends FunSpec {
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

  private val left:  Either[Throwable, Int] = Left(thr)
  private val right: Either[Throwable, Int] = Right(42)

  private val correct:   Result[Int] = Result(42)
  private val incorrect: Result[Int] = Result.fail(ano)

  private val int2str: Int => String = i => i.toString

  private val thr2str: Throwable => String    = thr => thr.getMessage
  private val thr2ano: Throwable => Anomaly   = thr => ForbiddenFailure
  private val thr2thr: Throwable => Throwable = thr => iae

  private val failV: Try[Int] = Try.fail(ano)
  private val pureV: Try[Int] = Try.pure(42)

  private val btrue:  Try[Boolean] = Try.pure(true)
  private val bfalse: Try[Boolean] = Try.pure(false)
  private val bfail:  Try[Boolean] = Try.failThr(iae)

  //---------------------------------------------------------------------------

  describe("Try — companion object syntax") {

    describe("constructors") {
      test("pure") {
        assert(Try.pure(42).unsafeGet() == 42)
        assert(Try.success(42).unsafeGet() == 42)
        assert(Try(42).unsafeGet() == 42)
      }

      test("fail") {
        assertThrows[InvalidInputFailure](Try.fail(ano).r)
        assertThrows[RuntimeException](Try.failThr(thr).r)

        assertThrows[InvalidInputFailure](Try.failure(ano).r)
        assertThrows[RuntimeException](Try.failureThr(thr).r)
      }

      test("unit") {
        assert(Try.unit == Try.unit)
      }

      describe("fromOption") {
        test("none") {
          assertThrows[InvalidInputFailure](Try.fromOption(none, ano).r)
        }

        test("some") {
          assert(Try.fromOption(some, ano).r == 42)
        }
      }

      describe("fromOptionThr") {
        test("none") {
          assertThrows[RuntimeException](Try.fromOptionThr(none, thr).r)
        }

        test("some") {
          assert(Try.fromOptionThr(some, thr).r == 42)
        }
      }

      describe("fromEither") {
        test("left") {
          assertThrows[RuntimeException](Try.fromEitherThr(left).r)
        }

        test("left — transform") {
          assertThrows[ForbiddenFailure](Try.fromEither(left, thr2ano).r)
        }

        test("right") {
          assert(Try.fromEitherThr(right).r == 42)
        }

        test("right — transform") {
          assert(Try.fromEither(right, thr2ano).r == 42)
        }
      }

      describe("fromEitherThr") {
        test("left — transform") {
          assertThrows[IllegalArgumentException](Try.fromEitherThr(left, (t: Throwable) => iae).r)
        }

        test("right") {
          assert(Try.fromEitherThr(right, (t: Throwable) => iae).r == 42)
        }
      }

      describe("fromResult") {
        test("incorrect") {
          assertThrows[InvalidInputFailure](Try.fromResult(incorrect).r)
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
          assertThrows[InvalidInputFailure](value.r)
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

      describe("condThr") {
        test("false") {
          val value = Try.condThr(
            false,
            42,
            thr
          )
          assertThrows[RuntimeException](value.r)
        }

        test("true") {
          val value = Try.condThr(
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
          assertThrows[InvalidInputFailure](value.r)
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
          assertThrows[InvalidInputFailure](value.r)
        }

        test("true — fail") {
          val value = Try.condWith(
            true,
            failV,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }
      }

      describe("condWithThr") {
        test("false — pure") {
          val value = Try.condWithThr(
            false,
            pureV,
            thr
          )
          assertThrows[RuntimeException](value.r)
        }

        test("true — pure") {
          val value = Try.condWithThr(
            true,
            pureV,
            thr
          )
          assert(value.r == 42)
        }

        test("false — fail") {
          val value = Try.condWithThr(
            false,
            failV,
            thr
          )
          assertThrows[RuntimeException](value.r)
        }

        test("true — fail") {
          val value = Try.condWithThr(
            true,
            failV,
            thr
          )
          assertThrows[InvalidInputFailure](value.r)
        }
      }

      describe("flatCond") {
        test("false") {
          val value = Try.flatCond(
            bfalse,
            42,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
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
          assertThrows[IllegalArgumentException](value.r)
        }
      }

      describe("flatCondThr") {
        test("false") {
          val value = Try.flatCondThr(
            bfalse,
            42,
            thr
          )
          assertThrows[RuntimeException](value.r)
        }

        test("true") {
          val value = Try.flatCondThr(
            btrue,
            42,
            thr
          )
          assert(value.r == 42)
        }

        test("fail") {
          val value = Try.flatCondThr(
            bfail,
            42,
            thr
          )
          assertThrows[IllegalArgumentException](value.r)
        }
      }

      describe("flatCondWith") {
        test("false — pure") {
          val value = Try.flatCondWith(
            bfalse,
            pureV,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("false — fail") {
          val value = Try.flatCondWith(
            bfalse,
            failV,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
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
          assertThrows[InvalidInputFailure](value.r)
        }

        test("fail — pure") {
          val value = Try.flatCondWith(
            bfail,
            pureV,
            ano
          )
          assertThrows[IllegalArgumentException](value.r)
        }

        test("fail — fail") {
          val value = Try.flatCondWith(
            bfail,
            failV,
            ano
          )
          assertThrows[IllegalArgumentException](value.r)
        }
      }

      describe("flatCondWithThr") {
        test("false — pure") {
          val value = Try.flatCondWithThr(
            bfalse,
            pureV,
            thr
          )
          assertThrows[RuntimeException](value.r)
        }

        test("false — fail") {
          val value = Try.flatCondWithThr(
            bfalse,
            failV,
            thr
          )
          assertThrows[RuntimeException](value.r)
        }

        test("true — pure") {
          val value = Try.flatCondWithThr(
            btrue,
            pureV,
            thr
          )
          assert(value.r == 42)
        }

        test("true — fail") {
          val value = Try.flatCondWithThr(
            btrue,
            failV,
            thr
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("fail — pure") {
          val value = Try.flatCondWithThr(
            bfail,
            pureV,
            thr
          )
          assertThrows[IllegalArgumentException](value.r)
        }

        test("fail — fail") {
          val value = Try.flatCondWithThr(
            bfail,
            failV,
            thr
          )
          assertThrows[IllegalArgumentException](value.r)
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
          assertThrows[InvalidInputFailure](value.r)
        }
      }

      describe("failOnTrueThr") {
        test("false") {
          val value = Try.failOnTrueThr(
            false,
            thr
          )
          value.r
        }

        test("true") {
          val value = Try.failOnTrueThr(
            true,
            thr
          )
          assertThrows[RuntimeException](value.r)
        }
      }

      describe("failOnFalse") {
        test("false") {
          val value = Try.failOnFalse(
            false,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("true") {
          val value = Try.failOnFalse(
            true,
            ano
          )
          value.r
        }
      }

      describe("failOnFalseThr") {
        test("false") {
          val value = Try.failOnFalseThr(
            false,
            thr
          )
          assertThrows[RuntimeException](value.r)
        }

        test("true") {
          val value = Try.failOnFalseThr(
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

      describe("flatFailOnTrueThr") {
        test("false") {
          val value = Try.flatFailOnTrueThr(
            bfalse,
            thr
          )
          value.r
        }

        test("true") {
          val value = Try.flatFailOnTrueThr(
            btrue,
            thr
          )
          assertThrows[RuntimeException](value.r)
        }

        test("fail") {
          val value = Try.flatFailOnTrueThr(
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

      describe("flatFailOnFalseThr") {
        test("false") {
          val value = Try.flatFailOnFalseThr(
            bfalse,
            thr
          )
          assertThrows[RuntimeException](value.r)
        }

        test("true") {
          val value = Try.flatFailOnFalseThr(
            btrue,
            thr
          )
          value.r
        }

        test("fail") {
          val value = Try.flatFailOnFalseThr(
            bfail,
            thr
          )
          assertThrows[IllegalArgumentException](value.r)
        }

      }

    } //end boolean

    describe("nested") {

      describe("unpack") {
        test("pure — none") {
          assertThrows[InvalidInputAnomaly] {
            Try.unpackOption(Try.pure(none), ano).r
          }
        }

        test("pure — some") {
          assert(Try.unpackOption(Try.pure(some), ano).r == 42)
        }

        test("fail") {
          assertThrows[RuntimeException] {
            Try.unpackOption(Try.failureThr[Option[Int]](thr), ano).r
          }
        }
      }

      describe("unpackThr") {

        test("pure — none") {
          assertThrows[RuntimeException] {
            Try.unpackOptionThr(Try.pure(none), thr).r
          }
        }

        test("pure — some") {
          assert(Try.unpackOptionThr(Try.pure(some), thr).r == 42)
        }

        test("fail") {
          assertThrows[InvalidInputFailure] {
            Try.unpackOptionThr(Try.failure[Option[Int]](ano), thr).r
          }
        }
      }

      describe("unpack") {
        test("incorrect") {
          assertThrows[InvalidInputFailure](
            Try.unpackResult(Try.pure(incorrect)).r
          )
        }

        test("correct") {
          assert(Try.unpackResult(Try.pure(correct)).r == 42)
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

      describe("bimapThr") {

        test("fail") {
          val value = Try.bimapThr(
            failV,
            int2str,
            thr2thr
          )

          assertThrows[IllegalArgumentException](value.r)
        }

        test("pure") {
          val value = Try.bimapThr(
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
          assertThrows[InvalidInputFailure](value.r)
        }

        test("true") {
          val value = true.condTry(
            42,
            ano
          )
          assert(value.r == 42)
        }
      }

      describe("condThr") {
        test("false") {
          val value =
            false.condTryThr(
              42,
              thr
            )
          assertThrows[RuntimeException](value.r)
        }

        test("true") {
          val value = true.condTryThr(
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
          assertThrows[InvalidInputFailure](value.r)
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
          assertThrows[InvalidInputFailure](value.r)
        }

        test("true — fail") {
          val value = true.condWithTry(
            failV,
            ano
          )
          assertThrows[InvalidInputFailure](value.r)
        }
      }

      describe("condWithThr") {
        test("false — pure") {
          val value = false.condWithTryThr(
            pureV,
            thr
          )
          assertThrows[RuntimeException](value.r)
        }

        test("true — pure") {
          val value = true.condWithTryThr(
            pureV,
            thr
          )
          assert(value.r == 42)
        }

        test("false — fail") {
          val value =
            false.condWithTryThr(
              failV,
              thr
            )
          assertThrows[RuntimeException](value.r)
        }

        test("true — fail") {
          val value = true.condWithTryThr(
            failV,
            thr
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
          assertThrows[IllegalArgumentException](value.r)
        }
      }

      describe("flatCondThr") {
        test("false") {
          val value = bfalse.condThr(
            42,
            thr
          )
          assertThrows[RuntimeException](value.r)
        }

        test("true") {
          val value = btrue.condThr(
            42,
            thr
          )
          assert(value.r == 42)
        }

        test("fail") {
          val value = bfail.condThr(
            42,
            thr
          )
          assertThrows[IllegalArgumentException](value.r)
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
          assertThrows[IllegalArgumentException](value.r)
        }

        test("fail — fail") {
          val value = bfail.condWith(
            failV,
            ano
          )
          assertThrows[IllegalArgumentException](value.r)
        }
      }

      describe("flatCondWithThr") {
        test("false — pure") {
          val value = bfalse.condWithThr(
            pureV,
            thr
          )
          assertThrows[RuntimeException](value.r)
        }

        test("false — fail") {
          val value = bfalse.condWithThr(
            failV,
            thr
          )
          assertThrows[RuntimeException](value.r)
        }

        test("true — pure") {
          val value = btrue.condWithThr(
            pureV,
            thr
          )
          assert(value.r == 42)
        }

        test("true — fail") {
          val value = btrue.condWithThr(
            failV,
            thr
          )
          assertThrows[InvalidInputFailure](value.r)
        }

        test("fail — pure") {
          val value = bfail.condWithThr(
            pureV,
            thr
          )
          assertThrows[IllegalArgumentException](value.r)
        }

        test("fail — fail") {
          val value = bfail.condWithThr(
            failV,
            thr
          )
          assertThrows[IllegalArgumentException](value.r)
        }
      }

      describe("failOnTrue") {
        test("false") {
          val value = false.failOnTrueTry(ano)
          value.r
        }

        test("true") {
          val value = true.failOnTrueTry(ano)
          assertThrows[InvalidInputFailure](value.r)
        }
      }

      describe("failOnTrueThr") {
        test("false") {
          val value = false.failOnTrueTryThr(thr)
          value.r
        }

        test("true") {
          val value = true.failOnTrueTryThr(thr)
          assertThrows[RuntimeException](value.r)
        }
      }

      describe("failOnFalse") {
        test("false") {
          val value = false.failOnFalseTry(ano)
          assertThrows[InvalidInputFailure](value.r)
        }

        test("true") {
          val value = true.failOnFalseTry(ano)
          value.r
        }
      }

      describe("failOnFalseThr") {
        test("false") {
          val value = false.failOnFalseTryThr(thr)
          assertThrows[RuntimeException](value.r)
        }

        test("true") {
          val value = true.failOnFalseTryThr(thr)
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

      describe("flatFailOnTrueThr") {
        test("false") {
          val value = bfalse.failOnTrueThr(thr)
          value.r
        }

        test("true") {
          val value = btrue.failOnTrueThr(thr)
          assertThrows[RuntimeException](value.r)
        }

        test("fail") {
          val value = bfail.failOnTrueThr(thr)
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

      describe("flatFailOnFalseThr") {
        test("false") {
          val value = bfalse.failOnFalseThr(thr)
          assertThrows[RuntimeException](value.r)
        }

        test("true") {
          val value = btrue.failOnFalseThr(thr)
          value.r
        }

        test("fail") {
          val value = bfail.failOnFalseThr(thr)
          assertThrows[IllegalArgumentException](value.r)
        }

      }

    } //end boolean

    describe("nested") {

      describe("unpack") {
        test("pure — none") {
          assertThrows[InvalidInputAnomaly] {
            Try.pure(none).unpack(ano).r
          }
        }

        test("pure — some") {
          assert(Try.pure(some).unpack(ano).r == 42)
        }

        test("fail") {
          assertThrows[RuntimeException] {
            Try.failureThr[Option[Int]](thr).unpack(ano).r
          }
        }
      }

      describe("unpackThr") {

        test("pure — none") {
          assertThrows[RuntimeException] {
            Try.pure(none).unpackThr(thr).r
          }
        }

        test("pure — some") {
          assert(Try.pure(some).unpackThr(thr).r == 42)
        }

        test("fail") {
          assertThrows[InvalidInputFailure] {
            Try.failure[Option[Int]](ano).unpackThr(thr).r
          }
        }
      }

      describe("unpack") {
        test("incorrect") {
          assertThrows[InvalidInputFailure](
            Try.pure(incorrect).unpack.r
          )
        }

        test("correct") {
          assert(Try.pure(correct).unpack.r == 42)
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

      describe("bimapThr") {

        test("fail") {
          val value = failV.bimapThr(
            int2str,
            thr2thr
          )

          assertThrows[IllegalArgumentException](value.r)
        }

        test("pure") {
          val value = pureV.bimapThr(
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
