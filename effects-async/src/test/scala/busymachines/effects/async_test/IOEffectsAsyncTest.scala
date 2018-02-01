package busymachines.effects.async_test

import busymachines.core._
import busymachines.effects.async._
import busymachines.effects.sync._
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

  private implicit class TestSyntax[T](value: IO[T]) {
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

  private val failedF:  Future[Int] = Future.fail(ano)
  private val successF: Future[Int] = Future.pure(42)

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

  private val failV: IO[Int] = IO.fail(ano)
  private val pureV: IO[Int] = IO.pure(42)

  private val btrue:  IO[Boolean] = IO.pure(true)
  private val bfalse: IO[Boolean] = IO.pure(false)
  private val bfail:  IO[Boolean] = IO.failWeak(iae)

  //---------------------------------------------------------------------------
  describe("sync + pure") {

    describe("IO — companion object syntax") {

      describe("constructors") {
        test("pure") {
          assert(IO.pure(42).unsafeSyncGet() == 42)
        }

        test("fail") {
          assertThrows[InvalidInputFailure](IO.fail(ano).r)
          assertThrows[RuntimeException](IO.failWeak(thr).r)
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

        describe("fromOptionWeak") {
          test("none") {
            assertThrows[RuntimeException](IO.fromOptionWeak(none, thr).r)
          }

          test("some") {
            assert(IO.fromOptionWeak(some, thr).r == 42)
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
            assertThrows[RuntimeException](IO.fromEitherWeak(left).r)
          }

          test("left — transform") {
            assertThrows[ForbiddenFailure](IO.fromEitherAnomaly(left, thr2ano).r)
          }

          test("right") {
            assert(IO.fromEitherWeak(right).r == 42)
          }

          test("right — transform") {
            assert(IO.fromEitherAnomaly(right, thr2ano).r == 42)
          }
        }

        describe("fromEitherWeak") {
          test("left — transform") {
            assertThrows[IllegalArgumentException](IO.fromEitherWeak(left, (t: Throwable) => iae).r)
          }

          test("right") {
            assert(IO.fromEitherWeak(right, (t: Throwable) => iae).r == 42)
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

        describe("fromFuture") {
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
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = IO.cond(
              true,
              42,
              ano
            )
            assert(value.r == 42)
          }
        }

        describe("condWeak") {
          test("false") {
            val value = IO.condWeak(
              false,
              42,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = IO.condWeak(
              true,
              42,
              thr
            )
            assert(value.r == 42)
          }
        }

        describe("condWith") {
          test("false — pure") {
            val value = IO.condWith(
              false,
              pureV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true — pure") {
            val value = IO.condWith(
              true,
              pureV,
              ano
            )
            assert(value.r == 42)
          }

          test("false — fail") {
            val value = IO.condWith(
              false,
              failV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true — fail") {
            val value = IO.condWith(
              true,
              failV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }
        }

        describe("condWithWeak") {
          test("false — pure") {
            val value = IO.condWithWeak(
              false,
              pureV,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true — pure") {
            val value = IO.condWithWeak(
              true,
              pureV,
              thr
            )
            assert(value.r == 42)
          }

          test("false — fail") {
            val value = IO.condWithWeak(
              false,
              failV,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true — fail") {
            val value = IO.condWithWeak(
              true,
              failV,
              thr
            )
            assertThrows[InvalidInputFailure](value.r)
          }
        }

        describe("flatCond") {
          test("false") {
            val value = IO.flatCond(
              bfalse,
              42,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = IO.flatCond(
              btrue,
              42,
              ano
            )
            assert(value.r == 42)
          }

          test("fail") {
            val value = IO.flatCond(
              bfail,
              42,
              ano
            )
            assertThrows[IllegalArgumentException](value.r)
          }
        }

        describe("flatCondWeak") {
          test("false") {
            val value = IO.flatCondWeak(
              bfalse,
              42,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = IO.flatCondWeak(
              btrue,
              42,
              thr
            )
            assert(value.r == 42)
          }

          test("fail") {
            val value = IO.flatCondWeak(
              bfail,
              42,
              thr
            )
            assertThrows[IllegalArgumentException](value.r)
          }
        }

        describe("flatCondWith") {
          test("false — pure") {
            val value = IO.flatCondWith(
              bfalse,
              pureV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("false — fail") {
            val value = IO.flatCondWith(
              bfalse,
              failV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true — pure") {
            val value = IO.flatCondWith(
              btrue,
              pureV,
              ano
            )
            assert(value.r == 42)
          }

          test("true — fail") {
            val value = IO.flatCondWith(
              btrue,
              failV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("fail — pure") {
            val value = IO.flatCondWith(
              bfail,
              pureV,
              ano
            )
            assertThrows[IllegalArgumentException](value.r)
          }

          test("fail — fail") {
            val value = IO.flatCondWith(
              bfail,
              failV,
              ano
            )
            assertThrows[IllegalArgumentException](value.r)
          }
        }

        describe("flatCondWithWeak") {
          test("false — pure") {
            val value = IO.flatCondWithWeak(
              bfalse,
              pureV,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("false — fail") {
            val value = IO.flatCondWithWeak(
              bfalse,
              failV,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true — pure") {
            val value = IO.flatCondWithWeak(
              btrue,
              pureV,
              thr
            )
            assert(value.r == 42)
          }

          test("true — fail") {
            val value = IO.flatCondWithWeak(
              btrue,
              failV,
              thr
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("fail — pure") {
            val value = IO.flatCondWithWeak(
              bfail,
              pureV,
              thr
            )
            assertThrows[IllegalArgumentException](value.r)
          }

          test("fail — fail") {
            val value = IO.flatCondWithWeak(
              bfail,
              failV,
              thr
            )
            assertThrows[IllegalArgumentException](value.r)
          }
        }

        describe("failOnTrue") {
          test("false") {
            val value = IO.failOnTrue(
              false,
              ano
            )
            value.r
          }

          test("true") {
            val value = IO.failOnTrue(
              true,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }
        }

        describe("failOnTrueWeak") {
          test("false") {
            val value = IO.failOnTrueWeak(
              false,
              thr
            )
            value.r
          }

          test("true") {
            val value = IO.failOnTrueWeak(
              true,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }
        }

        describe("failOnFalse") {
          test("false") {
            val value = IO.failOnFalse(
              false,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = IO.failOnFalse(
              true,
              ano
            )
            value.r
          }
        }

        describe("failOnFalseWeak") {
          test("false") {
            val value = IO.failOnFalseWeak(
              false,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = IO.failOnFalseWeak(
              true,
              thr
            )
            value.r
          }
        }

        describe("flatFailOnTrue") {
          test("false") {
            val value = IO.flatFailOnTrue(
              bfalse,
              ano
            )
            value.r
          }

          test("true") {
            val value = IO.flatFailOnTrue(
              btrue,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("fail") {
            val value = IO.flatFailOnTrue(
              bfail,
              ano
            )
            assertThrows[IllegalArgumentException](value.r)
          }

        }

        describe("flatFailOnTrueWeak") {
          test("false") {
            val value = IO.flatFailOnTrueWeak(
              bfalse,
              thr
            )
            value.r
          }

          test("true") {
            val value = IO.flatFailOnTrueWeak(
              btrue,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("fail") {
            val value = IO.flatFailOnTrueWeak(
              bfail,
              thr
            )
            assertThrows[IllegalArgumentException](value.r)
          }

        }

        describe("flatFailOnFalse") {
          test("false") {
            val value = IO.flatFailOnFalse(
              bfalse,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = IO.flatFailOnFalse(
              btrue,
              ano
            )
            value.r
          }

          test("fail") {
            val value = IO.flatFailOnFalse(
              bfail,
              ano
            )
            assertThrows[IllegalArgumentException](value.r)
          }

        }

        describe("flatFailOnFalseWeak") {
          test("false") {
            val value = IO.flatFailOnFalseWeak(
              bfalse,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = IO.flatFailOnFalseWeak(
              btrue,
              thr
            )
            value.r
          }

          test("fail") {
            val value = IO.flatFailOnFalseWeak(
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
              IO.flattenOption(IO.pure(none), ano).r
            }
          }

          test("pure — some") {
            assert(IO.flattenOption(IO.pure(some), ano).r == 42)
          }

          test("fail") {
            assertThrows[RuntimeException] {
              IO.flattenOption(IO.failWeak[Option[Int]](thr), ano).r
            }
          }
        }

        describe("flattenOptionWeak") {

          test("pure — none") {
            assertThrows[RuntimeException] {
              IO.flattenOptionWeak(IO.pure(none), thr).r
            }
          }

          test("pure — some") {
            assert(IO.flattenOptionWeak(IO.pure(some), thr).r == 42)
          }

          test("fail") {
            assertThrows[InvalidInputFailure] {
              IO.flattenOptionWeak(IO.fail[Option[Int]](ano), thr).r
            }
          }
        }

        describe("flattenResult") {
          test("incorrect") {
            assertThrows[InvalidInputFailure](
              IO.flattenResult(IO.pure(incorrect)).r
            )
          }

          test("correct") {
            assert(IO.flattenResult(IO.pure(correct)).r == 42)
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

        describe("option asIOWeak") {
          test("fail") {
            assertThrows[IllegalArgumentException](Option.asIOWeak(none, iae).r)
          }

          test("pure") {
            assert(Option.asIOWeak(some, iae).r == 42)
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

        describe("either asIOWeak — transform") {
          test("fail") {
            assertThrows[RuntimeException](Either.asIOWeak(left, thr2thr).r)
          }

          test("pure") {
            assert(Either.asIOWeak(right, thr2thr).r == 42)
          }
        }

        describe("either asIOWeak") {
          test("fail") {
            assertThrows[RuntimeException](Either.asIOWeak(left).r)
          }

          test("pure") {
            assert(Either.asIOWeak(right).r == 42)
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
              thr2ano
            )

            assertThrows[ForbiddenFailure](value.r)
          }

          test("pure") {
            val value = IO.bimap(
              pureV,
              int2str,
              thr2ano
            )

            assert(value.r == "42")
          }

        }

        describe("bimap — result") {

          test("fail") {
            val value = IO.bimap(
              failV,
              res2res
            )

            assertThrows[ForbiddenFailure](value.r)
          }

          test("pure") {
            val value = IO.bimap(
              pureV,
              res2res
            )

            assert(value.r == "42")
          }

        }

        describe("bimapWeak") {

          test("fail") {
            val value = IO.bimapWeak(
              failV,
              int2str,
              thr2thr
            )

            assertThrows[IllegalArgumentException](value.r)
          }

          test("pure") {
            val value = IO.bimapWeak(
              pureV,
              int2str,
              thr2thr
            )

            assert(value.r == "42")
          }

        }

        describe("morph") {

          test("fail") {
            val value = IO.morph(
              failV,
              int2str,
              thr2str
            )
            assert(value.r == ano.message)
          }

          test("pure") {
            val value = IO.morph(
              pureV,
              int2str,
              thr2str
            )
            assert(value.r == "42")
          }
        }

        describe("morph — result") {

          test("fail") {
            val value = IO.morph(
              failV,
              res2str
            )
            assert(value.r == ano.message)
          }

          test("pure") {
            val value = IO.morph(
              pureV,
              res2str
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
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true") {
            val value = true.condIO(
              42,
              ano
            )
            assert(value.r == 42)
          }
        }

        describe("condWeak") {
          test("false") {
            val value =
              false.condIOWeak(
                42,
                thr
              )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = true.condIOWeak(
              42,
              thr
            )
            assert(value.r == 42)
          }
        }

        describe("condWith") {
          test("false — pure") {
            val value = false.condWithIO(
              pureV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true — pure") {
            val value = true.condWithIO(
              pureV,
              ano
            )
            assert(value.r == 42)
          }

          test("false — fail") {
            val value = false.condWithIO(
              failV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("true — fail") {
            val value = true.condWithIO(
              failV,
              ano
            )
            assertThrows[InvalidInputFailure](value.r)
          }
        }

        describe("condWithWeak") {
          test("false — pure") {
            val value = false.condWithIOWeak(
              pureV,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true — pure") {
            val value = true.condWithIOWeak(
              pureV,
              thr
            )
            assert(value.r == 42)
          }

          test("false — fail") {
            val value =
              false.condWithIOWeak(
                failV,
                thr
              )
            assertThrows[RuntimeException](value.r)
          }

          test("true — fail") {
            val value = true.condWithIOWeak(
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

        describe("flatCondWeak") {
          test("false") {
            val value = bfalse.condWeak(
              42,
              thr
            )
            assertThrows[RuntimeException](value.r)
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

        describe("flatCondWithWeak") {
          test("false — pure") {
            val value = bfalse.condWithWeak(
              pureV,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("false — fail") {
            val value = bfalse.condWithWeak(
              failV,
              thr
            )
            assertThrows[RuntimeException](value.r)
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
            assertThrows[InvalidInputFailure](value.r)
          }

          test("fail — pure") {
            val value = bfail.condWithWeak(
              pureV,
              thr
            )
            assertThrows[IllegalArgumentException](value.r)
          }

          test("fail — fail") {
            val value = bfail.condWithWeak(
              failV,
              thr
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

        describe("failOnTrueWeak") {
          test("false") {
            val value = false.failOnTrueIOWeak(thr)
            value.r
          }

          test("true") {
            val value = true.failOnTrueIOWeak(thr)
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

        describe("failOnFalseWeak") {
          test("false") {
            val value = false.failOnFalseIOWeak(thr)
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = true.failOnFalseIOWeak(thr)
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
              IO.pure(none).flattenOption(ano).r
            }
          }

          test("pure — some") {
            assert(IO.pure(some).flattenOption(ano).r == 42)
          }

          test("fail") {
            assertThrows[RuntimeException] {
              IO.failWeak[Option[Int]](thr).flattenOption(ano).r
            }
          }
        }

        describe("flattenOptionWeak") {

          test("pure — none") {
            assertThrows[RuntimeException] {
              IO.pure(none).flattenOptionWeak(thr).r
            }
          }

          test("pure — some") {
            assert(IO.pure(some).flattenOptionWeak(thr).r == 42)
          }

          test("fail") {
            assertThrows[InvalidInputFailure] {
              IO.fail[Option[Int]](ano).flattenOptionWeak(thr).r
            }
          }
        }

        describe("flattenResult") {
          test("incorrect") {
            assertThrows[InvalidInputFailure](
              IO.pure(incorrect).flattenResult.r
            )
          }

          test("correct") {
            assert(IO.pure(correct).flattenResult.r == 42)
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

        describe("option asIOWeak") {
          test("fail") {
            assertThrows[IllegalArgumentException](none.asIOWeak(iae).r)
          }

          test("pure") {
            assert(some.asIOWeak(iae).r == 42)
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

        describe("either asIOWeak — transform") {
          test("fail") {
            assertThrows[RuntimeException](left.asIOWeak(thr2thr).r)
          }

          test("pure") {
            assert(right.asIOWeak(thr2thr).r == 42)
          }
        }

        describe("either asIOWeak") {
          test("fail") {
            assertThrows[RuntimeException](left.asIOWeak.r)
          }

          test("pure") {
            assert(right.asIOWeak.r == 42)
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

    describe("IO — companion object syntax") {

      describe("suspend") {

        test("suspendOption") {
          val f = IO.suspendOption(
            Option(throw thr),
            ano
          )
          assertThrows[RuntimeException](f.r)

        }

        test("suspendOptionWeak") {
          val f = IO.suspendOptionWeak(
            Option(throw thr),
            iae
          )
          assertThrows[RuntimeException](f.r)

        }

        test("suspendTry") {
          val f = IO.suspendTry(
            Try.pure(throw thr)
          )
          assertThrows[RuntimeException](f.r)
        }

        test("suspendEither") {
          val f = IO.suspendEither(
            Right[Throwable, String](throw thr),
            thr2ano
          )
          assertThrows[RuntimeException](f.r)
        }

        test("suspendEitherWeak") {
          val f = IO.suspendEitherWeak(
            Right[Throwable, String](throw thr)
          )
          assertThrows[RuntimeException](f.r)
        }

        test("suspendEitherWeak — transform") {
          val f = IO.suspendEitherWeak(
            Right[Throwable, String](throw thr),
            thr2thr
          )
          assertThrows[RuntimeException](f.r)
        }

        test("suspendResult") {
          val f = IO.suspendResult(
            Result.pure(throw thr)
          )
          assertThrows[RuntimeException](f.r)
        }

        test("suspendFuture") {
          var sideEffect: Int = 0
          val f = IO.suspendFuture(
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
            val f = IO.effectOnFalse(
              false,
              IO {
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
            val f = IO.effectOnFalse(
              true,
              IO {
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
            val f = IO.effectOnTrue(
              false,
              IO {
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
            val f = IO.effectOnTrue(
              true,
              IO {
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
            val f = IO.flatEffectOnFalse(
              bfalse,
              IO {
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
            val f = IO.flatEffectOnFalse(
              btrue,
              IO {
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
            val f = IO.flatEffectOnFalse(
              bfail,
              IO {
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
            val f = IO.flatEffectOnTrue(
              bfalse,
              IO {
                sideEffect = 42
                sideEffect
              }
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
              }
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
              }
            )
            assertThrows[IllegalArgumentException](f.r)
            assert(sideEffect == 0, "side effect should not have applied on fail")

          }

        }
      }

      describe("effect on option") {

        describe("effectOnEmpty") {

          test("none") {
            var sideEffect: Int = 0
            val f = IO.effectOnEmpty(
              none,
              IO {
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
            val f = IO.effectOnEmpty(
              some,
              IO {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }
        }

        describe("effectOnSome") {

          test("none") {
            var sideEffect: Int = 0
            val f = IO.effectOnSome(
              none,
              (x: Int) =>
                IO {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("some") {
            var sideEffect: Int = 0
            val f = IO.effectOnSome(
              some,
              (x: Int) =>
                IO {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            assert(sideEffect == 42)

          }
        }

        describe("flatEffectOnEmpty") {

          test("none") {
            var sideEffect: Int = 0
            val f = IO.flatEffectOnEmpty(
              IO.pure(none),
              IO {
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
            val f = IO.flatEffectOnEmpty(
              IO.pure(some),
              IO {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = IO.flatEffectOnEmpty(
              IO.fail[Option[Int]](ano),
              IO {
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
            val f = IO.flatEffectOnSome(
              IO.pure(none),
              (x: Int) =>
                IO {
                  sideEffect = x
                  sideEffect
              }
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
              }
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
              }
            )
            assertThrows[InvalidInputFailure](f.r)
            assert(sideEffect == 0, "side effect should not have applied on fail")

          }

        }

      }

      describe("effect on result") {

        describe("effectOnIncorrect") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f = IO.effectOnIncorrect(
              incorrect,
              (a: Anomaly) =>
                IO {
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
            val f = IO.effectOnIncorrect(
              correct,
              (a: Anomaly) =>
                IO {
                  sideEffect = 42
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }
        }

        describe("effectOnCorrect") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f = IO.effectOnCorrect(
              incorrect,
              (x: Int) =>
                IO {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("correct") {
            var sideEffect: Int = 0
            val f = IO.effectOnCorrect(
              correct,
              (x: Int) =>
                IO {
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
            val f = IO.flatEffectOnIncorrect(
              IO.pure(incorrect),
              (a: Anomaly) =>
                IO {
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
            val f = IO.flatEffectOnIncorrect(
              IO.pure(correct),
              (a: Anomaly) =>
                IO {
                  sideEffect = 42
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = IO.flatEffectOnIncorrect(
              IO.fail[Result[Int]](ano),
              (a: Anomaly) =>
                IO {
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
            val f = IO.flatEffectOnCorrect(
              IO.pure(incorrect),
              (x: Int) =>
                IO {
                  sideEffect = x
                  sideEffect
              }
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
              }
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
              }
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

          test("suspendOptionWeak") {
            val f = Option(throw thr).suspendInIOWeak(thr)
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

          test("suspendEitherWeak") {
            val f = Right[Throwable, String](throw thr).suspendInIOWeak
            assertThrows[RuntimeException](f.r)
          }

          test("suspendEitherWeak — transform") {
            val f = Right[Throwable, String](throw thr).suspendInIOWeak(thr2thr)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendResult") {
            val f = Result.pure(throw thr).suspendInIO
            assertThrows[RuntimeException](f.r)
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

          test("suspendOptionWeak") {
            val f = Option.suspendInIOWeak(Option(throw thr), thr)
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

          test("suspendEitherWeak") {
            val f = Either.suspendInIOWeak(Right[Throwable, String](throw thr))
            assertThrows[RuntimeException](f.r)
          }

          test("suspendEitherWeak — transform") {
            val f = Either.suspendInIOWeak(Right[Throwable, String](throw thr), thr2thr)
            assertThrows[RuntimeException](f.r)
          }

          test("suspendResult") {
            val f = Result.suspendInIO(Result.pure(throw thr))
            assertThrows[RuntimeException](f.r)
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
              }
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
              }
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
              }
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
              IO {
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
              IO {
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
              IO {
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
              IO {
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
              IO {
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
              IO {
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

        describe("effectOnEmpty") {

          test("none") {
            var sideEffect: Int = 0
            val f = none.effectOnEmptyIO(
              IO {
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
            val f = some.effectOnEmptyIO(
              IO {
                sideEffect = 42
                sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }
        }

        describe("effectOnSome") {

          test("none") {
            var sideEffect: Int = 0
            val f = none.effectOnSomeIO(
              (x: Int) =>
                IO {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("some") {
            var sideEffect: Int = 0
            val f = some.effectOnSomeIO(
              (x: Int) =>
                IO {
                  sideEffect = x
                  sideEffect
              }
            )
            if (sideEffect == 42) fail("side effect should not have been applied yet")
            f.r
            assert(sideEffect == 42)

          }
        }

        describe("flatEffectOnEmpty") {

          test("none") {
            var sideEffect: Int = 0
            val f = IO
              .pure(none)
              .effectOnEmpty(
                IO {
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
              IO.pure(some)
                .effectOnEmpty(
                  IO {
                    sideEffect = 42
                    sideEffect
                  }
                )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = IO
              .fail[Option[Int]](ano)
              .effectOnEmpty(
                IO {
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
            val f = IO
              .pure(none)
              .effectOnSome(
                (x: Int) =>
                  IO {
                    sideEffect = x
                    sideEffect
                }
              )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")
          }

          test("some") {
            var sideEffect: Int = 0
            val f = IO
              .pure(some)
              .effectOnSome(
                (x: Int) =>
                  IO {
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
            val f = IO
              .fail[Option[Int]](ano)
              .effectOnSome(
                (x: Int) =>
                  IO {
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

        describe("effectOnIncorrect") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f = incorrect.effectOnIncorrectIO(
              (a: Anomaly) =>
                IO {
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
            val f = correct.effectOnIncorrectIO(
              (a: Anomaly) =>
                IO {
                  sideEffect = 42
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }
        }

        describe("effectOnCorrect") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f = incorrect.effectOnCorrectIO(
              (x: Int) =>
                IO {
                  sideEffect = x
                  sideEffect
              }
            )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("correct") {
            var sideEffect: Int = 0
            val f = correct.effectOnCorrectIO(
              (x: Int) =>
                IO {
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
            val f = IO
              .pure(incorrect)
              .effectOnIncorrect(
                (a: Anomaly) =>
                  IO {
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
            val f = IO
              .pure(correct)
              .effectOnIncorrect(
                (a: Anomaly) =>
                  IO {
                    sideEffect = 42
                    sideEffect
                }
              )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")

          }

          test("fail") {
            var sideEffect: Int = 0
            val f = IO
              .fail[Result[Int]](ano)
              .effectOnIncorrect(
                (a: Anomaly) =>
                  IO {
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
              IO.pure(incorrect)
                .effectOnCorrect(
                  (x: Int) =>
                    IO {
                      sideEffect = x
                      sideEffect
                  }
                )
            f.r
            if (sideEffect == 42) fail("side effect should not have executed on other branch")
          }

          test("correct") {
            var sideEffect: Int = 0
            val f = IO
              .pure(correct)
              .effectOnCorrect(
                (x: Int) =>
                  IO {
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
              IO.fail[Result[Int]](ano)
                .effectOnCorrect(
                  (x: Int) =>
                    IO {
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

    describe("IO.traverse") {

      test("empty list") {
        val input:    Seq[Int] = List()
        val expected: Seq[Int] = List()

        var sideEffect: Int = 0

        val eventualResult = IO.traverse(input) { i =>
          IO {
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

        val eventualResult: IO[Seq[String]] = IO.traverse(input) { i =>
          IO {
            assert(
              startedFlag.isEmpty,
              s"started flag should have been empty at the start of each tasks but was: $startedFlag"
            )
            previouslyProcessed foreach { previous =>
              assertResult(expected = i - 1, "... the task were not executed in the correct order.")(
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
              s"started flag should have been empty at the start of each tasks but was: $startedFlag"
            )
            previouslyProcessed foreach { previous =>
              assertResult(expected = i - 1, "... the task were not executed in the correct order.")(
                actual = previous
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

    describe("IO.serialize") {

      test("empty list") {
        val input:    Seq[Int] = List()
        val expected: Seq[Int] = List()

        var sideEffect: Int = 0

        val eventualResult = IO.serialize(input) { i =>
          IO {
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

        val eventualResult: IO[Seq[String]] = IO.serialize(input) { i =>
          IO {
            assert(
              startedFlag.isEmpty,
              s"started flag should have been empty at the start of each tasks but was: $startedFlag"
            )
            previouslyProcessed foreach { previous =>
              assertResult(expected = i - 1, "... the task were not executed in the correct order.")(
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
      }

    }
  }

} //end test
