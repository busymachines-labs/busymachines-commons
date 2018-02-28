package busymachines.effects.async_test

import busymachines.core._
import busymachines.effects.sync._
import busymachines.effects.async._

import busymachines.effects.sync.validated._
import busymachines.effects.async.validated._

import org.scalatest._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Jan 2018
  *
  */
final class FutureEffectsAsyncTest extends FunSpec {
  implicit val ec: ExecutionContext = ExecutionContext.global
  //prevents atrocious English
  private def test: ItWord = it

  private implicit class TestSyntax[T](value: Future[T]) {
    //short for "run"
    def r: T = value.unsafeSyncGet()
  }

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

  private val int2str: Int => String = i => i.toString
  private val res2str: Result[Int] => String = {
    case Correct(i)   => i.toString
    case Incorrect(t) => t.message
  }

  private val thr2str: Throwable => String    = thr => thr.getMessage
  private val thr2ano: Throwable => Anomaly   = thr => ForbiddenFailure
  private val thr2thr: Throwable => Throwable = thr => iae
  private val res2res: Result[Int] => Result[String] = {
    case Correct(i)   => Correct(i.toString)
    case Incorrect(_) => Incorrect(ForbiddenFailure)
  }

  private val failV: Future[Int] = Future.fail(ano)
  private val pureV: Future[Int] = Future.pure(42)

  private val btrue:  Future[Boolean] = Future.pure(true)
  private val bfalse: Future[Boolean] = Future.pure(false)
  private val bfail:  Future[Boolean] = Future.failThr(iae)

