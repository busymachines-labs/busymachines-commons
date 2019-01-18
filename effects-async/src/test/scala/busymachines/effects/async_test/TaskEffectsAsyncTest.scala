package busymachines.effects.async_test

import busymachines.core._
import busymachines.effects.async._
import busymachines.effects.sync._

import busymachines.effects.sync.validated._
import busymachines.effects.async.validated._

import org.scalatest._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Jan 2018
  *
  */
final class TaskEffectsAsyncTest extends FunSpec {
  implicit val ec: Scheduler = Scheduler.global
  //prevents atrocious English
  private def test: ItWord = it

  implicit private class TestSyntax[T](value: Task[T]) {
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

  private val failV: Task[Int] = Task.fail(ano)
  private val pureV: Task[Int] = Task.pure(42)

  private val btrue:  Task[Boolean] = Task.pure(true)
  private val bfalse: Task[Boolean] = Task.pure(false)
  private val bfail:  Task[Boolean] = Task.failThr(iae)

  //---------------------------------------------------------------------------

  describe("sync + pure") {

    describe("Task — companion object syntax") {

      describe("constructors") {
        test("pure") {
          assert(Task.pure(42).unsafeSyncGet() == 42)
        }

        test("fail") {
          assertThrows[InvalidInputFailure](Task.fail(ano).r)
          assertThrows[RuntimeException](Task.failThr(thr).r)
        }

        test("unit") {
          assert(Task.unit == Task.unit)
        }

        describe("fromOption") {
          test("none") {
            assertThrows[InvalidInputFailure](Task.fromOption(none, ano).r)
          }

          test("some") {
            assert(Task.fromOption(some, ano).r == 42)
          }
        }

        describe("fromOptionThr") {
          test("none") {
            assertThrows[RuntimeException](Task.fromOptionThr(none, thr).r)
          }

          test("some") {
            assert(Task.fromOptionThr(some, thr).r == 42)
          }
        }

        describe("fromTry") {

          test("failure") {
            assertThrows[InvalidInputFailure](Task.fromTry(failure).r)
          }

          test("success") {
            assert(Task.fromTry(success).r == 42)
          }
        }

        describe("fromEither") {
          test("left") {
            assertThrows[RuntimeException](Task.fromEitherThr(left).r)
          }

          test("left — transform") {
            assertThrows[ForbiddenFailure](Task.fromEither(left, thr2ano).r)
          }

          test("right") {
            assert(Task.fromEitherThr(right).r == 42)
          }

          test("right — transform") {
            assert(Task.fromEither(right, thr2ano).r == 42)
          }
        }

        describe("fromEitherThr") {
          test("left — transform") {
            assertThrows[IllegalArgumentException](Task.fromEitherThr(left, (_: Throwable) => iae).r)
          }

          test("right") {
            assert(Task.fromEitherThr(right, (_: Throwable) => iae).r == 42)
          }
        }

        describe("fromResult") {
          test("incorrect") {
            assertThrows[InvalidInputFailure](Task.fromResult(incorrect).r)
          }

          test("correct") {
            assert(Task.fromResult(correct).r == 42)
          }
        }

        describe("fromValidated") {
          test("invalid") {
            assertThrows[GenericValidationFailures](Task.fromValidated(invalid).r)
          }

          test("valid") {
            assert(Task.fromValidated(valid).r == 42)
          }

          test("invalid — ano") {
            assertThrows[TVFs](Task.fromValidated(invalid, TVFs).r)
          }

          test("valid — ano") {
            assert(Task.fromValidated(valid, TVFs).r == 42)
          }

        }

        describe("fromFuturePure") {
          test("failed") {
            assertThrows[InvalidInputFailure](Task.fromFuturePure(failedF).r)
          }

          test("success") {
            assert(Task.fromFuturePure(successF).r == 42)
          }
        }

        describe("fromIO") {
          test("fail") {
            assertThrows[InvalidInputFailure](Task.fromIO(IO.fail(ano)).r)
          }

          test("pure") {
            assert(Task.fromIO(IO.pure(42)).r == 42)
          }
        }

      } //end constructors

      describe("boolean") {

        describe("cond") {
          test("false") {
            val value = Task.cond(
              false,
              42,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = Task.cond(
              true,
              42,
              ano
            )
            assert(value.r == 42)
          }
        }

        describe("condThr") {
          test("false") {
            val value = Task.condThr(
              false,
              42,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = Task.condThr(
              true,
              42,
              thr
            )
            assert(value.r == 42)
          }
        }

        describe("condWith") {
          test("false — pure") {
            val value = Task.condWith(
              false,
              pureV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true — pure") {
            val value = Task.condWith(
              true,
              pureV,
              ano
            )
            assert(value.r == 42)
          }

          test("false — fail") {
            val value = Task.condWith(
              false,
              failV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true — fail") {
            val value = Task.condWith(
              true,
              failV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }
        }

        describe("condWithThr") {
          test("false — pure") {
            val value = Task.condWithThr(
              false,
              pureV,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true — pure") {
            val value = Task.condWithThr(
              true,
              pureV,
              thr
            )
            assert(value.r == 42)
          }

          test("false — fail") {
            val value = Task.condWithThr(
              false,
              failV,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true — fail") {
            val value = Task.condWithThr(
              true,
              failV,
              thr
            )
            assertThrows[InvalidInputFailure](value.r)
          }
        }

        describe("flatCond") {
          test("false") {
            val value = Task.flatCond(
              bfalse,
              42,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = Task.flatCond(
              btrue,
              42,
              ano
            )
            assert(value.r == 42)
          }

          test("fail") {
            val value = Task.flatCond(
              bfail,
              42,
              ano
            )
            assertThrows[IllegalArgumentException](value.r)
          }
        }

        describe("flatCondThr") {
          test("false") {
            val value = Task.flatCondThr(
              bfalse,
              42,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = Task.flatCondThr(
              btrue,
              42,
              thr
            )
            assert(value.r == 42)
          }

          test("fail") {
            val value = Task.flatCondThr(
              bfail,
              42,
              thr
            )
            assertThrows[IllegalArgumentException](value.r)
          }
        }

        describe("flatCondWith") {
          test("false — pure") {
            val value = Task.flatCondWith(
              bfalse,
              pureV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("false — fail") {
            val value = Task.flatCondWith(
              bfalse,
              failV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true — pure") {
            val value = Task.flatCondWith(
              btrue,
              pureV,
              ano
            )
            assert(value.r == 42)
          }

          test("true — fail") {
            val value = Task.flatCondWith(
              btrue,
              failV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("fail — pure") {
            val value = Task.flatCondWith(
              bfail,
              pureV,
              ano
            )
            assertThrows[IllegalArgumentException](value.r)
          }

          test("fail — fail") {
            val value = Task.flatCondWith(
              bfail,
              failV,
              ano
            )
            assertThrows[IllegalArgumentException](value.r)
          }
        }

        describe("flatCondWithThr") {
          test("false — pure") {
            val value = Task.flatCondWithThr(
              bfalse,
              pureV,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("false — fail") {
            val value = Task.flatCondWithThr(
              bfalse,
              failV,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true — pure") {
            val value = Task.flatCondWithThr(
              btrue,
              pureV,
              thr
            )
            assert(value.r == 42)
          }

          test("true — fail") {
            val value = Task.flatCondWithThr(
              btrue,
              failV,
              thr
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("fail — pure") {
            val value = Task.flatCondWithThr(
              bfail,
              pureV,
              thr
            )
            assertThrows[IllegalArgumentException](value.r)
          }

          test("fail — fail") {
            val value = Task.flatCondWithThr(
              bfail,
              failV,
              thr
            )
            assertThrows[IllegalArgumentException](value.r)
          }
        }

        describe("failOnTrue") {
          test("false") {
            val value = Task.failOnTrue(
              false,
              ano
            )
            value.r
          }

          test("true") {
            val value = Task.failOnTrue(
              true,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }
        }

        describe("failOnTrueThr") {
          test("false") {
            val value = Task.failOnTrueThr(
              false,
              thr
            )
            value.r
          }

          test("true") {
            val value = Task.failOnTrueThr(
              true,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }
        }

        describe("failOnFalse") {
          test("false") {
            val value = Task.failOnFalse(
              false,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = Task.failOnFalse(
              true,
              ano
            )
            value.r
          }
        }

        describe("failOnFalseThr") {
          test("false") {
            val value = Task.failOnFalseThr(
              false,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = Task.failOnFalseThr(
              true,
              thr
            )
            value.r
          }
        }

        describe("flatFailOnTrue") {
          test("false") {
            val value = Task.flatFailOnTrue(
              bfalse,
              ano
            )
            value.r
          }

          test("true") {
            val value = Task.flatFailOnTrue(
              btrue,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("fail") {
            val value = Task.flatFailOnTrue(
              bfail,
              ano
            )
            assertThrows[IllegalArgumentException](value.r)
          }

        }

        describe("flatFailOnTrueThr") {
          test("false") {
            val value = Task.flatFailOnTrueThr(
              bfalse,
              thr
            )
            value.r
          }

          test("true") {
            val value = Task.flatFailOnTrueThr(
              btrue,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("fail") {
            val value = Task.flatFailOnTrueThr(
              bfail,
              thr
            )
            assertThrows[IllegalArgumentException](value.r)
          }

        }

        describe("flatFailOnFalse") {
          test("false") {
            val value = Task.flatFailOnFalse(
              bfalse,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = Task.flatFailOnFalse(
              btrue,
              ano
            )
            value.r
          }

          test("fail") {
            val value = Task.flatFailOnFalse(
              bfail,
              ano
            )
            assertThrows[IllegalArgumentException](value.r)
          }

        }

        describe("flatFailOnFalseThr") {
          test("false") {
            val value = Task.flatFailOnFalseThr(
              bfalse,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = Task.flatFailOnFalseThr(
              btrue,
              thr
            )
            value.r
          }

          test("fail") {
            val value = Task.flatFailOnFalseThr(
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
              Task.unpackOption(Task.pure(none), ano).r
            }
          }

          test("pure — some") {
            assert(Task.unpackOption(Task.pure(some), ano).r == 42)
          }

          test("fail") {
            assertThrows[RuntimeException] {
              Task.unpackOption(Task.failThr[Option[Int]](thr), ano).r
            }
          }
        }

        describe("unpackThr") {

          test("pure — none") {
            assertThrows[RuntimeException] {
              Task.unpackOptionThr(Task.pure(none), thr).r
            }
          }

          test("pure — some") {
            assert(Task.unpackOptionThr(Task.pure(some), thr).r == 42)
          }

          test("fail") {
            assertThrows[InvalidInputFailure] {
              Task.unpackOptionThr(Task.fail[Option[Int]](ano), thr).r
            }
          }
        }

        describe("unpack") {
          test("incorrect") {
            assertThrows[InvalidInputFailure](
              Task.unpackResult(Task.pure(incorrect)).r
            )
          }

          test("correct") {
            assert(Task.unpackResult(Task.pure(correct)).r == 42)
          }
        }

      } //end nested

      describe("as{Effect}") {

        describe("attemptResult") {
          test("fail") {
            assert(Task.attemptResult(failV).r == incorrect)
          }

          test("pure") {
            assert(Task.attemptResult(pureV).r == correct)
          }

        }

        describe("unsafeGet") {

          test("fail") {
            assertThrows[InvalidInputFailure](Task.unsafeSyncGet(failV))
          }

          test("pure") {
            assert(Task.unsafeSyncGet(pureV) == 42)
          }

        }

      } //end as{Effect}

      describe("as{Effect} — reverse") {

        describe("option asTask") {
          test("fail") {
            assertThrows[InvalidInputFailure](Option.asTask(none, ano).r)
          }

          test("pure") {
            assert(Option.asTask(some, ano).r == 42)
          }
        }

        describe("option asTaskThr") {
          test("fail") {
            assertThrows[IllegalArgumentException](Option.asTaskThr(none, iae).r)
          }

          test("pure") {
            assert(Option.asTaskThr(some, iae).r == 42)
          }
        }

        describe("try asTask") {
          test("fail") {
            assertThrows[InvalidInputFailure](Try.asTask(failure).r)
          }

          test("pure") {
            assert(Try.asTask(success).r == 42)
          }
        }

        describe("either asTask") {
          test("fail") {
            assertThrows[ForbiddenFailure](Either.asTask(left, thr2ano).r)
          }

          test("pure") {
            assert(Either.asTask(right, thr2ano).r == 42)
          }
        }

        describe("either asTaskThr — transform") {
          test("fail") {
            assertThrows[RuntimeException](Either.asTaskThr(left, thr2thr).r)
          }

          test("pure") {
            assert(Either.asTaskThr(right, thr2thr).r == 42)
          }
        }

        describe("either asTaskThr") {
          test("fail") {
            assertThrows[RuntimeException](Either.asTaskThr(left).r)
          }

          test("pure") {
            assert(Either.asTaskThr(right).r == 42)
          }
        }

        describe("result asTask") {
          test("fail") {
            assertThrows[InvalidInputFailure](Result.asTask(incorrect).r)
          }

          test("pure") {
            assert(Result.asTask(correct).r == 42)
          }
        }

        describe("validated asFuture") {
          test("invalid") {
            assertThrows[GenericValidationFailures](Validated.asTask(invalid).r)
          }

          test("valid") {
            assert(Validated.asTask(valid).r == 42)
          }

          test("invalid — ano") {
            assertThrows[TVFs](Validated.asTask(invalid, TVFs).r)
          }

          test("valid — ano") {
            assert(Validated.asTask(valid, TVFs).r == 42)
          }
        }

        describe("future as Task") {
          test("fail") {
            assertThrows[InvalidInputFailure](Future.asTask(failedF).r)
          }

          test("pure") {
            assert(Future.asTask(successF).r == 42)
          }
        }

        describe("io as Task") {
          test("fail") {
            assertThrows[InvalidInputFailure](IO.asTask(IO.fail(ano)).r)
          }

          test("pure") {
            assert(IO.asTask(IO.pure(42)).r == 42)
          }

        }

      } //end as{Effect} reverse

      describe("transformers") {

        describe("bimap") {

          test("fail") {
            val value = Task.bimap(
              failV,
              int2str,
              thr2ano
            )

            assertThrows[ForbiddenFailure](value.r)
          }

          test("pure") {
            val value = Task.bimap(
              pureV,
              int2str,
              thr2ano
            )

            assert(value.r == "42")
          }

        }

        describe("bimap — result") {

          test("fail") {
            val value = Task.bimap(
              failV,
              res2res
            )

            assertThrows[ForbiddenFailure](value.r)
          }

          test("pure") {
            val value = Task.bimap(
              pureV,
              res2res
            )

            assert(value.r == "42")
          }

        }

        describe("bimapThr") {

          test("fail") {
            val value = Task.bimapThr(
              failV,
              int2str,
              thr2thr
            )

            assertThrows[IllegalArgumentException](value.r)
          }

          test("pure") {
            val value = Task.bimapThr(
              pureV,
              int2str,
              thr2thr
            )

            assert(value.r == "42")
          }

        }

        describe("morph") {

          test("fail") {
            val value = Task.morph(
              failV,
              int2str,
              thr2str
            )
            assert(value.r == ano.message)
          }

          test("pure") {
            val value = Task.morph(
              pureV,
              int2str,
              thr2str
            )
            assert(value.r == "42")
          }
        }

        describe("morph — result") {

          test("fail") {
            val value = Task.morph(
              failV,
              res2str
            )
            assert(value.r == ano.message)
          }

          test("pure") {
            val value = Task.morph(
              pureV,
              res2str
            )
            assert(value.r == "42")
          }
        }

        describe("discardContent") {

          test("fail") {
            assertThrows[InvalidInputFailure](Task.discardContent(failV).r)
          }

          test("pure") {
            Task.discardContent(pureV).r
          }
        }

      } //end transformers

    } //end companion object syntax tests

    //===========================================================================
    //===========================================================================
    //===========================================================================

    describe("Task — reference syntax") {

      describe("boolean") {

        describe("cond") {
          test("false") {
            val value = false.condTask(
              42,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = true.condTask(
              42,
              ano
            )
            assert(value.r == 42)
          }
        }

        describe("condThr") {
          test("false") {
            val value =
              false.condTaskThr(
                42,
                thr
              )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = true.condTaskThr(
              42,
              thr
            )
            assert(value.r == 42)
          }
        }

        describe("condWith") {
          test("false — pure") {
            val value = false.condWithTask(
              pureV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true — pure") {
            val value = true.condWithTask(
              pureV,
              ano
            )
            assert(value.r == 42)
          }

          test("false — fail") {
            val value = false.condWithTask(
              failV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true — fail") {
            val value = true.condWithTask(
              failV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }
        }

        describe("condWithThr") {
          test("false — pure") {
            val value = false.condWithTaskThr(
              pureV,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true — pure") {
            val value = true.condWithTaskThr(
              pureV,
              thr
            )
            assert(value.r == 42)
          }

          test("false — fail") {
            val value =
              false.condWithTaskThr(
                failV,
                thr
              )
            assertThrows[RuntimeException](value.r)
          }

          test("true — fail") {
            val value = true.condWithTaskThr(
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
            val value = false.failOnTrueTask(ano)
            value.r
          }

          test("true") {
            val value = true.failOnTrueTask(ano)
            assertThrows[InvalidInputFailure](value.r)
          }
        }

        describe("failOnTrueThr") {
          test("false") {
            val value = false.failOnTrueTaskThr(thr)
            value.r
          }

          test("true") {
            val value = true.failOnTrueTaskThr(thr)
            assertThrows[RuntimeException](value.r)
          }
        }

        describe("failOnFalse") {
          test("false") {
            val value = false.failOnFalseTask(ano)
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = true.failOnFalseTask(ano)
            value.r
          }
        }

        describe("failOnFalseThr") {
          test("false") {
            val value = false.failOnFalseTaskThr(thr)
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = true.failOnFalseTaskThr(thr)
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
              Task.pure(none).unpack(ano).r
            }
          }

          test("pure — some") {
            assert(Task.pure(some).unpack(ano).r == 42)
          }

          test("fail") {
            assertThrows[RuntimeException] {
              Task.failThr[Option[Int]](thr).unpack(ano).r
            }
          }
        }

        describe("unpackThr") {

          test("pure — none") {
            assertThrows[RuntimeException] {
              Task.pure(none).unpackThr(thr).r
            }
          }

          test("pure — some") {
            assert(Task.pure(some).unpackThr(thr).r == 42)
          }

          test("fail") {
            assertThrows[InvalidInputFailure] {
              Task.fail[Option[Int]](ano).unpackThr(thr).r
            }
          }
        }

        describe("unpack") {
          test("incorrect") {
            assertThrows[InvalidInputFailure](
              Task.pure(incorrect).unpack.r
            )
          }

          test("correct") {
            assert(Task.pure(correct).unpack.r == 42)
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

        describe("option asTask") {
          test("fail") {
            assertThrows[InvalidInputFailure](none.asTask(ano).r)
          }

          test("pure") {
            assert(some.asTask(ano).r == 42)
          }
        }

        describe("option asTaskThr") {
          test("fail") {
            assertThrows[IllegalArgumentException](none.asTaskThr(iae).r)
          }

          test("pure") {
            assert(some.asTaskThr(iae).r == 42)
          }
        }

        describe("try asTask") {
          test("fail") {
            assertThrows[InvalidInputFailure](failure.asTask.r)
          }

          test("pure") {
            assert(success.asTask.r == 42)
          }
        }

        describe("either asTask") {
          test("fail") {
            assertThrows[ForbiddenFailure](left.asTask(thr2ano).r)
          }

          test("pure") {
            assert(right.asTask(thr2ano).r == 42)
          }
        }

        describe("either asTaskThr — transform") {
          test("fail") {
            assertThrows[RuntimeException](left.asTaskThr(thr2thr).r)
          }

          test("pure") {
            assert(right.asTaskThr(thr2thr).r == 42)
          }
        }

        describe("either asTaskThr") {
          test("fail") {
            assertThrows[RuntimeException](left.asTaskThr.r)
          }

          test("pure") {
            assert(right.asTaskThr.r == 42)
          }
        }

        describe("result asTask") {
          test("fail") {
            assertThrows[InvalidInputFailure](incorrect.asTask.r)
          }

          test("pure") {
            assert(correct.asTask.r == 42)
          }
        }

        describe("validated asTask") {
          test("invalid") {
            assertThrows[GenericValidationFailures](invalid.asTask.r)
          }

          test("valid") {
            assert(valid.asTask.r == 42)
          }

          test("invalid — ano") {
            assertThrows[TVFs](invalid.asTask(TVFs).r)
          }

          test("valid — ano") {
            assert(valid.asTask(TVFs).r == 42)
          }
        }

        describe("future as Task") {
          test("fail") {
            assertThrows[InvalidInputFailure](failedF.asTask.r)
          }

          test("pure") {
            assert(successF.asTask.r == 42)
          }
        }

        describe("io as Task") {
          test("fail") {
            assertThrows[InvalidInputFailure](IO.fail(ano).asTask.r)
          }

          test("pure") {
            assert(IO.pure(42).asTask.r == 42)
          }

        }

      } //end as{Effect} reverse

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

    describe("Task — companion object syntax") {

      describe("suspend") {

        test("suspendOption") {
          val f = Task.suspendOption(
            Option(throw thr),
            ano
          )
          assertThrows[RuntimeException](f.r)

        }

        test("suspendOptionThr") {
          val f = Task.suspendOptionThr(
            Option(throw thr),
            iae
          )
          assertThrows[RuntimeException](f.r)

        }

        test("suspendTry") {
          val f = Task.suspendTry(
            Try.pure(throw thr)
          )
          assertThrows[RuntimeException](f.r)
        }

        test("suspendEither") {
          val f = Task.suspendEither(
            Right[Throwable, String](throw thr),
            thr2ano
          )
          assertThrows[RuntimeException](f.r)
        }

        test("suspendEitherThr") {
          val f = Task.suspendEitherThr(
            Right[Throwable, String](throw thr)
          )
          assertThrows[RuntimeException](f.r)
        }

        test("suspendEitherThr — transform") {
          val f = Task.suspendEitherThr(
            Right[Throwable, String](throw thr),
            thr2thr
          )
          assertThrows[RuntimeException](f.r)
        }

        test("suspendResult") {
          val f = Task.suspendResult(
            Result.pure(throw thr)
          )
          assertThrows[RuntimeException](f.r)
        }

        describe("suspendValidated") {
          test("normal") {
            val f = Task.suspendValidated(
              Validated.pure(throw thr)
            )
            assertThrows[RuntimeException](f.r)
          }

          test("ano") {
            val f = Task.suspendValidated(
              Validated.pure(throw thr),
              TVFs
            )
            assertThrows[RuntimeException](f.r)
          }
        }

        test("suspendFuture") {
          var sideEffect: Int = 0
          val f = Task.suspendFuture(
            Future {
              sideEffect = 42
              sideEffect
            }
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
            val f = Task.effectOnFalse(
              false,
              Task {
                sideEffect = 42
                sideEffect
              }
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("true") {
            var sideEffect: Int = 0
            val f = Task.effectOnFalse(
              true,
              Task {
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
            val f = Task.effectOnTrue(
              false,
              Task {
                sideEffect = 42
                sideEffect
              }
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("true") {
            var sideEffect: Int = 0
            val f = Task.effectOnTrue(
              true,
              Task {
                sideEffect = 42
                sideEffect
              }
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }
        }

        describe("flatEffectOnFalse") {

          test("false") {
            var sideEffect: Int = 0
            val f = Task.flatEffectOnFalse(
              bfalse,
              Task {
                sideEffect = 42
                sideEffect
              }
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("true") {
            var sideEffect: Int = 0
            val f = Task.flatEffectOnFalse(
              btrue,
              Task {
                sideEffect = 42
                sideEffect
              }
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = Task.flatEffectOnFalse(
              bfail,
              Task {
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
            val f = Task.flatEffectOnTrue(
              bfalse,
              Task {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("true") {
            var sideEffect: Int = 0
            val f = Task.flatEffectOnTrue(
              btrue,
              Task {
                sideEffect = 42
                sideEffect
              }
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = Task.flatEffectOnTrue(
              bfail,
              Task {
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
            val f = Task.effectOnFail(
              none,
              Task {
                sideEffect = 42
                sideEffect
              }
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("some") {
            var sideEffect: Int = 0
            val f = Task.effectOnFail(
              some,
              Task {
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
            val f = Task.effectOnPure(
              none,
              (x: Int) =>
                Task {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("some") {
            var sideEffect: Int = 0
            val f = Task.effectOnPure(
              some,
              (x: Int) =>
                Task {
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
            val f = Task.flatEffectOnNone(
              Task.pure(none),
              Task {
                sideEffect = 42
                sideEffect
              }
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)
          }

          test("some") {
            var sideEffect: Int = 0
            val f = Task.flatEffectOnNone(
              Task.pure(some),
              Task {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = Task.flatEffectOnNone(
              Task.fail[Option[Int]](ano),
              Task {
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
            val f = Task.flatEffectOnSome(
              Task.pure(none),
              (x: Int) =>
                Task {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")
          }

          test("some") {
            var sideEffect: Int = 0
            val f = Task.flatEffectOnSome(
              Task.pure(some),
              (x: Int) =>
                Task {
                  sideEffect = x
                  sideEffect
              }
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = Task.flatEffectOnSome(
              Task.fail[Option[Int]](ano),
              (x: Int) =>
                Task {
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
            val f = Task.effectOnFail(
              incorrect,
              (_: Anomaly) =>
                Task {
                  sideEffect = 42
                  sideEffect
              }
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("correct") {
            var sideEffect: Int = 0
            val f = Task.effectOnFail(
              correct,
              (_: Anomaly) =>
                Task {
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
            val f = Task.effectOnPure(
              incorrect,
              (x: Int) =>
                Task {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("correct") {
            var sideEffect: Int = 0
            val f = Task.effectOnPure(
              correct,
              (x: Int) =>
                Task {
                  sideEffect = x
                  sideEffect
              }
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }
        }

        describe("flatEffectOnIncorrect") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f = Task.flatEffectOnIncorrect(
              Task.pure(incorrect),
              (_: Anomaly) =>
                Task {
                  sideEffect = 42
                  sideEffect
              }
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)
          }

          test("correct") {
            var sideEffect: Int = 0
            val f = Task.flatEffectOnIncorrect(
              Task.pure(correct),
              (_: Anomaly) =>
                Task {
                  sideEffect = 42
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = Task.flatEffectOnIncorrect(
              Task.fail[Result[Int]](ano),
              (_: Anomaly) =>
                Task {
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
            val f = Task.flatEffectOnCorrect(
              Task.pure(incorrect),
              (x: Int) =>
                Task {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")
          }

          test("correct") {
            var sideEffect: Int = 0
            val f = Task.flatEffectOnCorrect(
              Task.pure(correct),
              (x: Int) =>
                Task {
                  sideEffect = x
                  sideEffect
              }
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = Task.flatEffectOnCorrect(
              Task.fail[Result[Int]](ano),
              (x: Int) =>
                Task {
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

    describe("Task — other effect reference syntax") {

      describe("suspendInTask") {

        describe("reference") {

          test("suspendOption") {
            val f = Option(throw thr).suspendInTask(ano)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendOptionThr") {
            val f = Option(throw thr).suspendInTaskThr(thr)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendTry") {
            val f = Try.pure(throw thr).suspendInTask
            assertThrows[RuntimeException](f.r)
          }

          test("suspendEither") {
            val f = Right[Throwable, String](throw thr).suspendInTask(thr2ano)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendEitherThr") {
            val f = Right[Throwable, String](throw thr).suspendInTaskThr
            assertThrows[RuntimeException](f.r)
          }

          test("suspendEitherThr — transform") {
            val f = Right[Throwable, String](throw thr).suspendInTaskThr(thr2thr)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendResult") {
            val f = Result.pure(throw thr).suspendInTask
            assertThrows[RuntimeException](f.r)
          }

          describe("suspendValidated") {
            test("normal") {
              val f = Validated.pure(throw thr).suspendInTask
              assertThrows[RuntimeException](f.r)
            }

            test("ano") {
              val f = Validated.pure(throw thr).suspendInTask(TVFs)
              assertThrows[RuntimeException](f.r)
            }
          }

          test("suspendFuture") {
            var sideEffect: Int = 0
            val f = Future[Int] {
              sideEffect = 42
              sideEffect
            }.suspendInTask
            sleep()
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)
          }
        }

        describe("companion") {

          test("suspendOption") {
            val f = Option.suspendInTask(Option(throw thr), ano)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendOptionThr") {
            val f = Option.suspendInTaskThr(Option(throw thr), thr)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendTry") {
            val f = Try.suspendInTask(Try.pure(throw thr))
            assertThrows[RuntimeException](f.r)
          }

          test("suspendEither") {
            val f = Either.suspendInTask(Right[Throwable, String](throw thr), thr2ano)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendEitherThr") {
            val f = Either.suspendInTaskThr(Right[Throwable, String](throw thr))
            assertThrows[RuntimeException](f.r)
          }

          test("suspendEitherThr — transform") {
            val f = Either.suspendInTaskThr(Right[Throwable, String](throw thr), thr2thr)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendResult") {
            val f = Result.suspendInTask(Result.pure(throw thr))
            assertThrows[RuntimeException](f.r)
          }

          describe("suspendValidated") {
            test("normal") {
              val f = Validated.suspendInTask(Validated.pure(throw thr))
              assertThrows[RuntimeException](f.r)
            }

            test("ano") {
              val f = Validated.suspendInTask(Validated.pure(throw thr), TVFs)
              assertThrows[RuntimeException](f.r)
            }
          }

          test("suspendFuture") {
            var sideEffect: Int = 0
            val f = Future.suspendInTask {
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
            val f = false.effectOnFalseTask(
              Task {
                sideEffect = 42
                sideEffect
              }
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("true") {
            var sideEffect: Int = 0
            val f = true.effectOnFalseTask(
              Task {
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
            val f = false.effectOnTrueTask(
              Task {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("true") {
            var sideEffect: Int = 0
            val f = true.effectOnTrueTask(
              Task {
                sideEffect = 42
                sideEffect
              }
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
              Task {
                sideEffect = 42
                sideEffect
              }
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("true") {
            var sideEffect: Int = 0
            val f = btrue.effectOnFalse(
              Task {
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
              Task {
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
              Task {
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
              Task {
                sideEffect = 42
                sideEffect
              }
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = bfail.effectOnTrue(
              Task {
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
            val f = none.effectOnFailTask(
              Task {
                sideEffect = 42
                sideEffect
              }
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("some") {
            var sideEffect: Int = 0
            val f = some.effectOnFailTask(
              Task {
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
            val f = none.effectOnPureTask(
              (x: Int) =>
                Task {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("some") {
            var sideEffect: Int = 0
            val f = some.effectOnPureTask(
              (x: Int) =>
                Task {
                  sideEffect = x
                  sideEffect
              }
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }
        }

        describe("flatEffectOnNone") {

          test("none") {
            var sideEffect: Int = 0
            val f = Task
              .pure(none)
              .effectOnFail(
                Task {
                  sideEffect = 42
                  sideEffect
                }
              )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)
          }

          test("some") {
            var sideEffect: Int = 0
            val f =
              Task
                .pure(some)
                .effectOnFail(
                  Task {
                    sideEffect = 42
                    sideEffect
                  }
                )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = Task
              .fail[Option[Int]](ano)
              .effectOnFail(
                Task {
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
            val f = Task
              .pure(none)
              .effectOnPure(
                (x: Int) =>
                  Task {
                    sideEffect = x
                    sideEffect
                }
              )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")
          }

          test("some") {
            var sideEffect: Int = 0
            val f = Task
              .pure(some)
              .effectOnPure(
                (x: Int) =>
                  Task {
                    sideEffect = x
                    sideEffect
                }
              )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = Task
              .fail[Option[Int]](ano)
              .effectOnPure(
                (x: Int) =>
                  Task {
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
            val f = incorrect.effectOnFailTask(
              (_: Anomaly) =>
                Task {
                  sideEffect = 42
                  sideEffect
              }
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("correct") {
            var sideEffect: Int = 0
            val f = correct.effectOnFailTask(
              (_: Anomaly) =>
                Task {
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
            val f = incorrect.effectOnPureTask(
              (x: Int) =>
                Task {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("correct") {
            var sideEffect: Int = 0
            val f = correct.effectOnPureTask(
              (x: Int) =>
                Task {
                  sideEffect = x
                  sideEffect
              }
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }
        }

        describe("flatEffectOnIncorrect") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f = Task
              .pure(incorrect)
              .effectOnFail(
                (_: Anomaly) =>
                  Task {
                    sideEffect = 42
                    sideEffect
                }
              )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)
          }

          test("correct") {
            var sideEffect: Int = 0
            val f = Task
              .pure(correct)
              .effectOnFail(
                (_: Anomaly) =>
                  Task {
                    sideEffect = 42
                    sideEffect
                }
              )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = Task
              .fail[Result[Int]](ano)
              .effectOnFail(
                (_: Anomaly) =>
                  Task {
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
              Task
                .pure(incorrect)
                .effectOnPure(
                  (x: Int) =>
                    Task {
                      sideEffect = x
                      sideEffect
                  }
                )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")
          }

          test("correct") {
            var sideEffect: Int = 0
            val f = Task
              .pure(correct)
              .effectOnPure(
                (x: Int) =>
                  Task {
                    sideEffect = x
                    sideEffect
                }
              )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }

          test("fail") {
            var sideEffect: Int = 0
            val f =
              Task
                .fail[Result[Int]](ano)
                .effectOnPure(
                  (x: Int) =>
                    Task {
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

      describe("Task.traverse_") {

        test("empty list") {
          val input: Seq[Int] = List()

          var sideEffect: Int = 0

          val eventualResult = Task.traverse_(input) { _ =>
            Task {
              sideEffect = 42
            }
          }

          eventualResult.r
          assert(sideEffect == 0, "nothing should have happened")
        }

      }

      describe("Task.sequence_") {

        test("empty list") {
          val input: Seq[Int] = List()

          var sideEffect: Int = 0

          val eventualResult = Task.sequence_ {
            input.map { _ =>
              Task {
                sideEffect = 42
              }
            }
          }

          eventualResult.r
          assert(sideEffect == 0, "nothing should have happened")
        }

      }

      describe("Task.serialize") {

        test("empty list") {
          val input:    Seq[Int] = List()
          val expected: Seq[Int] = List()

          var sideEffect: Int = 0

          val eventualResult = Task.serialize(input) { _ =>
            Task {
              sideEffect = 42
            }
          }

          assert(eventualResult.r == expected)
          assert(sideEffect == 0, "nothing should have happened")
        }

        test("no two tasks should run in parallel") {
          val input: Seq[Int] = (1 to 100).toList
          val expected = input.map(_.toString)

          var previouslyProcessed: Option[Int] = None
          var startedFlag:         Option[Int] = None

          val eventualResult: Task[Seq[String]] = Task.serialize(input) { i =>
            Task {
              assert(
                startedFlag.isEmpty,
                s"started flag should have been empty at the start of each task but was: $startedFlag"
              )
              previouslyProcessed foreach { previous =>
                assertResult(expected = i - 1, "... the tasks were not executed in the correct order.")(
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

      describe("Task.serialize_") {

        test("empty list") {
          val input: Seq[Int] = List()

          var sideEffect: Int = 0

          val eventualResult = Task.serialize_(input) { _ =>
            Task {
              sideEffect = 42
            }
          }

          eventualResult.r
          assert(sideEffect == 0, "nothing should have happened")
        }

        test("no two tasks should run in parallel") {
          val input: Seq[Int] = (1 to 100).toList

          var previouslyProcessed: Option[Int] = None
          var startedFlag:         Option[Int] = None

          val eventualResult: Task[Unit] = Task.serialize_(input) { i =>
            Task {
              assert(
                startedFlag.isEmpty,
                s"started flag should have been empty at the start of each task but was: $startedFlag"
              )
              previouslyProcessed foreach { previous =>
                assertResult(expected = i - 1, "... the tasks were not executed in the correct order.")(
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

    }

  }

} //end test
