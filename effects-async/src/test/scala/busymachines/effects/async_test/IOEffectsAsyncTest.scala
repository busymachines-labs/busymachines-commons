package busymachines.effects.async_test

import busymachines.core._
import busymachines.effects.async._
import busymachines.effects.sync._

import busymachines.effects.async.validated._
import busymachines.effects.sync.validated._

import org.scalatest._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Jan 2018
  *
  */
final class IOEffectsAsyncTest extends FunSpec {
  implicit val ec: Scheduler = Scheduler.global
  //prevents atrocious English
  private def test: ItWord = it

  implicit private class TestSyntax[T](value: IO[T]) {
    //short for "run"
    def r: T = value.unsafeSyncGet()
  }

  private def sleep(l: Long = 10L): Unit = Thread.sleep(l)

  //--------------------------------------------------------------------------

  private val thr: RuntimeException         = new RuntimeException("runtime_exception")
  private val iae: IllegalArgumentException = new IllegalArgumentException("illegal_argument_exception")

  private val ano: InvalidInputAnomaly = InvalidInputFailure("invalid_input_failure")

  private val none: Option[Int] = Option.empty
  private val some: Option[Int] = Option(42)

  private val success: Try[Int] = Try.pure(42)
  private val failure: Try[Int] = Try.fail(ano)

  private val left:  Either[Throwable, Int] = Left(thr)
  private val right: Either[Throwable, Int] = Right(42)

  private val correct:   Result[Int] = Result(42)
  private val incorrect: Result[Int] = Result.fail(ano)

  private val valid:   Validated[Int] = Validated.pure(42)
  private val invalid: Validated[Int] = Validated.fail(ano)

  private val failedF:  Future[Int] = Future.fail(ano)
  private val successF: Future[Int] = Future.pure(42)

  private val int2str: Int => String = i => i.toString
  private val res2str: Result[Int] => String = {
    case Correct(i)   => i.toString
    case Incorrect(t) => t.message
  }

  private val thr2str: Throwable => String    = thr => thr.getMessage
  private val thr2ano: Throwable => Anomaly   = _ => ForbiddenFailure
  private val thr2thr: Throwable => Throwable = _ => iae
  private val res2res: Result[Int] => Result[String] = {
    case Correct(i)   => Correct(i.toString)
    case Incorrect(_) => Incorrect(ForbiddenFailure)
  }

  private val failV: IO[Int] = IO.fail(ano)
  private val pureV: IO[Int] = IO.pure(42)

  private val btrue:  IO[Boolean] = IO.pure(true)
  private val bfalse: IO[Boolean] = IO.pure(false)
  private val bfail:  IO[Boolean] = IO.failThr(iae)

