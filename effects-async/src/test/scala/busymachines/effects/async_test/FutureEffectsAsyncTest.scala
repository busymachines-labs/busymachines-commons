package busymachines.effects.async_test

import busymachines.core._
import busymachines.effects.sync._
import busymachines.effects.async._
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
  private val bfail:  Future[Boolean] = Future.failWeak(iae)

  //---------------------------------------------------------------------------
  describe("sync + pure") {

    describe("Future — companion object syntax") {

      describe("constructors") {
        test("pure") {
          assert(Future.pure(42).unsafeSyncGet() == 42)
        }

        test("fail") {
          assertThrows[InvalidInputFailure](Future.fail(ano).r)
          assertThrows[RuntimeException](Future.failWeak(thr).r)
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

        describe("fromOptionWeak") {
          test("none") {
            assertThrows[RuntimeException](Future.fromOptionWeak(none, thr).r)
          }

          test("some") {
            assert(Future.fromOptionWeak(some, thr).r == 42)
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
            assertThrows[RuntimeException](Future.fromEitherWeak(left).r)
          }

          test("left — transform") {
            assertThrows[ForbiddenFailure](Future.fromEither(left, thr2ano).r)
          }

          test("right") {
            assert(Future.fromEitherWeak(right).r == 42)
          }

          test("right — transform") {
            assert(Future.fromEither(right, thr2ano).r == 42)
          }
        }

        describe("fromEitherWeak") {
          test("left — transform") {
            assertThrows[IllegalArgumentException](Future.fromEitherWeak(left, (t: Throwable) => iae).r)
          }

          test("right") {
            assert(Future.fromEitherWeak(right, (t: Throwable) => iae).r == 42)
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

        describe("condWeak") {
          test("false") {
            val value = Future.condWeak(
              false,
              42,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = Future.condWeak(
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

        describe("condWithWeak") {
          test("false — pure") {
            val value = Future.condWithWeak(
              false,
              pureV,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true — pure") {
            val value = Future.condWithWeak(
              true,
              pureV,
              thr
            )
            assert(value.r == 42)
          }

          test("false — fail") {
            val value = Future.condWithWeak(
              false,
              failV,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true — fail") {
            val value = Future.condWithWeak(
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

        describe("flatCondWeak") {
          test("false") {
            val value = Future.flatCondWeak(
              bfalse,
              42,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = Future.flatCondWeak(
              btrue,
              42,
              thr
            )
            assert(value.r == 42)
          }

          test("fail") {
            val value = Future.flatCondWeak(
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

        describe("flatCondWithWeak") {
          test("false — pure") {
            val value = Future.flatCondWithWeak(
              bfalse,
              pureV,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("false — fail") {
            val value = Future.flatCondWithWeak(
              bfalse,
              failV,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true — pure") {
            val value = Future.flatCondWithWeak(
              btrue,
              pureV,
              thr
            )
            assert(value.r == 42)
          }

          test("true — fail") {
            val value = Future.flatCondWithWeak(
              btrue,
              failV,
              thr
            )
            assertThrows[InvalidInputFailure](value.r)
          }

          test("fail — pure") {
            val value = Future.flatCondWithWeak(
              bfail,
              pureV,
              thr
            )
            assertThrows[IllegalArgumentException](value.r)
          }

          test("fail — fail") {
            val value = Future.flatCondWithWeak(
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

        describe("failOnTrueWeak") {
          test("false") {
            val value = Future.failOnTrueWeak(
              false,
              thr
            )
            value.r
          }

          test("true") {
            val value = Future.failOnTrueWeak(
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

        describe("failOnFalseWeak") {
          test("false") {
            val value = Future.failOnFalseWeak(
              false,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = Future.failOnFalseWeak(
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

        describe("flatFailOnTrueWeak") {
          test("false") {
            val value = Future.flatFailOnTrueWeak(
              bfalse,
              thr
            )
            value.r
          }

          test("true") {
            val value = Future.flatFailOnTrueWeak(
              btrue,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("fail") {
            val value = Future.flatFailOnTrueWeak(
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

        describe("flatFailOnFalseWeak") {
          test("false") {
            val value = Future.flatFailOnFalseWeak(
              bfalse,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = Future.flatFailOnFalseWeak(
              btrue,
              thr
            )
            value.r
          }

          test("fail") {
            val value = Future.flatFailOnFalseWeak(
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
              Future.flattenOption(Future.pure(none), ano).r
            }
          }

          test("pure — some") {
            assert(Future.flattenOption(Future.pure(some), ano).r == 42)
          }

          test("fail") {
            assertThrows[RuntimeException] {
              Future.flattenOption(Future.failWeak[Option[Int]](thr), ano).r
            }
          }
        }

        describe("flattenOptionWeak") {

          test("pure — none") {
            assertThrows[RuntimeException] {
              Future.flattenOptionWeak(Future.pure(none), thr).r
            }
          }

          test("pure — some") {
            assert(Future.flattenOptionWeak(Future.pure(some), thr).r == 42)
          }

          test("fail") {
            assertThrows[InvalidInputFailure] {
              Future.flattenOptionWeak(Future.fail[Option[Int]](ano), thr).r
            }
          }
        }

        describe("flattenResult") {
          test("incorrect") {
            assertThrows[InvalidInputFailure](
              Future.flattenResult(Future.pure(incorrect)).r
            )
          }

          test("correct") {
            assert(Future.flattenResult(Future.pure(correct)).r == 42)
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

        describe("bimapWeak") {

          test("fail") {
            val value = Future.bimapWeak(
              failV,
              int2str,
              thr2thr
            )

            assertThrows[IllegalArgumentException](value.r)
          }

          test("pure") {
            val value = Future.bimapWeak(
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

        describe("condWeak") {
          test("false") {
            val value =
              false.condFutureWeak(
                42,
                thr
              )
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = true.condFutureWeak(
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

        describe("condWithWeak") {
          test("false — pure") {
            val value = false.condWithFutureWeak(
              pureV,
              thr
            )
            assertThrows[RuntimeException](value.r)
          }

          test("true — pure") {
            val value = true.condWithFutureWeak(
              pureV,
              thr
            )
            assert(value.r == 42)
          }

          test("false — fail") {
            val value =
              false.condWithFutureWeak(
                failV,
                thr
              )
            assertThrows[RuntimeException](value.r)
          }

          test("true — fail") {
            val value = true.condWithFutureWeak(
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
            val value = false.failOnTrueFuture(ano)
            value.r
          }

          test("true") {
            val value = true.failOnTrueFuture(ano)
            assertThrows[InvalidInputFailure](value.r)
          }
        }

        describe("failOnTrueWeak") {
          test("false") {
            val value = false.failOnTrueFutureWeak(thr)
            value.r
          }

          test("true") {
            val value = true.failOnTrueFutureWeak(thr)
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

        describe("failOnFalseWeak") {
          test("false") {
            val value = false.failOnFalseFutureWeak(thr)
            assertThrows[RuntimeException](value.r)
          }

          test("true") {
            val value = true.failOnFalseFutureWeak(thr)
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
              Future.pure(none).flattenOption(ano).r
            }
          }

          test("pure — some") {
            assert(Future.pure(some).flattenOption(ano).r == 42)
          }

          test("fail") {
            assertThrows[RuntimeException] {
              Future.failWeak[Option[Int]](thr).flattenOption(ano).r
            }
          }
        }

        describe("flattenOptionWeak") {

          test("pure — none") {
            assertThrows[RuntimeException] {
              Future.pure(none).flattenOptionWeak(thr).r
            }
          }

          test("pure — some") {
            assert(Future.pure(some).flattenOptionWeak(thr).r == 42)
          }

          test("fail") {
            assertThrows[InvalidInputFailure] {
              Future.fail[Option[Int]](ano).flattenOptionWeak(thr).r
            }
          }
        }

        describe("flattenResult") {
          test("incorrect") {
            assertThrows[InvalidInputFailure](
              Future.pure(incorrect).flattenResult.r
            )
          }

          test("correct") {
            assert(Future.pure(correct).flattenResult.r == 42)
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

        test("suspendOptionWeak") {
          val f = Future.suspendOptionWeak(
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

        test("suspendEitherWeak") {
          val f = Future.suspendEitherWeak(
            Right[Throwable, String](throw thr)
          )
          assertThrows[RuntimeException](f.r)
        }

        test("suspendEitherWeak — transform") {
          val f = Future.suspendEitherWeak(
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

        describe("effectOnEmpty") {

          test("none") {
            var sideEffect: Int = 0
            val f = Future.effectOnEmpty(
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
            val f = Future.effectOnEmpty(
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

        describe("effectOnSome") {

          test("none") {
            var sideEffect: Int = 0
            val f = Future.effectOnSome(
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
            val f = Future.effectOnSome(
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

        describe("flatEffectOnEmpty") {

          test("none") {
            var sideEffect: Int = 0
            val f = Future.flatEffectOnEmpty(
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
            val f = Future.flatEffectOnEmpty(
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
            val f = Future.flatEffectOnEmpty(
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

        describe("effectOnIncorrect") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f = Future.effectOnIncorrect(
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
            val f = Future.effectOnIncorrect(
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

        describe("effectOnCorrect") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f = Future.effectOnCorrect(
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
            val f = Future.effectOnCorrect(
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

        test("suspendOption") {
          val f = Option(throw thr).suspendInFuture(ano)
          assertThrows[RuntimeException](f.r)
        }

        test("suspendOptionWeak") {
          val f = Option(throw thr).suspendInFuture(ano)
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

        test("suspendEitherWeak") {
          val f = Right[Throwable, String](throw thr).suspendInFutureWeak
          assertThrows[RuntimeException](f.r)
        }

        test("suspendEitherWeak — transform") {
          val f = Right[Throwable, String](throw thr).suspendInFutureWeak(thr2thr)
          assertThrows[RuntimeException](f.r)
        }

        test("suspendResult") {
          val f = Result.pure(throw thr).suspendInFuture
          assertThrows[RuntimeException](f.r)
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

        describe("effectOnEmpty") {

          test("none") {
            var sideEffect: Int = 0
            val f = none.effectOnEmptyFuture(
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
            val f = some.effectOnEmptyFuture(
              Future {
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
            val f = none.effectOnSomeFuture(
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
            val f = some.effectOnSomeFuture(
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

        describe("flatEffectOnEmpty") {

          test("none") {
            var sideEffect: Int = 0
            val f =
              Future
                .pure(none)
                .effectOnEmpty(
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
                .effectOnEmpty(
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
                .effectOnEmpty(
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
                .effectOnSome(
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
                .effectOnSome(
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
              .effectOnSome(
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

        describe("effectOnIncorrect") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f = incorrect.effectOnIncorrectFuture(
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
            val f = correct.effectOnIncorrectFuture(
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

        describe("effectOnCorrect") {

          test("incorrect") {
            var sideEffect: Int = 0
            val f = incorrect.effectOnCorrectFuture(
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
            val f = correct.effectOnCorrectFuture(
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
                .effectOnIncorrect(
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
              .effectOnIncorrect(
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
              .effectOnIncorrect(
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
                .effectOnCorrect(
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
                .effectOnCorrect(
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
                .effectOnCorrect(
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
      }

    }
  }

} //end test
