//package busymachines.effects_test
//
//import busymachines.core._
//import busymachines.effects._
//import busymachines.scalatest.FunSpecAlias
//import org.scalatest._
//
//import scala.util._
//
///**
//  *
//  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
//  * @since 28 Jan 2018
//  *
//  */
//final class TryEffectsTest extends FunSpecAlias with Matchers {
//  implicit val sc: Scheduler = Scheduler.global
//
//  private val throwable: RuntimeException    = new RuntimeException("runtime_exception")
//  private val anomaly:   InvalidInputFailure = InvalidInputFailure("invalid_input_failure")
//
//  private val none: Option[Int] = Option.empty
//  private val some: Option[Int] = Option(42)
//
//  private val incorrect: Result[Int] = Result.fail(anomaly)
//  private val correct:   Result[Int] = Result(42)
//
//  //---------------------------------------------------------------------------
//
//  describe("Try — companion object syntax") {
//
//    describe("constructors") {
//      test("Try.pure + Try.unsafeGet") {
//        assert(Try.unsafeGet(Try.pure(42)) == 42)
//      }
//
//      test("Try.fail") {
//        the[InvalidInputFailure] thrownBy {
//          Try.fail(anomaly).get
//        }
//      }
//
//      test("Try.failWeak") {
//        the[RuntimeException] thrownBy {
//          Try.failWeak(throwable).get
//        }
//      }
//
//      test("Try.unit") {
//        assert(Try.unit == Try.unit)
//      }
//
//      describe("Try.fromResult") {
//        test("correct") {
//          assert(Try.fromResult(correct).get == 42)
//        }
//
//        test("incorrent") {
//          the[InvalidInputFailure] thrownBy {
//            Try.fromResult(incorrect).get
//          }
//        }
//      }
//
//      describe("Try.fromOption") {
//        test("some") {
//          assert(Try.fromOption(some, anomaly).get == 42)
//        }
//
//        test("none") {
//          the[InvalidInputFailure] thrownBy {
//            Try.fromOption(none, anomaly).get
//          }
//        }
//      }
//
//      describe("Try.fromOptionWeak") {
//        test("some") {
//          assert(Try.fromOptionWeak(some, throwable).get == 42)
//        }
//
//        test("none") {
//          the[RuntimeException] thrownBy {
//            Try.fromOptionWeak(none, throwable).get
//          }
//        }
//      }
//    }
//
//    describe("conversion to other effects") {
//      describe("Try.asResult") {
//        test("correct") {
//          assert(Try.asResult(Try.pure(42)) == correct)
//        }
//
//        test("incorrect") {
//          assert(Try.asResult(Try.fail(anomaly)) == incorrect)
//        }
//      }
//
//      describe("Try.asIO") {
//        test("success") {
//          assert(Try.asIO(Try.pure(42)).unsafeRunSync() == 42)
//        }
//
//        test("fail") {
//          the[InvalidInputFailure] thrownBy {
//            Try.asIO(Try.fail(anomaly)).unsafeRunSync()
//          }
//        }
//      }
//
//      describe("Try.asFuture") {
//        test("success") {
//          assert(Try.asFuture(Try.pure(42)).syncUnsafeGet() == 42)
//        }
//
//        test("fail") {
//          the[InvalidInputFailure] thrownBy {
//            Try.asFuture(Try.fail(anomaly)).syncUnsafeGet()
//          }
//        }
//      }
//
//      describe("Try.asTask") {
//        test("success") {
//          assert(Try.asTask(Try.pure(42)).syncUnsafeGet() == 42)
//        }
//
//        test("fail") {
//          the[InvalidInputFailure] thrownBy {
//            Try.asTask(Try.fail(anomaly)).syncUnsafeGet()
//          }
//        }
//      }
//    }
//
//    describe("transformers") {
//
//      describe("Try.bimap") {
//        test("success") {
//          val tr = Try.bimap(
//            Try.pure(42),
//            (i: Int)       => i.toString,
//            (t: Throwable) => anomaly
//          )
//
//          assert(tr.get == "42")
//        }
//
//        test("fail") {
//          val tr = Try.bimap(
//            Try.fail(anomaly),
//            (i: Int)       => i.toString,
//            (t: Throwable) => ForbiddenFailure("forbidden")
//          )
//
//          the[ForbiddenFailure] thrownBy {
//            tr.get
//          }
//        }
//      }
//
//      describe("Try.bimapWeak") {
//        test("success") {
//          val tr = Try.bimapWeak(
//            Try.pure(42),
//            (i: Int)       => i.toString,
//            (t: Throwable) => anomaly
//          )
//
//          assert(tr.get == "42")
//        }
//
//        test("fail") {
//          val tr = Try.bimapWeak(
//            Try.fail(anomaly),
//            (i: Int)       => i.toString,
//            (t: Throwable) => new IllegalArgumentException("illegal_argument")
//          )
//
//          the[IllegalArgumentException] thrownBy {
//            tr.get
//          }
//        }
//      }
//
//      describe("Try.morph") {
//        test("success") {
//          val tr = Try.morph(
//            Try.pure(42),
//            (i: Int)       => i.toString,
//            (t: Throwable) => t.getMessage
//          )
//
//          assert(tr.get == "42")
//        }
//
//        test("fail") {
//          val tr = Try.morph(
//            Try.fail(anomaly),
//            (i: Int)       => i.toString,
//            (t: Throwable) => "recovered_failure"
//          )
//
//          assert(tr.get == "recovered_failure")
//        }
//      }
//    }
//
//    describe("on boolean value") {
//
//      describe("Try.cond") {
//        test("true") {
//          val tr = Try.cond(
//            true,
//            42,
//            anomaly
//          )
//          assert(tr.unsafeGet == 42)
//        }
//
//        test("false") {
//          val tr = Try.cond(
//            false,
//            42,
//            anomaly
//          )
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//      }
//
//      describe("Try.condWeak") {
//        test("true") {
//          val tr = Try.condWeak(
//            true,
//            42,
//            throwable
//          )
//          assert(tr.unsafeGet == 42)
//        }
//
//        test("false") {
//          val tr = Try.condWeak(
//            false,
//            42,
//            throwable
//          )
//          the[RuntimeException] thrownBy {
//            tr.get
//          }
//        }
//      }
//
//      describe("Try.condWithWeak") {
//        test("true") {
//          val tr = Try.condWithWeak(
//            true,
//            Try.pure(42),
//            throwable
//          )
//          assert(tr.unsafeGet == 42)
//        }
//
//        test("false") {
//          val tr = Try.condWithWeak(
//            false,
//            Try.pure(42),
//            throwable
//          )
//          the[RuntimeException] thrownBy {
//            tr.get
//          }
//        }
//      }
//
//      describe("Try.condWith") {
//        test("true") {
//          val tr = Try.condWith(
//            true,
//            Try.pure(11),
//            anomaly
//          )
//          assert(tr.unsafeGet == 11)
//        }
//
//        test("false") {
//          val tr = Try.condWith(
//            false,
//            Try.pure(11),
//            anomaly
//          )
//
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//
//        test("true — failure") {
//          val tr = Try.condWith(
//            true,
//            Try.fail(anomaly),
//            ForbiddenFailure
//          )
//
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//      }
//
//      describe("Try.flatCond") {
//        test("Try(true)") {
//          val tr = Try.flatCond(
//            Try.pure(true),
//            42,
//            anomaly
//          )
//          assert(tr.unsafeGet == 42)
//        }
//
//        test("Try(false)") {
//          val tr = Try.flatCond(
//            Try.pure(false),
//            42,
//            anomaly
//          )
//
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//
//        test("Try — failure") {
//          val tr = Try.flatCond(
//            Try.fail[Boolean](anomaly),
//            42,
//            ForbiddenFailure
//          )
//
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//      }
//
//      describe("Try.flatCondWith") {
//        test("Try(true)") {
//          val tr = Try.flatCondWith(
//            Try.pure(true),
//            Try.pure(42),
//            anomaly
//          )
//          assert(tr.unsafeGet == 42)
//        }
//
//        test("Try(true) — failure") {
//          val tr = Try.flatCondWith(
//            Try.fail(anomaly),
//            Try.pure(42),
//            ForbiddenFailure
//          )
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//
//        test("Try(false)") {
//          val tr = Try.flatCondWith(
//            Try.pure(false),
//            Try.pure(42),
//            anomaly
//          )
//
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//
//        test("Try — failure") {
//          val tr = Try.flatCondWith(
//            Try.fail[Boolean](anomaly),
//            Try.pure(42),
//            ForbiddenFailure
//          )
//
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//      }
//
//      describe("Try.failOnTrue") {
//        test("true") {
//          val tr: Try[Unit] = Try.failOnTrue(
//            true,
//            anomaly
//          )
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//
//        test("false") {
//          val tr: Try[Unit] = Try.failOnTrue(
//            false,
//            anomaly
//          )
//          tr.get //unit
//        }
//      }
//
//      describe("Try.failOnFalse") {
//        test("true") {
//          val tr: Try[Unit] = Try.failOnFalse(
//            true,
//            anomaly
//          )
//          tr.get
//        }
//
//        test("false") {
//          val tr: Try[Unit] = Try.failOnFalse(
//            false,
//            anomaly
//          )
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//      }
//
//      describe("Try.flatFailOnTrue") {
//        test("true") {
//          val tr: Try[Unit] = Try.flatFailOnTrue(
//            Try.pure(true),
//            anomaly
//          )
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//
//        test("false") {
//          val tr: Try[Unit] = Try.flatFailOnTrue(
//            Try.pure(false),
//            anomaly
//          )
//          tr.get //unit
//        }
//      }
//
//      describe("Try.flatFailOnFalse") {
//        test("true") {
//          val tr: Try[Unit] = Try.flatFailOnFalse(
//            Try.pure(true),
//            anomaly
//          )
//          tr.get
//        }
//
//        test("false") {
//          val tr: Try[Unit] = Try.flatFailOnFalse(
//            Try.pure(false),
//            anomaly
//          )
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//      }
//    }
//
//    describe("misc") {
//      test("Try.discardContent") {
//        assert(Try.discardContent(Try.pure(42)) == Try.unit)
//      }
//
//      describe("Try.flattenOption") {
//        test("some") {
//          assert(Try.flattenOption(Try.pure(some), anomaly).get == 42)
//        }
//
//        test("none") {
//          the[InvalidInputFailure] thrownBy {
//            Try.flattenOption(Try.pure(none), anomaly).get
//          }
//
//        }
//      }
//
//      describe("Try.flattenOptionWeak") {
//        test("some") {
//          assert(Try.flattenOptionWeak(Try.pure(some), throwable).get == 42)
//        }
//
//        test("none") {
//          the[RuntimeException] thrownBy {
//            Try.flattenOptionWeak(Try.pure(none), throwable).get
//          }
//
//        }
//      }
//
//      describe("Try.flattenResult") {
//        test("some") {
//          assert(Try.flattenResult(Try.pure(correct)).get == 42)
//        }
//
//        test("none") {
//          the[InvalidInputFailure] thrownBy {
//            Try.flattenResult(Try.pure(incorrect)).get
//          }
//
//        }
//      }
//    }
//
//  }
//
//  describe("Try — infix object syntax") {
//
//    describe("conversion to other effects") {
//      describe(".asResult") {
//        test("correct") {
//          assert(Try.pure(42).asResult == correct)
//        }
//
//        test("incorrect") {
//          assert(Try.fail(anomaly).asResult == incorrect)
//        }
//      }
//
//      describe(".asIO") {
//        test("success") {
//          assert(Try.pure(42).asIO.unsafeRunSync() == 42)
//        }
//
//        test("fail") {
//          the[InvalidInputFailure] thrownBy {
//            Try.fail(anomaly).asIO.unsafeRunSync()
//          }
//        }
//      }
//
//      describe(".asFuture") {
//        test("success") {
//          assert(Try.pure(42).asFuture.syncUnsafeGet() == 42)
//        }
//
//        test("fail") {
//          the[InvalidInputFailure] thrownBy {
//            Try.fail(anomaly).asFuture.syncUnsafeGet()
//          }
//        }
//      }
//
//      describe(".asTask") {
//        test("success") {
//          assert(Try.pure(42).asTask.syncUnsafeGet() == 42)
//        }
//
//        test("fail") {
//          the[InvalidInputFailure] thrownBy {
//            Try.fail(anomaly).asTask.syncUnsafeGet()
//          }
//        }
//      }
//    }
//
//    describe("transformers") {
//
//      describe(".bimap") {
//        test("success") {
//          val tr = Try
//            .pure(42)
//            .bimap(
//              (i: Int)       => i.toString,
//              (t: Throwable) => anomaly
//            )
//
//          assert(tr.get == "42")
//        }
//
//        test("fail") {
//          val tr =
//            Try
//              .fail(anomaly)
//              .bimap(
//                (i: Int)       => i.toString,
//                (t: Throwable) => ForbiddenFailure("forbidden")
//              )
//
//          the[ForbiddenFailure] thrownBy {
//            tr.get
//          }
//        }
//      }
//
//      describe(".bimapWeak") {
//        test("success") {
//          val tr = Try
//            .pure(42)
//            .bimapWeak(
//              (i: Int)       => i.toString,
//              (t: Throwable) => anomaly
//            )
//
//          assert(tr.get == "42")
//        }
//
//        test("fail") {
//          val tr = Try
//            .fail(anomaly)
//            .bimapWeak(
//              (i: Int)       => i.toString,
//              (t: Throwable) => new IllegalArgumentException("illegal_argument")
//            )
//
//          the[IllegalArgumentException] thrownBy {
//            tr.get
//          }
//        }
//      }
//
//      describe(".morph") {
//        test("success") {
//          val tr = Try
//            .pure(42)
//            .morph(
//              (i: Int)       => i.toString,
//              (t: Throwable) => t.getMessage
//            )
//
//          assert(tr.get == "42")
//        }
//
//        test("fail") {
//          val tr = Try
//            .fail(anomaly)
//            .morph(
//              (i: Int)       => i.toString,
//              (t: Throwable) => "recovered_failure"
//            )
//
//          assert(tr.get == "recovered_failure")
//        }
//      }
//    }
//
//    describe("on boolean value") {
//
//      describe(".condTry") {
//        test("true") {
//          val tr = true.condTry(
//            42,
//            anomaly
//          )
//          assert(tr.unsafeGet == 42)
//        }
//
//        test("false") {
//          val tr = false.condTry(
//            42,
//            anomaly
//          )
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//      }
//
//      describe(".condTryWeak") {
//        test("true") {
//          val tr = true.condTryWeak(
//            42,
//            throwable
//          )
//          assert(tr.unsafeGet == 42)
//        }
//
//        test("false") {
//          val tr = false.condTryWeak(
//            42,
//            throwable
//          )
//          the[RuntimeException] thrownBy {
//            tr.get
//          }
//        }
//      }
//
//      describe(".condWithTry") {
//        test("true") {
//          val tr = true.condWithTry(
//            Try.pure(11),
//            anomaly
//          )
//          assert(tr.unsafeGet == 11)
//        }
//
//        test("false") {
//          val tr = false.condWithTry(
//            Try.pure(11),
//            anomaly
//          )
//
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//
//        test("true — failure") {
//          val tr = true.condWithTry(
//            Try.fail(anomaly),
//            ForbiddenFailure
//          )
//
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//      }
//
//      describe(".condWithWeakTry") {
//        test("true") {
//          val tr = true.condWithWeakTry(
//            Try.pure(42),
//            throwable
//          )
//          assert(tr.unsafeGet == 42)
//        }
//
//        test("false") {
//          val tr = false.condWithWeakTry(
//            Try.pure(42),
//            throwable
//          )
//          the[RuntimeException] thrownBy {
//            tr.get
//          }
//        }
//      }
//
//      describe(".cond") {
//        test("Try(true)") {
//          val tr = Try
//            .pure(true)
//            .cond(
//              42,
//              anomaly
//            )
//          assert(tr.unsafeGet == 42)
//        }
//
//        test("Try(false)") {
//          val tr = Try
//            .pure(false)
//            .cond(
//              42,
//              anomaly
//            )
//
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//
//        test("Try — failure") {
//          val tr = Try
//            .fail[Boolean](anomaly)
//            .cond(
//              42,
//              ForbiddenFailure
//            )
//
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//      }
//
//      describe(".condWith") {
//        test("Try(true)") {
//          val tr = Try
//            .pure(true)
//            .condWith(
//              Try.pure(42),
//              anomaly
//            )
//          assert(tr.unsafeGet == 42)
//        }
//
//        test("Try(true) — failure") {
//          val tr = Try
//            .fail(anomaly)
//            .condWith(
//              Try.pure(42),
//              ForbiddenFailure
//            )
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//
//        test("Try(false)") {
//          val tr = Try
//            .pure(false)
//            .condWith(
//              Try.pure(42),
//              anomaly
//            )
//
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//
//        test("Try — failure") {
//          val tr = Try
//            .fail[Boolean](anomaly)
//            .condWith(
//              Try.pure(42),
//              ForbiddenFailure
//            )
//
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//      }
//
//      describe(".failOnTrueTry") {
//        test("true") {
//          val tr: Try[Unit] = true.failOnTrueTry(anomaly)
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//
//        test("false") {
//          val tr: Try[Unit] = false.failOnTrueTry(anomaly)
//          tr.get //unit
//        }
//      }
//
//      describe(".failOnFalseTry") {
//        test("true") {
//          val tr: Try[Unit] = true.failOnFalseTry(anomaly)
//          tr.get
//        }
//
//        test("false") {
//          val tr: Try[Unit] = false.failOnFalseTry(anomaly)
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//      }
//
//      describe(".failOnTrue") {
//        test("true") {
//          val tr: Try[Unit] = Try.pure(true).failOnTrue(anomaly)
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//
//        test("false") {
//          val tr: Try[Unit] = Try.pure(false).failOnTrue(anomaly)
//          tr.get //unit
//        }
//      }
//
//      describe(".failOnFalse") {
//        test("true") {
//          val tr: Try[Unit] = Try.pure(true).failOnFalse(anomaly)
//          tr.get
//        }
//
//        test("false") {
//          val tr: Try[Unit] = Try.pure(false).failOnFalse(anomaly)
//          the[InvalidInputFailure] thrownBy {
//            tr.get
//          }
//        }
//      }
//    }
//
//    describe("misc") {
//      test(".discardContent") {
//        assert(Try.pure(42).discardContent == Try.unit)
//      }
//
//      describe(".flattenOption") {
//        test("some") {
//          assert(Try.pure(some).flattenOption(anomaly).get == 42)
//        }
//
//        test("none") {
//          the[InvalidInputFailure] thrownBy {
//            Try.pure(none).flattenOption(anomaly).get
//          }
//
//        }
//      }
//
//      describe(".flattenWeak") {
//        test("some") {
//          assert(Try.pure(some).flattenOptionWeak(throwable).get == 42)
//        }
//
//        test("none") {
//          the[RuntimeException] thrownBy {
//            Try.pure(none).flattenOptionWeak(throwable).get
//          }
//
//        }
//      }
//
//      describe(".flattenResult") {
//        test("some") {
//          assert(Try.pure(correct).flattenResult.get == 42)
//        }
//
//        test("none") {
//          the[InvalidInputFailure] thrownBy {
//            Try.pure(incorrect).flattenResult.get
//          }
//
//        }
//      }
//    }
//
//  }
//
//}