  //---------------------------------------------------------------------------
  describe("sync + pure") {

    describe("IO — companion object syntax") {

      describe("constructors") {
        test("pure") {
          assert(IO.pure(42).unsafeSyncGet() == 42)
        }

        test("fail") {
          assertThrows[InvalidInputFailure](IO.fail(ano).r)
          assertThrows[RuntimeException](IO.failThr(thr).r)
        }

        test("unit") {
          assert(IO.unit == IO.unit)
        }

        describe("fromOption") {
          test("none") {
            assertThrows[InvalidInputFailure](IO.fromOption(none, ano).r)
          }

          test("some") {
            assert(IO.fromOption(some, ano).r == 42)
          }
        }

        describe("fromOptionThr") {
          test("none") {
            assertThrows[RuntimeException](IO.fromOptionThr(none, thr).r)
          }

          test("some") {
            assert(IO.fromOptionThr(some, thr).r == 42)
          }
        }

        describe("fromTry") {

          test("failure") {
            assertThrows[InvalidInputFailure](IO.fromTry(failure).r)
          }

          test("success") {
            assert(IO.fromTry(success).r == 42)
          }
        }

        describe("fromEither") {
          test("left") {
            assertThrows[RuntimeException](IO.fromEitherThr(left).r)
          }

          test("left — transform") {
            assertThrows[ForbiddenFailure](IO.fromEitherAnomaly(left, thr2ano).r)
          }

          test("right") {
            assert(IO.fromEitherThr(right).r == 42)
          }

          test("right — transform") {
            assert(IO.fromEitherAnomaly(right, thr2ano).r == 42)
          }
        }

        describe("fromEitherThr") {
          test("left — transform") {
            assertThrows[IllegalArgumentException](IO.fromEitherThr(left, (_: Throwable) => iae).r)
          }

          test("right") {
            assert(IO.fromEitherThr(right, (_: Throwable) => iae).r == 42)
          }
        }

        describe("fromResult") {
          test("incorrect") {
            assertThrows[InvalidInputFailure](IO.fromResult(incorrect).r)
          }

          test("correct") {
            assert(IO.fromResult(correct).r == 42)
          }
        }

        describe("fromValidated") {
          test("invalid") {
            assertThrows[GenericValidationFailures](IO.fromValidated(invalid).r)
          }

          test("valid") {
            assert(IO.fromValidated(valid).r == 42)
          }

          test("invalid — ano") {
            assertThrows[TVFs](IO.fromValidated(invalid, TVFs).r)
          }

          test("valid — ano") {
            assert(IO.fromValidated(valid, TVFs).r == 42)
          }

        }

        describe("fromFuturePure") {
          test("failed") {
            assertThrows[InvalidInputFailure](IO.fromFuturePure(failedF).r)
          }

          test("success") {
            assert(IO.fromFuturePure(successF).r == 42)
          }
        }

        describe("fromTask") {
          test("fail") {
            assertThrows[InvalidInputFailure](IO.fromTask(Task.fail(ano)).r)
          }

          test("pure") {
            assert(IO.fromTask(Task.pure(42)).r == 42)
          }
        }

      } //end constructors

      describe("boolean") {

        describe("cond") {
          test("false") {
            val value = IO.cond(
              false,
              42,
              ano,
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = IO.cond(
              true,
              42,
              ano,
            )
            assert(value.r == 42)
          }
        }

        describe("condThr") {
          test("false") {
            val value = IO.condThr(
              false,
              42,
              thr,
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = IO.condThr(
              true,
              42,
              thr,
            )
            assert(value.r == 42)
          }
        }

        describe("condWith") {
          test("false — pure") {
            val value = IO.condWith(
              false,
              pureV,
              ano,
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true — pure") {
            val value = IO.condWith(
              true,
              pureV,
              ano,
            )
            assert(value.r == 42)
          }

          test("false — fail") {
            val value = IO.condWith(
              false,
              failV,
              ano,
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true — fail") {
            val value = IO.condWith(
              true,
              failV,
              ano,
            )
            assertThrows[InvalidInputFailure](value.r)
          }
        }

        describe("condWithThr") {
          test("false — pure") {
            val value = IO.condWithThr(
              false,
              pureV,
              thr,
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true — pure") {
            val value = IO.condWithThr(
              true,
              pureV,
              thr,
            )
            assert(value.r == 42)
          }

          test("false — fail") {
            val value = IO.condWithThr(
              false,
              failV,
              thr,
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true — fail") {
            val value = IO.condWithThr(
              true,
              failV,
              thr,
            )
            assertThrows[InvalidInputFailure](value.r)
          }
        }

        describe("flatCond") {
          test("false") {
            val value = IO.flatCond(
              bfalse,
              42,
              ano,
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = IO.flatCond(
              btrue,
              42,
              ano,
            )
            assert(value.r == 42)
          }

          test("fail") {
            val value = IO.flatCond(
              bfail,
              42,
              ano,
            )
            assertThrows[IllegalArgumentException](value.r)
          }
        }

        describe("flatCondThr") {
          test("false") {
            val value = IO.flatCondThr(
              bfalse,
              42,
              thr,
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = IO.flatCondThr(
              btrue,
              42,
              thr,
            )
            assert(value.r == 42)
          }

          test("fail") {
            val value = IO.flatCondThr(
              bfail,
              42,
              thr,
            )
            assertThrows[IllegalArgumentException](value.r)
          }
        }

        describe("flatCondWith") {
          test("false — pure") {
            val value = IO.flatCondWith(
              bfalse,
              pureV,
              ano,
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("false — fail") {
            val value = IO.flatCondWith(
              bfalse,
              failV,
              ano,
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true — pure") {
            val value = IO.flatCondWith(
              btrue,
              pureV,
              ano,
            )
            assert(value.r == 42)
          }

          test("true — fail") {
            val value = IO.flatCondWith(
              btrue,
              failV,
              ano,
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("fail — pure") {
            val value = IO.flatCondWith(
              bfail,
              pureV,
              ano,
            )
            assertThrows[IllegalArgumentException](value.r)
          }

          test("fail — fail") {
            val value = IO.flatCondWith(
              bfail,
              failV,
              ano,
            )
            assertThrows[IllegalArgumentException](value.r)
          }
        }

        describe("flatCondWithThr") {
          test("false — pure") {
            val value = IO.flatCondWithThr(
              bfalse,
              pureV,
              thr,
            )
            assertThrows[RuntimeException](value.r)
          }

          test("false — fail") {
            val value = IO.flatCondWithThr(
              bfalse,
              failV,
              thr,
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true — pure") {
            val value = IO.flatCondWithThr(
              btrue,
              pureV,
              thr,
            )
            assert(value.r == 42)
          }

          test("true — fail") {
            val value = IO.flatCondWithThr(
              btrue,
              failV,
              thr,
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("fail — pure") {
            val value = IO.flatCondWithThr(
              bfail,
              pureV,
              thr,
            )
            assertThrows[IllegalArgumentException](value.r)
          }

          test("fail — fail") {
            val value = IO.flatCondWithThr(
              bfail,
              failV,
              thr,
            )
            assertThrows[IllegalArgumentException](value.r)
          }
        }

        describe("failOnTrue") {
          test("false") {
            val value = IO.failOnTrue(
              false,
              ano,
            )
            value.r
          }

          test("true") {
            val value = IO.failOnTrue(
              true,
              ano,
            )
            assertThrows[InvalidInputFailure](value.r)
          }
        }

        describe("failOnTrueThr") {
          test("false") {
            val value = IO.failOnTrueThr(
              false,
              thr,
            )
            value.r
          }

          test("true") {
            val value = IO.failOnTrueThr(
              true,
              thr,
            )
            assertThrows[RuntimeException](value.r)
          }
        }

        describe("failOnFalse") {
          test("false") {
            val value = IO.failOnFalse(
              false,
              ano,
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = IO.failOnFalse(
              true,
              ano,
            )
            value.r
          }
        }

        describe("failOnFalseThr") {
          test("false") {
            val value = IO.failOnFalseThr(
              false,
              thr,
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = IO.failOnFalseThr(
              true,
              thr,
            )
            value.r
          }
        }

        describe("flatFailOnTrue") {
          test("false") {
            val value = IO.flatFailOnTrue(
              bfalse,
              ano,
            )
            value.r
          }

          test("true") {
            val value = IO.flatFailOnTrue(
              btrue,
              ano,
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("fail") {
            val value = IO.flatFailOnTrue(
              bfail,
              ano,
            )
            assertThrows[IllegalArgumentException](value.r)
          }

        }

        describe("flatFailOnTrueThr") {
          test("false") {
            val value = IO.flatFailOnTrueThr(
              bfalse,
              thr,
            )
            value.r
          }

          test("true") {
            val value = IO.flatFailOnTrueThr(
              btrue,
              thr,
            )
            assertThrows[RuntimeException](value.r)
          }

          test("fail") {
            val value = IO.flatFailOnTrueThr(
              bfail,
              thr,
            )
            assertThrows[IllegalArgumentException](value.r)
          }

        }

        describe("flatFailOnFalse") {
          test("false") {
            val value = IO.flatFailOnFalse(
              bfalse,
              ano,
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = IO.flatFailOnFalse(
              btrue,
              ano,
            )
            value.r
          }

          test("fail") {
            val value = IO.flatFailOnFalse(
              bfail,
              ano,
            )
            assertThrows[IllegalArgumentException](value.r)
          }

        }

        describe("flatFailOnFalseThr") {
          test("false") {
            val value = IO.flatFailOnFalseThr(
              bfalse,
              thr,
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = IO.flatFailOnFalseThr(
              btrue,
              thr,
            )
            value.r
          }

          test("fail") {
            val value = IO.flatFailOnFalseThr(
              bfail,
              thr,
            )
            assertThrows[IllegalArgumentException](value.r)
          }

        }

      } //end boolean

      describe("nested") {

        describe("unpack") {
          test("pure — none") {
            assertThrows[InvalidInputAnomaly] {
              IO.unpackOption(IO.pure(none), ano).r
            }
          }

          test("pure — some") {
            assert(IO.unpackOption(IO.pure(some), ano).r == 42)
          }

          test("fail") {
            assertThrows[RuntimeException] {
              IO.unpackOption(IO.failThr[Option[Int]](thr), ano).r
            }
          }
        }

        describe("unpackThr") {

          test("pure — none") {
            assertThrows[RuntimeException] {
              IO.unpackOptionThr(IO.pure(none), thr).r
            }
          }

          test("pure — some") {
            assert(IO.unpackOptionThr(IO.pure(some), thr).r == 42)
          }

          test("fail") {
            assertThrows[InvalidInputFailure] {
              IO.unpackOptionThr(IO.fail[Option[Int]](ano), thr).r
            }
          }
        }

        describe("unpack") {
          test("incorrect") {
            assertThrows[InvalidInputFailure](
              IO.unpackResult(IO.pure(incorrect)).r,
            )
          }

          test("correct") {
            assert(IO.unpackResult(IO.pure(correct)).r == 42)
          }
        }

      } //end nested

      describe("as{Effect}") {

        describe("attemptResult") {
          test("fail") {
            assert(IO.attemptResult(failV).r == incorrect)
          }

          test("pure") {
            assert(IO.attemptResult(pureV).r == correct)
          }

        }

        describe("unsafeGet") {

          test("fail") {
            assertThrows[InvalidInputFailure](IO.unsafeSyncGet(failV))
          }

          test("pure") {
            assert(IO.unsafeSyncGet(pureV) == 42)
          }

        }

      } //end as{Effect}

      describe("as{Effect} — reverse") {

        describe("option asIO") {
          test("fail") {
            assertThrows[InvalidInputFailure](Option.asIO(none, ano).r)
          }

          test("pure") {
            assert(Option.asIO(some, ano).r == 42)
          }
        }

        describe("option asIOThr") {
          test("fail") {
            assertThrows[IllegalArgumentException](Option.asIOThr(none, iae).r)
          }

          test("pure") {
            assert(Option.asIOThr(some, iae).r == 42)
          }
        }

        describe("try asIO") {
          test("fail") {
            assertThrows[InvalidInputFailure](Try.asIO(failure).r)
          }

          test("pure") {
            assert(Try.asIO(success).r == 42)
          }
        }

        describe("either asIO") {
          test("fail") {
            assertThrows[ForbiddenFailure](Either.asIO(left, thr2ano).r)
          }

          test("pure") {
            assert(Either.asIO(right, thr2ano).r == 42)
          }
        }

        describe("either asIOThr — transform") {
          test("fail") {
            assertThrows[RuntimeException](Either.asIOThr(left, thr2thr).r)
          }

          test("pure") {
            assert(Either.asIOThr(right, thr2thr).r == 42)
          }
        }

        describe("either asIOThr") {
          test("fail") {
            assertThrows[RuntimeException](Either.asIOThr(left).r)
          }

          test("pure") {
            assert(Either.asIOThr(right).r == 42)
          }
        }

        describe("result asIO") {
          test("fail") {
            assertThrows[InvalidInputFailure](Result.asIO(incorrect).r)
          }

          test("pure") {
            assert(Result.asIO(correct).r == 42)
          }
        }

        describe("validated asIO") {
          test("invalid") {
            assertThrows[GenericValidationFailures](Validated.asIO(invalid).r)
          }

          test("valid") {
            assert(Validated.asIO(valid).r == 42)
          }

          test("invalid — ano") {
            assertThrows[TVFs](Validated.asIO(invalid, TVFs).r)
          }

          test("valid — ano") {
            assert(Validated.asIO(valid, TVFs).r == 42)
          }
        }

        describe("future asIO") {
          test("fail") {
            assertThrows[InvalidInputFailure](Future.asIO(failedF).r)
          }

          test("pure") {
            assert(Future.asIO(successF).r == 42)
          }
        }

        describe("task asIO") {
          test("fail") {
            assertThrows[InvalidInputFailure](Task.asIO(Task.fail(ano)).r)
          }

          test("pure") {
            assert(Task.asIO(Task.pure(42)).r == 42)
          }

        }

      } //end as{Effect} — reverse

      describe("transformers") {

        describe("bimap") {

          test("fail") {
            val value = IO.bimap(
              failV,
              int2str,
              thr2ano,
            )

            assertThrows[ForbiddenFailure](value.r)
          }

          test("pure") {
            val value = IO.bimap(
              pureV,
              int2str,
              thr2ano,
            )

            assert(value.r == "42")
          }

        }

        describe("bimap — result") {

          test("fail") {
            val value = IO.bimap(
              failV,
              res2res,
            )

            assertThrows[ForbiddenFailure](value.r)
          }

          test("pure") {
            val value = IO.bimap(
              pureV,
              res2res,
            )

            assert(value.r == "42")
          }

        }

        describe("bimapThr") {

          test("fail") {
            val value = IO.bimapThr(
              failV,
              int2str,
              thr2thr,
            )

            assertThrows[IllegalArgumentException](value.r)
          }

          test("pure") {
            val value = IO.bimapThr(
              pureV,
              int2str,
              thr2thr,
            )

            assert(value.r == "42")
          }

        }

        describe("morph") {

          test("fail") {
            val value = IO.morph(
              failV,
              int2str,
              thr2str,
            )
            assert(value.r == ano.message)
          }

          test("pure") {
            val value = IO.morph(
              pureV,
              int2str,
              thr2str,
            )
            assert(value.r == "42")
          }
        }

        describe("morph — result") {

          test("fail") {
            val value = IO.morph(
              failV,
              res2str,
            )
            assert(value.r == ano.message)
          }

          test("pure") {
            val value = IO.morph(
              pureV,
              res2str,
            )
            assert(value.r == "42")
          }
        }

        describe("discardContent") {

          test("fail") {
            assertThrows[InvalidInputFailure](IO.discardContent(failV).r)
          }

          test("pure") {
            IO.discardContent(pureV).r
          }
        }

      } //end transformers

    } //end companion object syntax tests

    //===========================================================================
    //===========================================================================
    //===========================================================================

    describe("IO — reference syntax") {

      describe("boolean") {

        describe("cond") {
          test("false") {
            val value = false.condIO(
              42,
              ano,
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = true.condIO(
              42,
              ano,
            )
            assert(value.r == 42)
          }
        }

        describe("condThr") {
          test("false") {
            val value =
              false.condIOThr(
                42,
                thr,
              )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = true.condIOThr(
              42,
              thr,
            )
            assert(value.r == 42)
          }
        }

        describe("condWith") {
          test("false — pure") {
            val value = false.condWithIO(
              pureV,
              ano,
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true — pure") {
            val value = true.condWithIO(
              pureV,
              ano,
            )
            assert(value.r == 42)
          }

          test("false — fail") {
            val value = false.condWithIO(
              failV,
              ano,
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true — fail") {
            val value = true.condWithIO(
              failV,
              ano,
            )
            assertThrows[InvalidInputFailure](value.r)
          }
        }

        describe("condWithThr") {
          test("false — pure") {
            val value = false.condWithIOThr(
              pureV,
              thr,
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true — pure") {
            val value = true.condWithIOThr(
              pureV,
              thr,
            )
            assert(value.r == 42)
          }

          test("false — fail") {
            val value =
              false.condWithIOThr(
                failV,
                thr,
              )
            assertThrows[RuntimeException](value.r)
          }

          test("true — fail") {
            val value = true.condWithIOThr(
              failV,
              thr,
            )
            assertThrows[InvalidInputFailure](value.r)
          }
        }

        describe("flatCond") {
          test("false") {
            val value = bfalse.cond(
              42,
              ano,
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = btrue.cond(
              42,
              ano,
            )
            assert(value.r == 42)
          }

          test("fail") {
            val value = bfail.cond(
              42,
              ano,
            )
            assertThrows[IllegalArgumentException](value.r)
          }
        }

        describe("flatCondThr") {
          test("false") {
            val value = bfalse.condThr(
              42,
              thr,
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = btrue.condThr(
              42,
              thr,
            )
            assert(value.r == 42)
          }

          test("fail") {
            val value = bfail.condThr(
              42,
              thr,
            )
            assertThrows[IllegalArgumentException](value.r)
          }
        }

        describe("flatCondWith") {
          test("false — pure") {
            val value = bfalse.condWith(
              pureV,
              ano,
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("false — fail") {
            val value = bfalse.condWith(
              failV,
              ano,
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true — pure") {
            val value = btrue.condWith(
              pureV,
              ano,
            )
            assert(value.r == 42)
          }

          test("true — fail") {
            val value = btrue.condWith(
              failV,
              ano,
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("fail — pure") {
            val value = bfail.condWith(
              pureV,
              ano,
            )
            assertThrows[IllegalArgumentException](value.r)
          }

          test("fail — fail") {
            val value = bfail.condWith(
              failV,
              ano,
            )
            assertThrows[IllegalArgumentException](value.r)
          }
        }

        describe("flatCondWithThr") {
          test("false — pure") {
            val value = bfalse.condWithThr(
              pureV,
              thr,
            )
            assertThrows[RuntimeException](value.r)
          }

          test("false — fail") {
            val value = bfalse.condWithThr(
              failV,
              thr,
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true — pure") {
            val value = btrue.condWithThr(
              pureV,
              thr,
            )
            assert(value.r == 42)
          }

          test("true — fail") {
            val value = btrue.condWithThr(
              failV,
              thr,
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("fail — pure") {
            val value = bfail.condWithThr(
              pureV,
              thr,
            )
            assertThrows[IllegalArgumentException](value.r)
          }

          test("fail — fail") {
            val value = bfail.condWithThr(
              failV,
              thr,
            )
            assertThrows[IllegalArgumentException](value.r)
          }
        }

        describe("failOnTrue") {
          test("false") {
            val value = false.failOnTrueIO(ano)
            value.r
          }

          test("true") {
            val value = true.failOnTrueIO(ano)
            assertThrows[InvalidInputFailure](value.r)
          }
        }

        describe("failOnTrueThr") {
          test("false") {
            val value = false.failOnTrueIOThr(thr)
            value.r
          }

          test("true") {
            val value = true.failOnTrueIOThr(thr)
            assertThrows[RuntimeException](value.r)
          }
        }

        describe("failOnFalse") {
          test("false") {
            val value = false.failOnFalseIO(ano)
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = true.failOnFalseIO(ano)
            value.r
          }
        }

        describe("failOnFalseThr") {
          test("false") {
            val value = false.failOnFalseIOThr(thr)
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = true.failOnFalseIOThr(thr)
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
              IO.pure(none).unpack(ano).r
            }
          }

          test("pure — some") {
            assert(IO.pure(some).unpack(ano).r == 42)
          }

          test("fail") {
            assertThrows[RuntimeException] {
              IO.failThr[Option[Int]](thr).unpack(ano).r
            }
          }
        }

        describe("unpackThr") {

          test("pure — none") {
            assertThrows[RuntimeException] {
              IO.pure(none).unpackThr(thr).r
            }
          }

          test("pure — some") {
            assert(IO.pure(some).unpackThr(thr).r == 42)
          }

          test("fail") {
            assertThrows[InvalidInputFailure] {
              IO.fail[Option[Int]](ano).unpackThr(thr).r
            }
          }
        }

        describe("unpack") {
          test("incorrect") {
            assertThrows[InvalidInputFailure](
              IO.pure(incorrect).unpack.r,
            )
          }

          test("correct") {
            assert(IO.pure(correct).unpack.r == 42)
          }
        }

      } //end nested

      describe("as{Effect}") {

        describe("attemptResult") {

          test("fail") {
            assert(failV.attempResult.r == incorrect)
          }

          test("pure") {
            assert(pureV.attempResult.r == correct)
          }

        }

        describe("unsafeGet") {

          test("fail") {
            assertThrows[InvalidInputFailure](failV.unsafeSyncGet())
          }

          test("pure") {
            assert(pureV.unsafeSyncGet() == 42)
          }

        }

      } //end as{Effect}

      describe("as{Effect} — reverse") {

        describe("option asIO") {
          test("fail") {
            assertThrows[InvalidInputFailure](none.asIO(ano).r)
          }

          test("pure") {
            assert(some.asIO(ano).r == 42)
          }
        }

        describe("option asIOThr") {
          test("fail") {
            assertThrows[IllegalArgumentException](none.asIOThr(iae).r)
          }

          test("pure") {
            assert(some.asIOThr(iae).r == 42)
          }
        }

        describe("try asIO") {
          test("fail") {
            assertThrows[InvalidInputFailure](failure.asIO.r)
          }

          test("pure") {
            assert(success.asIO.r == 42)
          }
        }

        describe("either asIO") {
          test("fail") {
            assertThrows[ForbiddenFailure](left.asIO(thr2ano).r)
          }

          test("pure") {
            assert(right.asIO(thr2ano).r == 42)
          }
        }

        describe("either asIOThr — transform") {
          test("fail") {
            assertThrows[RuntimeException](left.asIOThr(thr2thr).r)
          }

          test("pure") {
            assert(right.asIOThr(thr2thr).r == 42)
          }
        }

        describe("either asIOThr") {
          test("fail") {
            assertThrows[RuntimeException](left.asIOThr.r)
          }

          test("pure") {
            assert(right.asIOThr.r == 42)
          }
        }

        describe("result asIO") {
          test("fail") {
            assertThrows[InvalidInputFailure](incorrect.asIO.r)
          }

          test("pure") {
            assert(correct.asIO.r == 42)
          }
        }

        describe("validated asFuture") {
          test("invalid") {
            assertThrows[GenericValidationFailures](invalid.asIO.r)
          }

          test("valid") {
            assert(valid.asIO.r == 42)
          }

          test("invalid — ano") {
            assertThrows[TVFs](invalid.asIO(TVFs).r)
          }

          test("valid — ano") {
            assert(valid.asIO(TVFs).r == 42)
          }
        }

        describe("future asIO") {
          test("fail") {
            assertThrows[InvalidInputFailure](failedF.asIO.r)
          }

          test("pure") {
            assert(successF.asIO.r == 42)
          }
        }

        describe("task asIO") {
          test("fail") {
            assertThrows[InvalidInputFailure](Task.fail(ano).asIO.r)
          }

          test("pure") {
            assert(Task.pure(42).asIO.r == 42)
          }

        }

      } //end as{Effect} — reverse

      describe("transformers") {

        describe("bimap") {

          test("fail") {
            val value = failV.bimap(
              int2str,
              thr2ano,
            )

            assertThrows[ForbiddenFailure](value.r)
          }

          test("pure") {
            val value = pureV.bimap(
              int2str,
              thr2ano,
            )

            assert(value.r == "42")
          }

        }

        describe("bimap — result") {

          test("fail") {
            val value = failV.bimap(
              res2res,
            )

            assertThrows[ForbiddenFailure](value.r)
          }

          test("pure") {
            val value = pureV.bimap(
              res2res,
            )

            assert(value.r == "42")
          }

        }

        describe("bimapThr") {

          test("fail") {
            val value = failV.bimapThr(
              int2str,
              thr2thr,
            )

            assertThrows[IllegalArgumentException](value.r)
          }

          test("pure") {
            val value = pureV.bimapThr(
              int2str,
              thr2thr,
            )

            assert(value.r == "42")
          }

        }

        describe("morph") {

          test("fail") {
            val value = failV.morph(
              int2str,
              thr2str,
            )
            assert(value.r == ano.message)
          }

          test("pure") {
            val value = pureV.morph(
              int2str,
              thr2str,
            )
            assert(value.r == "42")
          }
        }

        describe("morph — result") {

          test("fail") {
            val value = failV.morph(
              res2str,
            )
            assert(value.r == ano.message)
          }

          test("pure") {
            val value = pureV.morph(
              res2str,
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
  }

  //===========================================================================
  //===========================================================================
  //===========================================================================

  describe("async + impure") {

    describe("IO — companion object syntax") {

      describe("suspend") {

        test("suspendOption") {
          val f = IO.suspendOption(
            Option(throw thr),
            ano,
          )
          assertThrows[RuntimeException](f.r)

        }

        test("suspendOptionThr") {
          val f = IO.suspendOptionThr(
            Option(throw thr),
            iae,
          )
          assertThrows[RuntimeException](f.r)

        }

        test("suspendTry") {
          val f = IO.suspendTry(
            Try.pure(throw thr),
          )
          assertThrows[RuntimeException](f.r)
        }

        test("suspendEither") {
          val f = IO.suspendEither(
            Right[Throwable, String](throw thr),
            thr2ano,
          )
          assertThrows[RuntimeException](f.r)
        }

        test("suspendEitherThr") {
          val f = IO.suspendEitherThr(
            Right[Throwable, String](throw thr),
          )
          assertThrows[RuntimeException](f.r)
        }

        test("suspendEitherThr — transform") {
          val f = IO.suspendEitherThr(
            Right[Throwable, String](throw thr),
            thr2thr,
          )
          assertThrows[RuntimeException](f.r)
        }

        test("suspendResult") {
          val f = IO.suspendResult(
            Result.pure(throw thr),
          )
          assertThrows[RuntimeException](f.r)
        }

        describe("suspendValidated") {
          test("normal") {
            val f = IO.suspendValidated(
              Validated.pure(throw thr),
            )
            assertThrows[RuntimeException](f.r)
          }

          test("ano") {
            val f = IO.suspendValidated(
              Validated.pure(throw thr),
              TVFs,
            )
            assertThrows[RuntimeException](f.r)
          }
        }

        test("suspendFuture") {
          var sideEffect: Int = 0
          val f = IO.suspendFuture(
            Future {
              sideEffect = 42
              sideEffect
            },
          )
          if (sideEffect == 42) fail("side effect should not have been applied yet")
          f.r
          assert(sideEffect == 42)
        }

      } //end suspend

      describe("effect on boolean") {

        describe("effectOnFalse") {

          test("false") {
            var sideEffect: Int = 0
            val f = IO.effectOnFalse(
              false,
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("true") {
            var sideEffect: Int = 0
            val f = IO.effectOnFalse(
              true,
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

        }

        describe("effectOnTrue") {

          test("false") {
            var sideEffect: Int = 0
            val f = IO.effectOnTrue(
              false,
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("true") {
            var sideEffect: Int = 0
            val f = IO.effectOnTrue(
              true,
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }
        }

        describe("flatEffectOnFalse") {

          test("false") {
            var sideEffect: Int = 0
            val f = IO.flatEffectOnFalse(
              bfalse,
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("true") {
            var sideEffect: Int = 0
            val f = IO.flatEffectOnFalse(
              btrue,
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = IO.flatEffectOnFalse(
              bfail,
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            assertThrows[IllegalArgumentException](f.r)
            assert(sideEffect == 0, "side effect should not have applied on fail")

          }

        }

        describe("flatEffectOnTrue") {

          test("false") {
            var sideEffect: Int = 0
            val f = IO.flatEffectOnTrue(
              bfalse,
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("true") {
            var sideEffect: Int = 0
            val f = IO.flatEffectOnTrue(
              btrue,
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = IO.flatEffectOnTrue(
              bfail,
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            assertThrows[IllegalArgumentException](f.r)
            assert(sideEffect == 0, "side effect should not have applied on fail")

          }

        }
      }

      describe("effect on option") {

        describe("effectOnFail") {

          test("none") {
            var sideEffect: Int = 0
            val f = IO.effectOnFail(
              none,
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("some") {
            var sideEffect: Int = 0
            val f = IO.effectOnFail(
              some,
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }
        }

        describe("effectOnPure") {

          test("none") {
            var sideEffect: Int = 0
            val f = IO.effectOnPure(
              none,
              (x: Int) =>
                IO {
                  sideEffect = x
                  sideEffect
                },
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("some") {
            var sideEffect: Int = 0
            val f = IO.effectOnPure(
              some,
              (x: Int) =>
                IO {
                  sideEffect = x
                  sideEffect
                },
            )
            f.r
            assert(sideEffect == 42)

          }
        }

        describe("flatEffectOnNone") {

          test("none") {
            var sideEffect: Int = 0
            val f = IO.flatEffectOnNone(
              IO.pure(none),
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)
          }

          test("some") {
            var sideEffect: Int = 0
            val f = IO.flatEffectOnNone(
              IO.pure(some),
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = IO.flatEffectOnNone(
              IO.fail[Option[Int]](ano),
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            assertThrows[InvalidInputFailure](f.r)
            assert(sideEffect == 0, "side effect should not have applied on fail")

          }

        }

        describe("flatEffectOnSome") {

          test("none") {
            var sideEffect: Int = 0
            val f = IO.flatEffectOnSome(
              IO.pure(none),
              (x: Int) =>
                IO {
                  sideEffect = x
                  sideEffect
                },
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")
          }

          test("some") {
            var sideEffect: Int = 0
            val f = IO.flatEffectOnSome(
              IO.pure(some),
              (x: Int) =>
                IO {
                  sideEffect = x
                  sideEffect
                },
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = IO.flatEffectOnSome(
              IO.fail[Option[Int]](ano),
              (x: Int) =>
                IO {
                  sideEffect = x
                  sideEffect
                },
            )
            assertThrows[InvalidInputFailure](f.r)
            assert(sideEffect == 0, "side effect should not have applied on fail")

          }

        }

      }

      describe("effect on result") {

        describe("effectOnFail") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f = IO.effectOnFail(
              incorrect,
              (_: Anomaly) =>
                IO {
                  sideEffect = 42
                  sideEffect
                },
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("correct") {
            var sideEffect: Int = 0
            val f = IO.effectOnFail(
              correct,
              (_: Anomaly) =>
                IO {
                  sideEffect = 42
                  sideEffect
                },
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }
        }

        describe("effectOnPure") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f = IO.effectOnPure(
              incorrect,
              (x: Int) =>
                IO {
                  sideEffect = x
                  sideEffect
                },
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("correct") {
            var sideEffect: Int = 0
            val f = IO.effectOnPure(
              correct,
              (x: Int) =>
                IO {
                  sideEffect = x
                  sideEffect
                },
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }
        }

        describe("flatEffectOnIncorrect") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f = IO.flatEffectOnIncorrect(
              IO.pure(incorrect),
              (_: Anomaly) =>
                IO {
                  sideEffect = 42
                  sideEffect
                },
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)
          }

          test("correct") {
            var sideEffect: Int = 0
            val f = IO.flatEffectOnIncorrect(
              IO.pure(correct),
              (_: Anomaly) =>
                IO {
                  sideEffect = 42
                  sideEffect
                },
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = IO.flatEffectOnIncorrect(
              IO.fail[Result[Int]](ano),
              (_: Anomaly) =>
                IO {
                  sideEffect = 42
                  sideEffect
                },
            )
            assertThrows[InvalidInputFailure](f.r)
            assert(sideEffect == 0, "side effect should not have applied on fail")

          }

        }

        describe("flatEffectOnCorrect") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f = IO.flatEffectOnCorrect(
              IO.pure(incorrect),
              (x: Int) =>
                IO {
                  sideEffect = x
                  sideEffect
                },
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")
          }

          test("correct") {
            var sideEffect: Int = 0
            val f = IO.flatEffectOnCorrect(
              IO.pure(correct),
              (x: Int) =>
                IO {
                  sideEffect = x
                  sideEffect
                },
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = IO.flatEffectOnCorrect(
              IO.fail[Result[Int]](ano),
              (x: Int) =>
                IO {
                  sideEffect = x
                  sideEffect
                },
            )
            assertThrows[InvalidInputFailure](f.r)
            assert(sideEffect == 0, "side effect should not have applied on fail")

          }

        }

      }

    }

    describe("IO — other effect reference syntax") {

      describe("suspendInIO") {

        describe("reference") {
          test("suspendOption") {
            val f = Option(throw thr).suspendInIO(ano)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendOptionThr") {
            val f = Option(throw thr).suspendInIOThr(thr)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendTry") {
            val f = Try.pure(throw thr).suspendInIO
            assertThrows[RuntimeException](f.r)
          }

          test("suspendEither") {
            val f = Right[Throwable, String](throw thr).suspendInIO(thr2ano)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendEitherThr") {
            val f = Right[Throwable, String](throw thr).suspendInIOThr
            assertThrows[RuntimeException](f.r)
          }

          test("suspendEitherThr — transform") {
            val f = Right[Throwable, String](throw thr).suspendInIOThr(thr2thr)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendResult") {
            val f = Result.pure(throw thr).suspendInIO
            assertThrows[RuntimeException](f.r)
          }

          describe("suspendValidated") {
            test("normal") {
              val f = Validated.pure(throw thr).suspendInIO
              assertThrows[RuntimeException](f.r)
            }

            test("ano") {
              val f = Validated.pure(throw thr).suspendInIO(TVFs)
              assertThrows[RuntimeException](f.r)
            }
          }

          test("suspendFuture") {
            var sideEffect: Int = 0
            val f = Future[Int] {
              sideEffect = 42
              sideEffect
            }.suspendInIO
            sleep()
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)
          }
        }

        describe("companion") {

          test("suspendOption") {
            val f = Option.suspendInIO(Option(throw thr), ano)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendOptionThr") {
            val f = Option.suspendInIOThr(Option(throw thr), thr)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendTry") {
            val f = Try.suspendInIO(Try.pure(throw thr))
            assertThrows[RuntimeException](f.r)
          }

          test("suspendEither") {
            val f = Either.suspendInIO(Right[Throwable, String](throw thr), thr2ano)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendEitherThr") {
            val f = Either.suspendInIOThr(Right[Throwable, String](throw thr))
            assertThrows[RuntimeException](f.r)
          }

          test("suspendEitherThr — transform") {
            val f = Either.suspendInIOThr(Right[Throwable, String](throw thr), thr2thr)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendResult") {
            val f = Result.suspendInIO(Result.pure(throw thr))
            assertThrows[RuntimeException](f.r)
          }

          describe("suspendValidated") {
            test("normal") {
              val f = Validated.suspendInIO(Validated.pure(throw thr))
              assertThrows[RuntimeException](f.r)
            }

            test("ano") {
              val f = Validated.suspendInIO(Validated.pure(throw thr), TVFs)
              assertThrows[RuntimeException](f.r)
            }
          }

          test("suspendFuture") {
            var sideEffect: Int = 0
            val f = Future.suspendInIO {
              Future[Int] {
                sideEffect = 42
                sideEffect
              }
            }
            sleep()
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)
          }
        }

      } //end suspend

      describe("effect on boolean") {

        describe("effectOnFalse") {

          test("false") {
            var sideEffect: Int = 0
            val f = false.effectOnFalseIO(
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("true") {
            var sideEffect: Int = 0
            val f = true.effectOnFalseIO(
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

        }

        describe("effectOnTrue") {

          test("false") {
            var sideEffect: Int = 0
            val f = false.effectOnTrueIO(
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("true") {
            var sideEffect: Int = 0
            val f = true.effectOnTrueIO(
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }
        }

        describe("flatEffectOnFalse") {

          test("false") {
            var sideEffect: Int = 0
            val f = bfalse.effectOnFalse(
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("true") {
            var sideEffect: Int = 0
            val f = btrue.effectOnFalse(
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = bfail.effectOnFalse(
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            assertThrows[IllegalArgumentException](f.r)
            assert(sideEffect == 0, "side effect should not have applied on fail")

          }

        }

        describe("flatEffectOnTrue") {

          test("false") {
            var sideEffect: Int = 0
            val f = bfalse.effectOnTrue(
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("true") {
            var sideEffect: Int = 0
            val f = btrue.effectOnTrue(
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = bfail.effectOnTrue(
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            assertThrows[IllegalArgumentException](f.r)
            assert(sideEffect == 0, "side effect should not have applied on fail")

          }

        }
      }

      describe("effect on option") {

        describe("effectOnFail") {

          test("none") {
            var sideEffect: Int = 0
            val f = none.effectOnFailIO(
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("some") {
            var sideEffect: Int = 0
            val f = some.effectOnFailIO(
              IO {
                sideEffect = 42
                sideEffect
              },
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }
        }

        describe("effectOnPure") {

          test("none") {
            var sideEffect: Int = 0
            val f = none.effectOnPureIO(
              (x: Int) =>
                IO {
                  sideEffect = x
                  sideEffect
                },
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("some") {
            var sideEffect: Int = 0
            val f = some.effectOnPureIO(
              (x: Int) =>
                IO {
                  sideEffect = x
                  sideEffect
                },
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }
        }

        describe("flatEffectOnNone") {

          test("none") {
            var sideEffect: Int = 0
            val f = IO
              .pure(none)
              .effectOnFail(
                IO {
                  sideEffect = 42
                  sideEffect
                },
              )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)
          }

          test("some") {
            var sideEffect: Int = 0
            val f =
              IO.pure(some)
                .effectOnFail(
                  IO {
                    sideEffect = 42
                    sideEffect
                  },
                )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = IO
              .fail[Option[Int]](ano)
              .effectOnFail(
                IO {
                  sideEffect = 42
                  sideEffect
                },
              )
            assertThrows[InvalidInputFailure](f.r)
            assert(sideEffect == 0, "side effect should not have applied on fail")

          }

        }

        describe("flatEffectOnSome") {

          test("none") {
            var sideEffect: Int = 0
            val f = IO
              .pure(none)
              .effectOnPure(
                (x: Int) =>
                  IO {
                    sideEffect = x
                    sideEffect
                  },
              )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")
          }

          test("some") {
            var sideEffect: Int = 0
            val f = IO
              .pure(some)
              .effectOnPure(
                (x: Int) =>
                  IO {
                    sideEffect = x
                    sideEffect
                  },
              )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = IO
              .fail[Option[Int]](ano)
              .effectOnPure(
                (x: Int) =>
                  IO {
                    sideEffect = x
                    sideEffect
                  },
              )
            assertThrows[InvalidInputFailure](f.r)
            assert(sideEffect == 0, "side effect should not have applied on fail")

          }

        }

      }

      describe("effect on result") {

        describe("effectOnFail") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f = incorrect.effectOnFailIO(
              (_: Anomaly) =>
                IO {
                  sideEffect = 42
                  sideEffect
                },
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("correct") {
            var sideEffect: Int = 0
            val f = correct.effectOnFailIO(
              (_: Anomaly) =>
                IO {
                  sideEffect = 42
                  sideEffect
                },
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }
        }

        describe("effectOnPure") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f = incorrect.effectOnPureIO(
              (x: Int) =>
                IO {
                  sideEffect = x
                  sideEffect
                },
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("correct") {
            var sideEffect: Int = 0
            val f = correct.effectOnPureIO(
              (x: Int) =>
                IO {
                  sideEffect = x
                  sideEffect
                },
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }
        }

        describe("flatEffectOnIncorrect") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f = IO
              .pure(incorrect)
              .effectOnFail(
                (_: Anomaly) =>
                  IO {
                    sideEffect = 42
                    sideEffect
                  },
              )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)
          }

          test("correct") {
            var sideEffect: Int = 0
            val f = IO
              .pure(correct)
              .effectOnFail(
                (_: Anomaly) =>
                  IO {
                    sideEffect = 42
                    sideEffect
                  },
              )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = IO
              .fail[Result[Int]](ano)
              .effectOnFail(
                (_: Anomaly) =>
                  IO {
                    sideEffect = 42
                    sideEffect
                  },
              )
            assertThrows[InvalidInputFailure](f.r)
            assert(sideEffect == 0, "side effect should not have applied on fail")

          }

        }

        describe("flatEffectOnCorrect") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f =
              IO.pure(incorrect)
                .effectOnPure(
                  (x: Int) =>
                    IO {
                      sideEffect = x
                      sideEffect
                    },
                )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")
          }

          test("correct") {
            var sideEffect: Int = 0
            val f = IO
              .pure(correct)
              .effectOnPure(
                (x: Int) =>
                  IO {
                    sideEffect = x
                    sideEffect
                  },
              )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("fail") {
            var sideEffect: Int = 0
            val f =
              IO.fail[Result[Int]](ano)
                .effectOnPure(
                  (x: Int) =>
                    IO {
                      sideEffect = x
                      sideEffect
                    },
                )
            assertThrows[InvalidInputFailure](f.r)
            assert(sideEffect == 0, "side effect should not have applied on fail")

          }

        }

      }
    }

    describe("traversals") {
      describe("IO.traverse") {

        test("empty list") {
          val input:    Seq[Int] = List()
          val expected: Seq[Int] = List()

          var sideEffect: Int = 0

          val eventualResult = IO.traverse(input) { _ =>
            IO {
              sideEffect = 42
            }
          }

          assert(eventualResult.r == expected)
          assert(sideEffect == 0, "nothing should have happened")
        }

        test("no two IOs should run in parallel") {
          val input: Seq[Int] = (1 to 100).toList
          val expected = input.map(_.toString)

          var previouslyProcessed: Option[Int] = None
          var startedFlag:         Option[Int] = None

          val eventualResult: IO[Seq[String]] = IO.traverse(input) { i =>
            IO {
              assert(
                startedFlag.isEmpty,
                s"started flag should have been empty at the start of each IO but was: $startedFlag",
              )
              previouslyProcessed foreach { previous =>
                assertResult(expected = i - 1, "... the IOs were not executed in the correct order.")(
                  actual = previous,
                )
              }
              startedFlag         = Some(i)
              startedFlag         = None
              previouslyProcessed = Some(i)
              i.toString
            }
          }
          assert(expected == eventualResult.r)
        }

      }

      describe("IO.traverse_") {

        test("empty list") {
          val input: Seq[Int] = List()

          var sideEffect: Int = 0

          val eventualResult = IO.traverse_(input) { _ =>
            IO {
              sideEffect = 42
            }
          }

          eventualResult.r
          assert(sideEffect == 0, "nothing should have happened")
        }

      }

      describe("IO.sequence") {

        test("empty list") {
          val input:    Seq[IO[Int]] = List()
          val expected: Seq[Int]     = List()

          val eventualResult = IO.sequence(input)
          assert(eventualResult.r == expected)
        }

        test("no two IOs should run in parallel") {
          val nrs = (1 to 100).toList
          val input: Seq[IO[Int]] = (1 to 100).toList.map(IO.pure)
          val expected = nrs.map(_.toString)

          var previouslyProcessed: Option[Int] = None
          var startedFlag:         Option[Int] = None

          val eventualResult: IO[Seq[String]] = IO.sequence(input map { io =>
            io.map { i =>
              assert(
                startedFlag.isEmpty,
                s"started flag should have been empty at the start of each IO but was: $startedFlag",
              )
              previouslyProcessed foreach { previous =>
                assertResult(expected = i - 1, "... the IOs were not executed in the correct order.")(
                  actual = previous,
                )
              }
              startedFlag         = Some(i)
              startedFlag         = None
              previouslyProcessed = Some(i)
              i.toString
            }
          })
          assert(expected == eventualResult.r)
        }

      }

      describe("IO.sequence_") {

        test("empty list") {
          val input: Seq[IO[Int]] = List()

          val eventualResult = IO.sequence_(input)
          eventualResult.r
        }
      }

      describe("IO.serialize") {

        test("empty list") {
          val input:    Seq[Int] = List()
          val expected: Seq[Int] = List()

          var sideEffect: Int = 0

          val eventualResult = IO.serialize(input) { _ =>
            IO {
              sideEffect = 42
            }
          }

          assert(eventualResult.r == expected)
          assert(sideEffect == 0, "nothing should have happened")
        }

        test("no two IOs should run in parallel") {
          val input: Seq[Int] = (1 to 100).toList
          val expected = input.map(_.toString)

          var previouslyProcessed: Option[Int] = None
          var startedFlag:         Option[Int] = None

          val eventualResult: IO[Seq[String]] = IO.serialize(input) { i =>
            IO {
              assert(
                startedFlag.isEmpty,
                s"started flag should have been empty at the start of each IO but was: $startedFlag",
              )
              previouslyProcessed foreach { previous =>
                assertResult(expected = i - 1, "... the IOs were not executed in the correct order.")(
                  actual = previous,
                )
              }
              startedFlag         = Some(i)
              startedFlag         = None
              previouslyProcessed = Some(i)
              i.toString
            }
          }
          assert(expected == eventualResult.r)
        }

      }

      describe("IO.serialize_") {

        test("empty list") {
          val input: Seq[Int] = List()

          var sideEffect: Int = 0

          val eventualResult = IO.serialize_(input) { _ =>
            IO {
              sideEffect = 42
            }
          }

          eventualResult.r
          assert(sideEffect == 0, "nothing should have happened")
        }
      }
    }

  }

} //end test