  //---------------------------------------------------------------------------
  describe("sync + pure") {

    describe("Future — companion object syntax") {

      describe("constructors") {
        test("pure") {
          assert(Future.pure(42).unsafeSyncGet() == 42)
        }

        test("fail") {
          assertThrows[InvalidInputFailure](Future.fail(ano).r)
          assertThrows[RuntimeException](Future.failThr(thr).r)
        }

        test("unit") {
          assert(Future.unit == Future.unit)
        }

        describe("fromOption") {
          test("none") {
            assertThrows[InvalidInputFailure](Future.fromOption(none, ano).r)
          }

          test("some") {
            assert(Future.fromOption(some, ano).r == 42)
          }
        }

        describe("fromOptionThr") {
          test("none") {
            assertThrows[RuntimeException](Future.fromOptionThr(none, thr).r)
          }

          test("some") {
            assert(Future.fromOptionThr(some, thr).r == 42)
          }
        }

        describe("fromTry") {

          test("failure") {
            assertThrows[InvalidInputFailure](Future.fromTry(failure).r)
          }

          test("success") {
            assert(Future.fromTry(success).r == 42)
          }
        }

        describe("fromEither") {
          test("left") {
            assertThrows[RuntimeException](Future.fromEitherThr(left).r)
          }

          test("left — transform") {
            assertThrows[ForbiddenFailure](Future.fromEither(left, thr2ano).r)
          }

          test("right") {
            assert(Future.fromEitherThr(right).r == 42)
          }

          test("right — transform") {
            assert(Future.fromEither(right, thr2ano).r == 42)
          }
        }

        describe("fromEitherThr") {
          test("left — transform") {
            assertThrows[IllegalArgumentException](Future.fromEitherThr(left, (t: Throwable) => iae).r)
          }

          test("right") {
            assert(Future.fromEitherThr(right, (t: Throwable) => iae).r == 42)
          }
        }

        describe("fromResult") {
          test("incorrect") {
            assertThrows[InvalidInputFailure](Future.fromResult(incorrect).r)
          }

          test("correct") {
            assert(Future.fromResult(correct).r == 42)
          }
        }

        describe("fromValidated") {
          test("invalid") {
            assertThrows[GenericValidationFailures](Future.fromValidated(invalid).r)
          }

          test("valid") {
            assert(Future.fromValidated(valid).r == 42)
          }

          test("invalid — ano") {
            assertThrows[TVFs](Future.fromValidated(invalid, TVFs).r)
          }

          test("valid — ano") {
            assert(Future.fromValidated(valid, TVFs).r == 42)
          }

        }

      } //end constructors

      describe("boolean") {

        describe("cond") {
          test("false") {
            val value = Future.cond(
              false,
              42,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = Future.cond(
              true,
              42,
              ano
            )
            assert(value.r == 42)
          }
        }

        describe("condThr") {
          test("false") {
            val value = Future.condThr(
              false,
              42,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = Future.condThr(
              true,
              42,
              thr
            )
            assert(value.r == 42)
          }
        }

        describe("condWith") {
          test("false — pure") {
            val value = Future.condWith(
              false,
              pureV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true — pure") {
            val value = Future.condWith(
              true,
              pureV,
              ano
            )
            assert(value.r == 42)
          }

          test("false — fail") {
            val value = Future.condWith(
              false,
              failV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true — fail") {
            val value = Future.condWith(
              true,
              failV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }
        }

        describe("condWithThr") {
          test("false — pure") {
            val value = Future.condWithThr(
              false,
              pureV,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true — pure") {
            val value = Future.condWithThr(
              true,
              pureV,
              thr
            )
            assert(value.r == 42)
          }

          test("false — fail") {
            val value = Future.condWithThr(
              false,
              failV,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true — fail") {
            val value = Future.condWithThr(
              true,
              failV,
              thr
            )
            assertThrows[InvalidInputFailure](value.r)
          }
        }

        describe("flatCond") {
          test("false") {
            val value = Future.flatCond(
              bfalse,
              42,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = Future.flatCond(
              btrue,
              42,
              ano
            )
            assert(value.r == 42)
          }

          test("fail") {
            val value = Future.flatCond(
              bfail,
              42,
              ano
            )
            assertThrows[IllegalArgumentException](value.r)
          }
        }

        describe("flatCondThr") {
          test("false") {
            val value = Future.flatCondThr(
              bfalse,
              42,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = Future.flatCondThr(
              btrue,
              42,
              thr
            )
            assert(value.r == 42)
          }

          test("fail") {
            val value = Future.flatCondThr(
              bfail,
              42,
              thr
            )
            assertThrows[IllegalArgumentException](value.r)
          }
        }

        describe("flatCondWith") {
          test("false — pure") {
            val value = Future.flatCondWith(
              bfalse,
              pureV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("false — fail") {
            val value = Future.flatCondWith(
              bfalse,
              failV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true — pure") {
            val value = Future.flatCondWith(
              btrue,
              pureV,
              ano
            )
            assert(value.r == 42)
          }

          test("true — fail") {
            val value = Future.flatCondWith(
              btrue,
              failV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("fail — pure") {
            val value = Future.flatCondWith(
              bfail,
              pureV,
              ano
            )
            assertThrows[IllegalArgumentException](value.r)
          }

          test("fail — fail") {
            val value = Future.flatCondWith(
              bfail,
              failV,
              ano
            )
            assertThrows[IllegalArgumentException](value.r)
          }
        }

        describe("flatCondWithThr") {
          test("false — pure") {
            val value = Future.flatCondWithThr(
              bfalse,
              pureV,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("false — fail") {
            val value = Future.flatCondWithThr(
              bfalse,
              failV,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true — pure") {
            val value = Future.flatCondWithThr(
              btrue,
              pureV,
              thr
            )
            assert(value.r == 42)
          }

          test("true — fail") {
            val value = Future.flatCondWithThr(
              btrue,
              failV,
              thr
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("fail — pure") {
            val value = Future.flatCondWithThr(
              bfail,
              pureV,
              thr
            )
            assertThrows[IllegalArgumentException](value.r)
          }

          test("fail — fail") {
            val value = Future.flatCondWithThr(
              bfail,
              failV,
              thr
            )
            assertThrows[IllegalArgumentException](value.r)
          }
        }

        describe("failOnTrue") {
          test("false") {
            val value = Future.failOnTrue(
              false,
              ano
            )
            value.r
          }

          test("true") {
            val value = Future.failOnTrue(
              true,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }
        }

        describe("failOnTrueThr") {
          test("false") {
            val value = Future.failOnTrueThr(
              false,
              thr
            )
            value.r
          }

          test("true") {
            val value = Future.failOnTrueThr(
              true,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }
        }

        describe("failOnFalse") {
          test("false") {
            val value = Future.failOnFalse(
              false,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = Future.failOnFalse(
              true,
              ano
            )
            value.r
          }
        }

        describe("failOnFalseThr") {
          test("false") {
            val value = Future.failOnFalseThr(
              false,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = Future.failOnFalseThr(
              true,
              thr
            )
            value.r
          }
        }

        describe("flatFailOnTrue") {
          test("false") {
            val value = Future.flatFailOnTrue(
              bfalse,
              ano
            )
            value.r
          }

          test("true") {
            val value = Future.flatFailOnTrue(
              btrue,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("fail") {
            val value = Future.flatFailOnTrue(
              bfail,
              ano
            )
            assertThrows[IllegalArgumentException](value.r)
          }

        }

        describe("flatFailOnTrueThr") {
          test("false") {
            val value = Future.flatFailOnTrueThr(
              bfalse,
              thr
            )
            value.r
          }

          test("true") {
            val value = Future.flatFailOnTrueThr(
              btrue,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("fail") {
            val value = Future.flatFailOnTrueThr(
              bfail,
              thr
            )
            assertThrows[IllegalArgumentException](value.r)
          }

        }

        describe("flatFailOnFalse") {
          test("false") {
            val value = Future.flatFailOnFalse(
              bfalse,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = Future.flatFailOnFalse(
              btrue,
              ano
            )
            value.r
          }

          test("fail") {
            val value = Future.flatFailOnFalse(
              bfail,
              ano
            )
            assertThrows[IllegalArgumentException](value.r)
          }

        }

        describe("flatFailOnFalseThr") {
          test("false") {
            val value = Future.flatFailOnFalseThr(
              bfalse,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = Future.flatFailOnFalseThr(
              btrue,
              thr
            )
            value.r
          }

          test("fail") {
            val value = Future.flatFailOnFalseThr(
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
              Future.unpackOption(Future.pure(none), ano).r
            }
          }

          test("pure — some") {
            assert(Future.unpackOption(Future.pure(some), ano).r == 42)
          }

          test("fail") {
            assertThrows[RuntimeException] {
              Future.unpackOption(Future.failThr[Option[Int]](thr), ano).r
            }
          }
        }

        describe("unpackThr") {

          test("pure — none") {
            assertThrows[RuntimeException] {
              Future.unpackOptionThr(Future.pure(none), thr).r
            }
          }

          test("pure — some") {
            assert(Future.unpackOptionThr(Future.pure(some), thr).r == 42)
          }

          test("fail") {
            assertThrows[InvalidInputFailure] {
              Future.unpackOptionThr(Future.fail[Option[Int]](ano), thr).r
            }
          }
        }

        describe("unpack") {
          test("incorrect") {
            assertThrows[InvalidInputFailure](
              Future.unpackResult(Future.pure(incorrect)).r
            )
          }

          test("correct") {
            assert(Future.unpackResult(Future.pure(correct)).r == 42)
          }
        }

      } //end nested

      describe("as{Effect}") {

        describe("attemptResult") {
          test("fail") {
            assert(Future.attemptResult(failV).r == incorrect)
          }

          test("pure") {
            assert(Future.attemptResult(pureV).r == correct)
          }

        }

        describe("unsafeGet") {

          test("fail") {
            assertThrows[InvalidInputFailure](Future.unsafeSyncGet(failV))
          }

          test("pure") {
            assert(Future.unsafeSyncGet(pureV) == 42)
          }

        }

      } //end as{Effect}

      describe("as{Effect} — reverse") {

        describe("option asFuture") {
          test("fail") {
            assertThrows[InvalidInputFailure](Option.asFuture(none, ano).r)
          }

          test("pure") {
            assert(Option.asFuture(some, ano).r == 42)
          }
        }

        describe("option asFutureThr") {
          test("fail") {
            assertThrows[IllegalArgumentException](Option.asFutureThr(none, iae).r)
          }

          test("pure") {
            assert(Option.asFutureThr(some, iae).r == 42)
          }
        }

        describe("try asFuture") {
          test("fail") {
            assertThrows[InvalidInputFailure](Try.asFuture(failure).r)
          }

          test("pure") {
            assert(Try.asFuture(success).r == 42)
          }
        }

        describe("either asFuture — transform") {
          test("fail") {
            assertThrows[ForbiddenFailure](Either.asFuture(left, thr2ano).r)
          }

          test("pure") {
            assert(Either.asFuture(right, thr2ano).r == 42)
          }
        }

        describe("either asFutureThr — transform") {
          test("fail") {
            assertThrows[IllegalArgumentException](Either.asFutureThr(left, thr2thr).r)
          }

          test("pure") {
            assert(Either.asFutureThr(right, thr2thr).r == 42)
          }
        }

        describe("either asFutureThr") {
          test("fail") {
            assertThrows[RuntimeException](Either.asFutureThr(left).r)
          }

          test("pure") {
            assert(Either.asFutureThr(right).r == 42)
          }
        }

        describe("result asFuture") {
          test("fail") {
            assertThrows[InvalidInputFailure](Result.asFuture(incorrect).r)
          }

          test("pure") {
            assert(Result.asFuture(correct).r == 42)
          }
        }

        describe("validated asFuture") {
          test("invalid") {
            assertThrows[GenericValidationFailures](Validated.asFuture(invalid).r)
          }

          test("valid") {
            assert(Validated.asFuture(valid).r == 42)
          }

          test("invalid — ano") {
            assertThrows[TVFs](Validated.asFuture(invalid, TVFs).r)
          }

          test("valid — ano") {
            assert(Validated.asFuture(valid, TVFs).r == 42)
          }
        }

        describe("io asFutureUnsafe()") {
          test("fail") {
            assertThrows[InvalidInputFailure](IO.asFutureUnsafe(IO.fail(ano)).r)
          }

          test("pure") {
            assert(IO.asFutureUnsafe(IO.pure(42)).r == 42)
          }
        }

        describe("task asFutureUnsafe()") {
          test("fail") {
            assertThrows[InvalidInputFailure](Task.asFutureUnsafe(Task.fail(ano))(Scheduler.global).r)
          }

          test("pure") {
            assert(Task.asFutureUnsafe(Task.pure(42))(Scheduler.global).r == 42)
          }

        }

      } //end as{Effect} — reverse

      describe("transformers") {

        describe("bimap") {

          test("fail") {
            val value = Future.bimap(
              failV,
              int2str,
              thr2ano
            )

            assertThrows[ForbiddenFailure](value.r)
          }

          test("pure") {
            val value = Future.bimap(
              pureV,
              int2str,
              thr2ano
            )

            assert(value.r == "42")
          }

        }

        describe("bimap — result") {

          test("fail") {
            val value = Future.bimap(
              failV,
              res2res
            )

            assertThrows[ForbiddenFailure](value.r)
          }

          test("pure") {
            val value = Future.bimap(
              pureV,
              res2res
            )

            assert(value.r == "42")
          }

        }

        describe("bimapThr") {

          test("fail") {
            val value = Future.bimapThr(
              failV,
              int2str,
              thr2thr
            )

            assertThrows[IllegalArgumentException](value.r)
          }

          test("pure") {
            val value = Future.bimapThr(
              pureV,
              int2str,
              thr2thr
            )

            assert(value.r == "42")
          }

        }

        describe("morph") {

          test("fail") {
            val value = Future.morph(
              failV,
              int2str,
              thr2str
            )
            assert(value.r == ano.message)
          }

          test("pure") {
            val value = Future.morph(
              pureV,
              int2str,
              thr2str
            )
            assert(value.r == "42")
          }
        }

        describe("morph — result") {

          test("fail") {
            val value = Future.morph(
              failV,
              res2str
            )
            assert(value.r == ano.message)
          }

          test("pure") {
            val value = Future.morph(
              pureV,
              res2str
            )
            assert(value.r == "42")
          }
        }

        describe("discardContent") {

          test("fail") {
            assertThrows[InvalidInputFailure](Future.discardContent(failV).r)
          }

          test("pure") {
            Future.discardContent(pureV).r
          }
        }

      } //end transformers

    } //end companion object syntax tests

    //===========================================================================
    //===========================================================================
    //===========================================================================

    describe("Future — reference syntax") {

      describe("boolean") {

        describe("cond") {
          test("false") {
            val value = false.condFuture(
              42,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = true.condFuture(
              42,
              ano
            )
            assert(value.r == 42)
          }
        }

        describe("condThr") {
          test("false") {
            val value =
              false.condFutureThr(
                42,
                thr
              )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = true.condFutureThr(
              42,
              thr
            )
            assert(value.r == 42)
          }
        }

        describe("condWith") {
          test("false — pure") {
            val value = false.condWithFuture(
              pureV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true — pure") {
            val value = true.condWithFuture(
              pureV,
              ano
            )
            assert(value.r == 42)
          }

          test("false — fail") {
            val value = false.condWithFuture(
              failV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true — fail") {
            val value = true.condWithFuture(
              failV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }
        }

        describe("condWithThr") {
          test("false — pure") {
            val value = false.condWithFutureThr(
              pureV,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true — pure") {
            val value = true.condWithFutureThr(
              pureV,
              thr
            )
            assert(value.r == 42)
          }

          test("false — fail") {
            val value =
              false.condWithFutureThr(
                failV,
                thr
              )
            assertThrows[RuntimeException](value.r)
          }

          test("true — fail") {
            val value = true.condWithFutureThr(
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
            val value = false.failOnTrueFuture(ano)
            value.r
          }

          test("true") {
            val value = true.failOnTrueFuture(ano)
            assertThrows[InvalidInputFailure](value.r)
          }
        }

        describe("failOnTrueThr") {
          test("false") {
            val value = false.failOnTrueFutureThr(thr)
            value.r
          }

          test("true") {
            val value = true.failOnTrueFutureThr(thr)
            assertThrows[RuntimeException](value.r)
          }
        }

        describe("failOnFalse") {
          test("false") {
            val value = false.failOnFalseFuture(ano)
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = true.failOnFalseFuture(ano)
            value.r
          }
        }

        describe("failOnFalseThr") {
          test("false") {
            val value = false.failOnFalseFutureThr(thr)
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = true.failOnFalseFutureThr(thr)
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
              Future.pure(none).unpack(ano).r
            }
          }

          test("pure — some") {
            assert(Future.pure(some).unpack(ano).r == 42)
          }

          test("fail") {
            assertThrows[RuntimeException] {
              Future.failThr[Option[Int]](thr).unpack(ano).r
            }
          }
        }

        describe("unpackThr") {

          test("pure — none") {
            assertThrows[RuntimeException] {
              Future.pure(none).unpackThr(thr).r
            }
          }

          test("pure — some") {
            assert(Future.pure(some).unpackThr(thr).r == 42)
          }

          test("fail") {
            assertThrows[InvalidInputFailure] {
              Future.fail[Option[Int]](ano).unpackThr(thr).r
            }
          }
        }

        describe("unpack") {
          test("incorrect") {
            assertThrows[InvalidInputFailure](
              Future.pure(incorrect).unpack.r
            )
          }

          test("correct") {
            assert(Future.pure(correct).unpack.r == 42)
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

        describe("option asFuture") {
          test("fail") {
            assertThrows[InvalidInputFailure](none.asFuture(ano).r)
          }

          test("pure") {
            assert(some.asFuture(ano).r == 42)
          }
        }

        describe("option asFutureThr") {
          test("fail") {
            assertThrows[IllegalArgumentException](none.asFutureThr(iae).r)
          }

          test("pure") {
            assert(some.asFutureThr(iae).r == 42)
          }
        }

        describe("try asFuture") {
          test("fail") {
            assertThrows[InvalidInputFailure](failure.asFuture.r)
          }

          test("pure") {
            assert(success.asFuture.r == 42)
          }
        }

        describe("either asFuture — transform") {
          test("fail") {
            assertThrows[ForbiddenFailure](left.asFuture(thr2ano).r)
          }

          test("pure") {
            assert(right.asFuture(thr2ano).r == 42)
          }
        }

        describe("either asFutureThr — transform") {
          test("fail") {
            assertThrows[IllegalArgumentException](left.asFutureThr(thr2thr).r)
          }

          test("pure") {
            assert(right.asFutureThr(thr2thr).r == 42)
          }
        }

        describe("either asFutureThr") {
          test("fail") {
            assertThrows[RuntimeException](left.asFutureThr.r)
          }

          test("pure") {
            assert(right.asFutureThr.r == 42)
          }
        }

        describe("result asFuture") {
          test("fail") {
            assertThrows[InvalidInputFailure](incorrect.asFuture.r)
          }

          test("pure") {
            assert(correct.asFuture.r == 42)
          }
        }

        describe("validated asFuture") {
          test("invalid") {
            assertThrows[GenericValidationFailures](invalid.asFuture.r)
          }

          test("valid") {
            assert(valid.asFuture.r == 42)
          }

          test("invalid — ano") {
            assertThrows[TVFs](invalid.asFuture(TVFs).r)
          }

          test("valid — ano") {
            assert(valid.asFuture(TVFs).r == 42)
          }
        }

        describe("io asFutureUnsafe()") {
          test("fail") {
            assertThrows[InvalidInputFailure](IO.fail(ano).asFutureUnsafe().r)
          }

          test("pure") {
            assert(IO.pure(42).asFutureUnsafe().r == 42)
          }
        }

        describe("task asFutureUnsafe()") {
          test("fail") {
            assertThrows[InvalidInputFailure](Task.fail(ano).asFutureUnsafe()(Scheduler.global).r)
          }

          test("pure") {
            assert(Task.pure(42).asFutureUnsafe()(Scheduler.global).r == 42)
          }

        }

      } //end as{Effect} — reverse

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

        describe("bimap — result") {

          test("fail") {
            val value = failV.bimap(
              res2res
            )

            assertThrows[ForbiddenFailure](value.r)
          }

          test("pure") {
            val value = pureV.bimap(
              res2res
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

        describe("morph — result") {

          test("fail") {
            val value = failV.morph(
              res2str
            )
            assert(value.r == ano.message)
          }

          test("pure") {
            val value = pureV.morph(
              res2str
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
    test("blocking") {
      val f = Future {
        blocking(42)
      }
      assert(f.r == 42)
    }

    describe("Future — companion object syntax") {

      /**
        * Unlike for Task or IO, with Future we cannot test the full suspension
        * of side-effects because Futures start executing immediately.
        * Thus if you use the same trick:
        * {{{
        *   var sideEffects: Int = 0
        *   // set sideEffect withing context
        *   //test to see it didn't execute
        * }}}
        * The sideEffect's mutation depends on timing, not on us calling "unsafeSyncGet()",
        * so to avoid flaky tests that depend on timing, we test if the failure is captured
        * within the Future, and not thrown into our face
        */
      describe("suspend") {

        test("suspendOption") {
          val f = Future.suspendOption(
            Option(throw thr),
            ano
          )
          assertThrows[RuntimeException](f.r)

        }

        test("suspendOptionThr") {
          val f = Future.suspendOptionThr(
            Option(throw thr),
            iae
          )
          assertThrows[RuntimeException](f.r)

        }

        test("suspendTry") {
          val f = Future.suspendTry(
            Try.pure(throw thr)
          )
          assertThrows[RuntimeException](f.r)
        }

        test("suspendEither") {
          val f = Future.suspendEither(
            Right[Throwable, String](throw thr),
            thr2ano
          )
          assertThrows[RuntimeException](f.r)
        }

        test("suspendEitherThr") {
          val f = Future.suspendEitherThr(
            Right[Throwable, String](throw thr)
          )
          assertThrows[RuntimeException](f.r)
        }

        test("suspendEitherThr — transform") {
          val f = Future.suspendEitherThr(
            Right[Throwable, String](throw thr),
            thr2thr
          )
          assertThrows[RuntimeException](f.r)
        }

        test("suspendResult") {
          val f = Future.suspendResult(
            Result.pure(throw thr)
          )
          assertThrows[RuntimeException](f.r)
        }

        describe("suspendValidated") {
          test("normal") {
            val f = Future.suspendValidated(
              Validated.pure(throw thr)
            )
            assertThrows[RuntimeException](f.r)
          }

          test("ano") {
            val f = Future.suspendValidated(
              Validated.pure(throw thr),
              TVFs
            )
            assertThrows[RuntimeException](f.r)
          }
        }

      } //end suspend

      describe("effect on boolean") {

        describe("effectOnFalse") {

          test("false") {
            var sideEffect: Int = 0
            val f = Future.effectOnFalse(
              false,
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            assert(sideEffect == 42)

          }

          test("true") {
            var sideEffect: Int = 0
            val f = Future.effectOnFalse(
              true,
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

        }

        describe("effectOnTrue") {

          test("false") {
            var sideEffect: Int = 0
            val f = Future.effectOnTrue(
              false,
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("true") {
            var sideEffect: Int = 0
            val f = Future.effectOnTrue(
              true,
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            assert(sideEffect == 42)

          }
        }

        describe("flatEffectOnFalse") {

          test("false") {
            var sideEffect: Int = 0
            val f = Future.flatEffectOnFalse(
              bfalse,
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            assert(sideEffect == 42)

          }

          test("true") {
            var sideEffect: Int = 0
            val f = Future.flatEffectOnFalse(
              btrue,
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = Future.flatEffectOnFalse(
              bfail,
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            assertThrows[IllegalArgumentException](f.r)
            assert(sideEffect == 0, "side effect should not have applied on fail")

          }

        }

        describe("flatEffectOnTrue") {

          test("false") {
            var sideEffect: Int = 0
            val f = Future.flatEffectOnTrue(
              bfalse,
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("true") {
            var sideEffect: Int = 0
            val f = Future.flatEffectOnTrue(
              btrue,
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            assert(sideEffect == 42)

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = Future.flatEffectOnTrue(
              bfail,
              Future {
                sideEffect = 42
                sideEffect
              }
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
            val f = Future.effectOnFail(
              none,
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            assert(sideEffect == 42)

          }

          test("some") {
            var sideEffect: Int = 0
            val f = Future.effectOnFail(
              some,
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }
        }

        describe("effectOnPure") {

          test("none") {
            var sideEffect: Int = 0
            val f = Future.effectOnPure(
              none,
              (x: Int) =>
                Future {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("some") {
            var sideEffect: Int = 0
            val f = Future.effectOnPure(
              some,
              (x: Int) =>
                Future {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            assert(sideEffect == 42)

          }
        }

        describe("flatEffectOnNone") {

          test("none") {
            var sideEffect: Int = 0
            val f = Future.flatEffectOnNone(
              Future.pure(none),
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            assert(sideEffect == 42)
          }

          test("some") {
            var sideEffect: Int = 0
            val f = Future.flatEffectOnNone(
              Future.pure(some),
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = Future.flatEffectOnNone(
              Future.fail[Option[Int]](ano),
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            assertThrows[InvalidInputFailure](f.r)
            assert(sideEffect == 0, "side effect should not have applied on fail")

          }

        }

        describe("flatEffectOnSome") {

          test("none") {
            var sideEffect: Int = 0
            val f = Future.flatEffectOnSome(
              Future.pure(none),
              (x: Int) =>
                Future {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")
          }

          test("some") {
            var sideEffect: Int = 0
            val f = Future.flatEffectOnSome(
              Future.pure(some),
              (x: Int) =>
                Future {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            assert(sideEffect == 42)

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = Future.flatEffectOnSome(
              Future.fail[Option[Int]](ano),
              (x: Int) =>
                Future {
                  sideEffect = x
                  sideEffect
              }
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
            val f = Future.effectOnFail(
              incorrect,
              (a: Anomaly) =>
                Future {
                  sideEffect = 42
                  sideEffect
              }
            )
            f.r
            assert(sideEffect == 42)

          }

          test("correct") {
            var sideEffect: Int = 0
            val f = Future.effectOnFail(
              correct,
              (a: Anomaly) =>
                Future {
                  sideEffect = 42
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }
        }

        describe("effectOnPure") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f = Future.effectOnPure(
              incorrect,
              (x: Int) =>
                Future {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("correct") {
            var sideEffect: Int = 0
            val f = Future.effectOnPure(
              correct,
              (x: Int) =>
                Future {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            assert(sideEffect == 42)

          }
        }

        describe("flatEffectOnIncorrect") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f = Future.flatEffectOnIncorrect(
              Future.pure(incorrect),
              (a: Anomaly) =>
                Future {
                  sideEffect = 42
                  sideEffect
              }
            )
            f.r
            assert(sideEffect == 42)
          }

          test("correct") {
            var sideEffect: Int = 0
            val f = Future.flatEffectOnIncorrect(
              Future.pure(correct),
              (a: Anomaly) =>
                Future {
                  sideEffect = 42
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = Future.flatEffectOnIncorrect(
              Future.fail[Result[Int]](ano),
              (a: Anomaly) =>
                Future {
                  sideEffect = 42
                  sideEffect
              }
            )
            assertThrows[InvalidInputFailure](f.r)
            assert(sideEffect == 0, "side effect should not have applied on fail")

          }

        }

        describe("flatEffectOnCorrect") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f = Future.flatEffectOnCorrect(
              Future.pure(incorrect),
              (x: Int) =>
                Future {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")
          }

          test("correct") {
            var sideEffect: Int = 0
            val f = Future.flatEffectOnCorrect(
              Future.pure(correct),
              (x: Int) =>
                Future {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            assert(sideEffect == 42)

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = Future.flatEffectOnCorrect(
              Future.fail[Result[Int]](ano),
              (x: Int) =>
                Future {
                  sideEffect = x
                  sideEffect
              }
            )
            assertThrows[InvalidInputFailure](f.r)
            assert(sideEffect == 0, "side effect should not have applied on fail")

          }

        }

      }

    }

    describe("Future — other effect reference syntax") {

      /**
        * Unlike for Task or IO, with Future we cannot test the full suspension
        * of side-effects because Futures start executing immediately.
        * Thus if you use the same trick:
        * {{{
        *   var sideEffects: Int = 0
        *   // set sideEffect withing context
        *   //test to see it didn't execute
        * }}}
        * The sideEffect's mutation depends on timing, not on us calling "unsafeSyncGet()",
        * so to avoid flaky tests that depend on timing, we test if the failure is captured
        * within the Future, and not thrown into our face
        */
      describe("suspendInFuture") {

        describe("reference") {

          test("suspendOption") {
            val f = Option(throw thr).suspendInFuture(ano)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendOptionThr") {
            val f = Option(throw thr).suspendInFutureThr(thr)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendTry") {
            val f = Try.pure(throw thr).suspendInFuture
            assertThrows[RuntimeException](f.r)
          }

          test("suspendEither") {
            val f = Right[Throwable, String](throw thr).suspendInFuture(thr2ano)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendEitherThr") {
            val f = Right[Throwable, String](throw thr).suspendInFutureThr
            assertThrows[RuntimeException](f.r)
          }

          test("suspendEitherThr — transform") {
            val f = Right[Throwable, String](throw thr).suspendInFutureThr(thr2thr)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendResult") {
            val f = Result.pure(throw thr).suspendInFuture
            assertThrows[RuntimeException](f.r)
          }

          describe("suspendValidated") {
            test("normal") {
              val f = Validated.pure(throw thr).suspendInFuture
              assertThrows[RuntimeException](f.r)
            }

            test("ano") {
              val f = Validated.pure(throw thr).suspendInFuture(TVFs)
              assertThrows[RuntimeException](f.r)
            }
          }
        }

        describe("companion") {

          test("suspendOption") {
            val f = Option.suspendInFuture(Option(throw thr), ano)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendOptionThr") {
            val f = Option.suspendInFutureThr(Option(throw thr), thr)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendTry") {
            val f = Try.suspendInFuture(Try.pure(throw thr))
            assertThrows[RuntimeException](f.r)
          }

          test("suspendEither") {
            val f = Either.suspendInFuture(Right[Throwable, String](throw thr), thr2ano)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendEitherThr") {
            val f = Either.suspendInFutureThr(Right[Throwable, String](throw thr))
            assertThrows[RuntimeException](f.r)
          }

          test("suspendEitherThr — transform") {
            val f = Either.suspendInFutureThr(Right[Throwable, String](throw thr), thr2thr)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendResult") {
            val f = Result.suspendInFuture(Result.pure(throw thr))
            assertThrows[RuntimeException](f.r)
          }

          describe("suspendValidated") {
            test("normal") {
              val f = Validated.suspendInFuture(Validated.pure(throw thr))
              assertThrows[RuntimeException](f.r)
            }

            test("ano") {
              val f = Validated.suspendInFuture(Validated.pure(throw thr), TVFs)
              assertThrows[RuntimeException](f.r)
            }
          }

        }

      } //end suspend

      describe("effect on boolean") {

        describe("effectOnFalse") {

          test("false") {
            var sideEffect: Int = 0
            val f = false.effectOnFalseFuture(
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            assert(sideEffect == 42)

          }

          test("true") {
            var sideEffect: Int = 0
            val f = true.effectOnFalseFuture(
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

        }

        describe("effectOnTrue") {

          test("false") {
            var sideEffect: Int = 0
            val f = false.effectOnTrueFuture(
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("true") {
            var sideEffect: Int = 0
            val f = true.effectOnTrueFuture(
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            assert(sideEffect == 42)

          }
        }

        describe("flatEffectOnFalse") {

          test("false") {
            var sideEffect: Int = 0
            val f = bfalse.effectOnFalse(
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            assert(sideEffect == 42)

          }

          test("true") {
            var sideEffect: Int = 0
            val f = btrue.effectOnFalse(
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = bfail.effectOnFalse(
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            assertThrows[IllegalArgumentException](f.r)
            assert(sideEffect == 0, "side effect should not have applied on fail")

          }

        }

        describe("flatEffectOnTrue") {

          test("false") {
            var sideEffect: Int = 0
            val f = bfalse.effectOnTrue(
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("true") {
            var sideEffect: Int = 0
            val f = btrue.effectOnTrue(
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            assert(sideEffect == 42)

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = bfail.effectOnTrue(
              Future {
                sideEffect = 42
                sideEffect
              }
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
            val f = none.effectOnFailFuture(
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            assert(sideEffect == 42)

          }

          test("some") {
            var sideEffect: Int = 0
            val f = some.effectOnFailFuture(
              Future {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }
        }

        describe("effectOnPure") {

          test("none") {
            var sideEffect: Int = 0
            val f = none.effectOnPureFuture(
              (x: Int) =>
                Future {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("some") {
            var sideEffect: Int = 0
            val f = some.effectOnPureFuture(
              (x: Int) =>
                Future {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            assert(sideEffect == 42)

          }
        }

        describe("flatEffectOnNone") {

          test("none") {
            var sideEffect: Int = 0
            val f =
              Future
                .pure(none)
                .effectOnFail(
                  Future {
                    sideEffect = 42
                    sideEffect
                  }
                )
            f.r
            assert(sideEffect == 42)
          }

          test("some") {
            var sideEffect: Int = 0
            val f =
              Future
                .pure(some)
                .effectOnFail(
                  Future {
                    sideEffect = 42
                    sideEffect
                  }
                )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("fail") {
            var sideEffect: Int = 0
            val f =
              Future
                .fail[Option[Int]](ano)
                .effectOnFail(
                  Future {
                    sideEffect = 42
                    sideEffect
                  }
                )
            assertThrows[InvalidInputFailure](f.r)
            assert(sideEffect == 0, "side effect should not have applied on fail")

          }

        }

        describe("flatEffectOnSome") {

          test("none") {
            var sideEffect: Int = 0
            val f =
              Future
                .pure(none)
                .effectOnPure(
                  (x: Int) =>
                    Future {
                      sideEffect = x
                      sideEffect
                  }
                )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")
          }

          test("some") {
            var sideEffect: Int = 0
            val f =
              Future
                .pure(some)
                .effectOnPure(
                  (x: Int) =>
                    Future {
                      sideEffect = x
                      sideEffect
                  }
                )
            f.r
            assert(sideEffect == 42)

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = Future
              .fail[Option[Int]](ano)
              .effectOnPure(
                (x: Int) =>
                  Future {
                    sideEffect = x
                    sideEffect
                }
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
            val f = incorrect.effectOnFailFuture(
              (a: Anomaly) =>
                Future {
                  sideEffect = 42
                  sideEffect
              }
            )
            f.r
            assert(sideEffect == 42)

          }

          test("correct") {
            var sideEffect: Int = 0
            val f = correct.effectOnFailFuture(
              (a: Anomaly) =>
                Future {
                  sideEffect = 42
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }
        }

        describe("effectOnPure") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f = incorrect.effectOnPureFuture(
              (x: Int) =>
                Future {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("correct") {
            var sideEffect: Int = 0
            val f = correct.effectOnPureFuture(
              (x: Int) =>
                Future {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            assert(sideEffect == 42)

          }
        }

        describe("flatEffectOnIncorrect") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f =
              Future
                .pure(incorrect)
                .effectOnFail(
                  (a: Anomaly) =>
                    Future {
                      sideEffect = 42
                      sideEffect
                  }
                )
            f.r
            assert(sideEffect == 42)
          }

          test("correct") {
            var sideEffect: Int = 0
            val f = Future
              .pure(correct)
              .effectOnFail(
                (a: Anomaly) =>
                  Future {
                    sideEffect = 42
                    sideEffect
                }
              )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = Future
              .fail[Result[Int]](ano)
              .effectOnFail(
                (a: Anomaly) =>
                  Future {
                    sideEffect = 42
                    sideEffect
                }
              )
            assertThrows[InvalidInputFailure](f.r)
            assert(sideEffect == 0, "side effect should not have applied on fail")

          }

        }

        describe("flatEffectOnCorrect") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f =
              Future
                .pure(incorrect)
                .effectOnPure(
                  (x: Int) =>
                    Future {
                      sideEffect = x
                      sideEffect
                  }
                )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")
          }

          test("correct") {
            var sideEffect: Int = 0
            val f =
              Future
                .pure(correct)
                .effectOnPure(
                  (x: Int) =>
                    Future {
                      sideEffect = x
                      sideEffect
                  }
                )
            f.r
            assert(sideEffect == 42)

          }

          test("fail") {
            var sideEffect: Int = 0
            val f =
              Future
                .fail[Result[Int]](ano)
                .effectOnPure(
                  (x: Int) =>
                    Future {
                      sideEffect = x
                      sideEffect
                  }
                )
            assertThrows[InvalidInputFailure](f.r)
            assert(sideEffect == 0, "side effect should not have applied on fail")

          }

        }

      }
    }

    describe("traversals") {

      describe("Future.serialize") {

        test("empty list") {
          val input:    Seq[Int] = List()
          val expected: Seq[Int] = List()

          var sideEffect: Int = 0

          val eventualResult = Future.serialize(input) { i =>
            Future {
              sideEffect = 42
            }
          }

          assert(eventualResult.r == expected)
          assert(sideEffect == 0, "nothing should have happened")
        }

        test("no two futures should run in parallel") {
          val input: Seq[Int] = (1 to 100).toList
          val expected = input.map(_.toString)

          var previouslyProcessed: Option[Int] = None
          var startedFlag:         Option[Int] = None

          val eventualResult: Future[Seq[String]] = Future.serialize(input) { i =>
            Future {
              assert(
                startedFlag.isEmpty,
                s"started flag should have been empty at the start of each future but was: $startedFlag"
              )
              previouslyProcessed foreach { previous =>
                assertResult(expected = i - 1, "... the futures were not executed in the correct order.")(
                  actual = previous
                )
              }
              startedFlag         = Some(i)
              startedFlag         = None
              previouslyProcessed = Some(i)
              i.toString
            }
          }
          assert(expected == eventualResult.r)
          assert(previouslyProcessed == Option(100))
        }
      }

      describe("Future.serialize_") {

        test("empty list") {
          val input: Seq[Int] = List()

          var sideEffect: Int = 0

          val eventualResult = Future.serialize_(input) { i =>
            Future {
              sideEffect = 42
            }
          }

          eventualResult.r
          assert(sideEffect == 0, "nothing should have happened")
        }

        test("no two futures should run in parallel") {
          val input: Seq[Int] = (1 to 100).toList

          var previouslyProcessed: Option[Int] = None
          var startedFlag:         Option[Int] = None

          val eventualResult: Future[Unit] = Future.serialize_(input) { i =>
            Future {
              assert(
                startedFlag.isEmpty,
                s"started flag should have been empty at the start of each future but was: $startedFlag"
              )
              previouslyProcessed foreach { previous =>
                assertResult(expected = i - 1, "... the futures were not executed in the correct order.")(
                  actual = previous
                )
              }
              startedFlag         = Some(i)
              startedFlag         = None
              previouslyProcessed = Some(i)
              i.toString
            }
          }
          eventualResult.r
          assert(previouslyProcessed == Option(100))
        }
      }

      describe("Future.traverse_") {

        test("empty list") {
          val input: Seq[Int] = List()

          var sideEffect: Int = 0

          val eventualResult: Future[Unit] = Future.traverse_(input) { i =>
            Future {
              sideEffect = 42
            }
          }

          eventualResult.r
          assert(sideEffect == 0, "nothing should have happened")
        }
      }

      describe("Future.sequence_") {

        test("empty list") {
          val input: Seq[Int] = List()

          var sideEffect: Int = 0

          val eventualResult: Future[Unit] = Future.sequence_ {
            input.map { i =>
              Future {
                sideEffect = 42
              }
            }
          }

          eventualResult.r
          assert(sideEffect == 0, "nothing should have happened")
        }
      }
    }

  }

} //end test
