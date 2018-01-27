package busymachines.effects_test

import scala.util._
import busymachines.core._, busymachines.effects._
import org.scalatest._

/**
  *
  * We test util methods through syntax, this way we get better test coverage
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 27 Jan 2018
  *
  */
class IOEffectsTest extends FunSpec with Matchers {
  private implicit val sc: Scheduler = Scheduler.global

  private val anomaly:   InvalidInputFailure = InvalidInputFailure("invalid_input_failure")
  private val throwable: RuntimeException    = new RuntimeException("runtime_exception")

  private val failedTryThrowable: Try[Int] = Failure(throwable)
  private val failedTryAnomaly:   Try[Int] = Failure(anomaly)
  private val tryInt:             Try[Int] = scala.util.Success(42)

  private val incorrect: Result[Int] = Result.fail(anomaly)
  private val correct:   Result[Int] = Result(42)

  private val none: Option[Int] = Option.empty
  private val some: Option[Int] = Option(42)

  private def sleep(time: Int = 20): Unit = Thread.sleep(time.toLong)

  describe("IO — companion methods constructors") {
    it("IO.fail") {
      val io = IO.fail(anomaly)
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }
    }

    it("IO.fromTry — success") {
      val io = IO.fromTry(tryInt)
      assert(io.unsafeRunSync() == tryInt.get)
    }

    it("IO.fromTry — failed") {
      val io = IO.fromTry(failedTryAnomaly)
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }

      val io2 = IO.fromTry(failedTryThrowable)
      the[RuntimeException] thrownBy {
        io2.unsafeRunSync()
      }
    }

    it("IO.fromTrySuspend — success") {
      var sideEffect: Int = 0
      val io = IO.fromTrySuspend {
        sideEffect = 42
        tryInt
      }
      if (sideEffect == 42) fail("IO failed to suspend side-effect")
      val v = io.unsafeRunSync()
      assert(v == tryInt.get)
    }

    it("IO.fromTrySuspend — failed") {
      var sideEffect: Int = 0
      val io = IO.fromTrySuspend {
        sideEffect = 42
        failedTryAnomaly
      }
      if (sideEffect == 42) fail("IO did not suspend side-effect properly")
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }

      val io2 = IO.fromTrySuspend(failedTryThrowable)
      the[RuntimeException] thrownBy {
        io2.unsafeRunSync()
      }
    }

    it("IO.fromResult — correct") {
      val io = IO.fromResult(correct)
      assert(io.unsafeRunSync() == 42)
    }

    it("IO.fromResult — incorrect") {
      val io = IO.fromResult(incorrect)
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }
    }

    it("IO.fromResultSuspend — success") {
      var sideEffect: Int = 0
      val io = IO.fromResultSuspend {
        sideEffect = 42
        correct
      }
      if (sideEffect == 42) fail("IO failed to suspend side-effect")
      val v = io.unsafeRunSync()
      assert(v == 42)
    }

    it("IO.fromResultSuspend — incorrect") {
      var sideEffect: Int = 0
      val io = IO.fromResultSuspend {
        sideEffect = 42
        incorrect
      }
      if (sideEffect == 42) fail("IO failed to suspend side-effect")
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }
    }

    it("IO.fromOption — Some") {
      val io = IO.fromOption(some, anomaly)
      assert(io.unsafeRunSync() == some.get)
    }

    it("IO.fromOption — None") {
      val io = IO.fromOption(none, anomaly)
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }
    }

    it("IO.fromOptionWeak — Some") {
      val io = IO.fromOptionWeak(some, throwable)
      assert(io.unsafeRunSync() == some.get)
    }

    it("IO.fromOptionWeak — None") {
      val io = IO.fromOptionWeak(none, throwable)
      the[RuntimeException] thrownBy {
        io.unsafeRunSync()
      }
    }

    it("IO.fromPureFuture — success") {
      val io = IO.fromPureFuture(Future.pure(42))
      assert(io.unsafeRunSync() == 42)
    }

    it("IO.fromPureFuture — failed anomaly") {
      val io = IO.fromPureFuture(Future.fail(anomaly))
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }
    }

    it("IO.fromPureFuture — failed throwable") {
      val io = IO.fromPureFuture(Future.failed(throwable))
      the[RuntimeException] thrownBy {
        io.unsafeRunSync()
      }
    }

    it("IO.fromFutureSuspend — suspend side-effects") {
      var sideEffect: Int = 0
      val io: IO[Int] = IO.fromFutureSuspend {
        Future {
          sideEffect = 42
          sideEffect
        }
      }
      sleep()
      if (sideEffect == 42) fail("IO did not properly suspend future's side effects")
      val value = io.unsafeRunSync()
      assert(value == 42,      "the resulting value of the IO is wrong")
      assert(sideEffect == 42, "the side effect ( have been applied after running IO")
    }

    it("IO.fromTask — suspend side-effects") {
      var sideEffect: Int = 0
      val io: IO[Int] = IO.fromTask {
        Task {
          sideEffect = 42
          sideEffect
        }
      }
      sleep()
      if (sideEffect == 42) fail("IO did not properly suspend side effects")
      val value = io.unsafeRunSync()
      assert(value == 42,      "the resulting value of the IO is wrong")
      assert(sideEffect == 42, "the side effect ( have been applied after running IO")
    }
  }

  describe("IO — companion methods IO to IO") {

    it("IO.cond — true — w/ side-effects") {
      var sideEffect: Int = 0
      val io = IO.cond(
        true, {
          sideEffect = 42
          sideEffect
        },
        anomaly
      )
      if (sideEffect == 42) fail("IO did not properly suspend side effects")
      val value = io.unsafeRunSync()
      assert(value == 42,      "the resulting value of the IO is wrong")
      assert(sideEffect == 42, "the side effect ( have been applied after running IO")
    }

    it("IO.cond — false — w/ side-effects") {
      var sideEffect: Int = 0
      val io = IO.cond(
        false, {
          sideEffect = 42
          sideEffect
        },
        anomaly
      )
      if (sideEffect == 42) fail("IO did not properly suspend side effects")
      the[InvalidInputFailure] thrownBy (
        io.unsafeRunSync()
      )
    }

    it("IO.condWith — true — w/ side-effects") {
      var sideEffect: Int = 0
      val io = IO.condWith(
        true,
        IO {
          sideEffect = 42
          sideEffect
        },
        anomaly
      )
      if (sideEffect == 42) fail("IO did not properly suspend side effects")
      val value = io.unsafeRunSync()
      assert(value == 42,      "the resulting value of the IO is wrong")
      assert(sideEffect == 42, "the side effect ( have been applied after running IO")
    }

    it("IO.condWith — false — w/ side-effects") {
      var sideEffect: Int = 0
      val io = IO.condWith(
        false,
        IO {
          sideEffect = 42
          sideEffect
        },
        anomaly
      )
      if (sideEffect == 42) fail("IO did not properly suspend side effects")
      the[InvalidInputFailure] thrownBy (
        io.unsafeRunSync()
      )
    }

    it("IO.failOnTrue — true") {
      val io = IO.failOnTrue(true, anomaly)
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }
    }

    it("IO.failOnTrue — false") {
      val io = IO.failOnTrue(false, anomaly)
      io.unsafeRunSync()
    }

    it("IO.failOnFalse — true") {
      val io = IO.failOnFalse(true, anomaly)
      io.unsafeRunSync()
    }

    it("IO.failOnFalse — false") {
      val io = IO.failOnFalse(false, anomaly)
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }
    }

    //===========================================

    it("IO.flatCond — true — w/ side-effects") {
      var sideEffect: Int = 0
      val io = IO.flatCond(
        IO(true), {
          sideEffect = 42
          sideEffect
        },
        anomaly
      )
      if (sideEffect == 42) fail("IO did not properly suspend side effects")
      val value = io.unsafeRunSync()
      assert(value == 42,      "the resulting value of the IO is wrong")
      assert(sideEffect == 42, "the side effect ( have been applied after running IO")
    }

    it("IO.flatCond — false — w/ side-effects") {
      var sideEffect: Int = 0
      val io = IO.flatCond(
        IO(false), {
          sideEffect = 42
          sideEffect
        },
        anomaly
      )
      if (sideEffect == 42) fail("IO did not properly suspend side effects")
      the[InvalidInputFailure] thrownBy (
        io.unsafeRunSync()
      )
    }

    it("IO.flatCondWith — true — w/ side-effects") {
      var sideEffect: Int = 0
      val io = IO.flatCondWith(
        IO(true),
        IO {
          sideEffect = 42
          sideEffect
        },
        anomaly
      )
      if (sideEffect == 42) fail("IO did not properly suspend side effects")
      val value = io.unsafeRunSync()
      assert(value == 42,      "the resulting value of the IO is wrong")
      assert(sideEffect == 42, "the side effect ( have been applied after running IO")
    }

    it("IO.flatCondWith — false — w/ side-effects") {
      var sideEffect: Int = 0
      val io = IO.flatCondWith(
        IO(false),
        IO {
          sideEffect = 42
          sideEffect
        },
        anomaly
      )
      if (sideEffect == 42) fail("IO did not properly suspend side effects")
      the[InvalidInputFailure] thrownBy (
        io.unsafeRunSync()
      )
    }

    it("IO.flatFailOnTrue — true") {
      val io = IO.flatFailOnTrue(IO(true), anomaly)
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }
    }

    it("IO.flatFailOnTrue — false") {
      val io = IO.flatFailOnTrue(IO(false), anomaly)
      io.unsafeRunSync()
    }

    it("IO.flatFailOnFalse — true") {
      val io = IO.flatFailOnFalse(IO(true), anomaly)
      io.unsafeRunSync()
    }

    it("IO.flatFailOnFalse — false") {
      val io = IO.flatFailOnFalse(IO(false), anomaly)
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }
    }

    it("IO.effectOnTrue — true") {
      var sideEffect: Int = 0
      val io: IO[Unit] = IO.effectOnTrue(
        true,
        IO {
          sideEffect = 42
        }
      )
      if (sideEffect == 42) fail("IO did not suspend side-effect properly")
      io.unsafeRunSync()
      assert(sideEffect == 42)
    }

    it("IO.effectOnTrue — false") {
      var sideEffect: Int = 0
      val io: IO[Unit] = IO.effectOnTrue(
        false,
        IO {
          sideEffect = 42
        }
      )
      if (sideEffect == 42) fail("IO did not suspend side-effect properly")
      io.unsafeRunSync()
      assert(sideEffect == 0, "side effect ( have never been executed")
    }

    it("IO.effectOnFalse — true") {
      var sideEffect: Int = 0
      val io: IO[Unit] = IO.effectOnFalse(
        true,
        IO {
          sideEffect = 42
        }
      )
      if (sideEffect == 42) fail("IO did not suspend side-effect properly")
      io.unsafeRunSync()
      assert(sideEffect == 0, "side effect ( have never been executed")
    }

    it("IO.effectOnFalse — false") {
      var sideEffect: Int = 0
      val io: IO[Unit] = IO.effectOnFalse(
        false,
        IO {
          sideEffect = 42
        }
      )
      if (sideEffect == 42) fail("IO did not suspend side-effect properly")
      io.unsafeRunSync()
      assert(sideEffect == 42)
    }

    it("IO.flatEffectOnTrue — true") {
      var sideEffect: Int = 0
      val io: IO[Unit] = IO.flatEffectOnTrue(
        IO(true),
        IO {
          sideEffect = 42
        }
      )
      if (sideEffect == 42) fail("IO did not suspend side-effect properly")
      io.unsafeRunSync()
      assert(sideEffect == 42)
    }

    it("IO.flatEffectOnTrue — false") {
      var sideEffect: Int = 0
      val io: IO[Unit] = IO.flatEffectOnTrue(
        IO(false),
        IO {
          sideEffect = 42
        }
      )
      if (sideEffect == 42) fail("IO did not suspend side-effect properly")
      io.unsafeRunSync()
      assert(sideEffect == 0, "side effect ( have never been executed")
    }

    it("IO.flatEffectOnFalse — true") {
      var sideEffect: Int = 0
      val io: IO[Unit] = IO.flatEffectOnFalse(
        IO(true),
        IO {
          sideEffect = 42
        }
      )
      if (sideEffect == 42) fail("IO did not suspend side-effect properly")
      io.unsafeRunSync()
      assert(sideEffect == 0, "side effect ( have never been executed")
    }

    it("IO.flatEffectOnFalse — false") {
      var sideEffect: Int = 0
      val io: IO[Unit] = IO.flatEffectOnFalse(
        IO(false),
        IO {
          sideEffect = 42
        }
      )
      if (sideEffect == 42) fail("IO did not suspend side-effect properly")
      io.unsafeRunSync()
      assert(sideEffect == 42)
    }

    it("IO.discardContent — but still apply side effects") {
      var sideEffect: Int = 0
      val ioInt: IO[Int] = IO {
        sideEffect = 42
        sideEffect
      }

      val ioUnit = IO.discardContent(ioInt)
      ioUnit.unsafeRunSync()
      assert(sideEffect == 42)
    }

    it("IO.optionFlatten - some") {
      val io     = IO(some)
      val flatIO = IO.optionFlatten(io, anomaly)

      assert(flatIO.unsafeRunSync() == 42)
    }

    it("IO.optionFlatten - none") {
      val io     = IO(none)
      val flatIO = IO.optionFlatten(io, anomaly)
      the[InvalidInputFailure] thrownBy {
        flatIO.unsafeRunSync()
      }
    }

    it("IO.optionFlattenWeak - some") {
      val io     = IO(some)
      val flatIO = IO.optionFlattenWeak(io, throwable)

      assert(flatIO.unsafeRunSync() == 42)
    }

    it("IO.optionFlattenWeak - none") {
      val io     = IO(none)
      val flatIO = IO.optionFlattenWeak(io, throwable)
      the[RuntimeException] thrownBy {
        flatIO.unsafeRunSync()
      }
    }

    it("IO.optionFlattenWeak - correct") {
      val io     = IO(Result(42))
      val flatIO = IO.resultFlatten(io)
      assert(flatIO.unsafeRunSync() == 42)
    }

    it("IO.optionFlattenWeak - incorrect") {
      val io     = IO(Result.fail(anomaly))
      val flatIO = IO.resultFlatten(io)
      the[InvalidInputFailure] thrownBy {
        flatIO.unsafeRunSync()
      }
    }
  }

  describe("IO — companion methods IO to X") {
    it("IO.asTask") {
      val task = IO.asTask(IO(42))
      assert(task.toIO.unsafeRunSync() == 42)
    }

    it("IO.asFuture") {
      val f = IO.asFuture(IO(42))
      assert(f.syncUnsafeGet() == 42)
    }

    it("IO.asResult — correct") {
      val ioResult = IO.asResult(IO(42))
      assert(ioResult.unsafeRunSync() == Correct(42))
    }

    it("IO.asResult — incorrect — anomaly") {
      val ioResult = IO.asResult(IO.fail(anomaly))
      the[InvalidInputFailure] thrownBy {
        ioResult.unsafeRunSync().unsafeGet
      }
    }

    it("IO.syncUnsafeResult — correct") {
      val io = IO.syncUnsafeAsResult(IO(42))
      assert(io == Correct(42))
    }

    it("IO.syncUnsafeResult — incorrect") {
      val io = IO.syncUnsafeAsResult(IO.fail(anomaly))
      assert(io.isLeft)
    }

    it("IO.syncUnsafeGet") {
      val io = IO.syncUnsafeGet(IO(42))
      assert(io == 42)
    }
  }

  describe("IO — companion methods transformers") {
    it("IO.bimap — good") {
      val io = IO.bimap(
        IO(42),
        (i: Int)       => i.toString,
        (t: Throwable) => InvalidInputFailure(t)
      )
      assert(io.unsafeRunSync() == "42")
    }

    it("IO.bimap — bad") {
      val io = IO.bimap(
        IO.fail(anomaly),
        (i: Int) => i.toString,
        (t: Throwable) =>
          t match {
            case i: InvalidInputFailure => i
            case _ => fail("got different throwable than put inside")
        }
      )
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }
    }

    it("IO.bimapWeak — good") {
      val io = IO.bimapWeak(
        IO(42),
        (i: Int)       => i.toString,
        (t: Throwable) => InvalidInputFailure(t)
      )
      assert(io.unsafeRunSync() == "42")
    }

    it("IO.bimapWeak — bad") {
      val io = IO.bimapWeak(
        IO.fail(anomaly),
        (i: Int) => i.toString,
        (t: Throwable) =>
          t match {
            case i: InvalidInputFailure => i
            case _ => fail("got different throwable than put inside")
        }
      )
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }
    }

    it("IO.morph — good") {
      val io = IO.morph(
        IO(42),
        (i: Int)       => i.toString,
        (t: Throwable) => t.getMessage
      )
      assert(io.unsafeRunSync() == "42")
    }

    it("IO.morph — bad") {
      val io = IO.morph(
        IO.fail(anomaly),
        (i: Int) => i.toString,
        (t: Throwable) =>
          t match {
            case i: InvalidInputFailure => i.getMessage
            case _ => fail("got different throwable than put inside")
        }
      )
      assert(io.unsafeRunSync() == anomaly.getMessage)
    }
  }

  describe("IO — syntax on simple IO[T] objects") {
    it("IO.asTask") {
      val task = IO(42).asTask
      assert(task.toIO.unsafeRunSync() == 42)
    }

    it("IO.asFuture") {
      val f = IO(42).asFuture
      assert(f.syncUnsafeGet() == 42)
    }

    it("IO.asResult — correct") {
      val ioResult = IO(42).asResult
      assert(ioResult.unsafeRunSync() == Correct(42))
    }

    it("IO.asResult — incorrect — anomaly") {
      val ioResult = IO.fail(anomaly).asResult
      the[InvalidInputFailure] thrownBy {
        ioResult.unsafeRunSync().unsafeGet
      }
    }

    it("IO.syncUnsafeResult — correct") {
      val io = IO(42).syncUnsafeAsResult
      assert(io == Correct(42))
    }

    it("IO.syncUnsafeResult — incorrect") {
      val io = IO.fail(anomaly).syncUnsafeAsResult
      assert(io.isLeft)
    }

    it("IO.syncUnsafeGet") {
      val io = IO(42).syncUnsafeGet
      assert(io == 42)
    }

    it("IO.discardContent — but still apply side effects") {
      var sideEffect: Int = 0
      val ioInt: IO[Int] = IO {
        sideEffect = 42
        sideEffect
      }

      val ioUnit = ioInt.discardContent
      ioUnit.unsafeRunSync()
      assert(sideEffect == 42)
    }

    it("IO.bimap — good") {
      val io =
        IO(42).bimap(
          (i: Int)       => i.toString,
          (t: Throwable) => InvalidInputFailure(t)
        )
      assert(io.unsafeRunSync() == "42")
    }

    it("IO.bimap — bad") {
      val io = IO
        .fail(anomaly)
        .bimap(
          (i: Int) => i.toString,
          (t: Throwable) =>
            t match {
              case i: InvalidInputFailure => i
              case _ => fail("got different throwable than put inside")
          }
        )
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }
    }

    it("IO.bimapWeak — good") {
      val io = IO(42).bimapWeak(
        (i: Int)       => i.toString,
        (t: Throwable) => InvalidInputFailure(t)
      )
      assert(io.unsafeRunSync() == "42")
    }

    it("IO.bimapWeak — bad") {
      val io = IO
        .fail(anomaly)
        .bimapWeak(
          (i: Int) => i.toString,
          (t: Throwable) =>
            t match {
              case i: InvalidInputFailure => i
              case _ => fail("got different throwable than put inside")
          }
        )
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }
    }

    it("IO.morph — good") {
      val io = IO(42).morph(
        (i: Int)       => i.toString,
        (t: Throwable) => t.getMessage
      )
      assert(io.unsafeRunSync() == "42")
    }

    it("IO.morph — bad") {
      val io = IO
        .fail(anomaly)
        .morph(
          (i: Int) => i.toString,
          (t: Throwable) =>
            t match {
              case i: InvalidInputFailure => i.getMessage
              case _ => fail("got different throwable than put inside")
          }
        )
      assert(io.unsafeRunSync() == anomaly.getMessage)
    }

  }

  describe("IO — syntax on Boolean") {
    it("IO.condIO — true") {
      val io = true.condIO(
        42,
        anomaly
      )
      assert(io.unsafeRunSync() == 42)
    }

    it("IO.condIO — false") {
      val io = false.condIO(
        42,
        anomaly
      )
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }
    }

    it("IO.condWithIO — true") {
      val io = true.condWithIO(
        IO(42),
        anomaly
      )
      assert(io.unsafeRunSync() == 42)
    }

    it("IO.condWithIO — false") {
      val io = false.condWithIO(
        IO(42),
        anomaly
      )
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }
    }

    it("IO.failOnTrueIO — true") {
      val io = true.failOnTrueIO(anomaly)
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }
    }

    it("IO.failOnTrueIO — false") {
      val io = false.failOnTrueIO(anomaly)
      io.unsafeRunSync()
    }

    it("IO.failOnFalseIO — true") {
      val io = true.failOnFalseIO(anomaly)
      io.unsafeRunSync()
    }

    it("IO.failOnFalseIO — false") {
      val io = false.failOnFalseIO(anomaly)
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }
    }

    it("IO.effectOnTrueIO — true") {
      var sideEffect: Int = 0
      val io = true.effectOnTrueIO {
        IO {
          sideEffect = 42
        }
      }
      if (sideEffect == 42) fail("IO did not defer side-effect")
      io.unsafeRunSync()
      assert(sideEffect == 42)
    }

    it("IO.effectOnTrueIO — false") {
      var sideEffect: Int = 0
      val io = false.effectOnTrueIO {
        IO {
          sideEffect = 42
        }
      }
      if (sideEffect == 42) fail("IO did not defer side-effect")
      io.unsafeRunSync()
      assert(sideEffect == 0, "side effect should have not been applied")
    }

    it("IO.effectOnFalseIO — false") {
      var sideEffect: Int = 0
      val io = false.effectOnFalseIO {
        IO {
          sideEffect = 42
        }
      }
      if (sideEffect == 42) fail("IO did not defer side-effect")
      io.unsafeRunSync()
      assert(sideEffect == 42)
    }

    it("IO.effectOnFalseIO — true") {
      var sideEffect: Int = 0
      val io = true.effectOnFalseIO {
        IO {
          sideEffect = 42
        }
      }
      if (sideEffect == 42) fail("IO did not defer side-effect")
      io.unsafeRunSync()
      assert(sideEffect == 0, "side effect should have not been applied")
    }
  }

  describe("IO — syntax on IO[Boolean]") {
    it("IO.cond — true") {
      val io = IO(true).cond(
        42,
        anomaly
      )
      assert(io.unsafeRunSync() == 42)
    }

    it("IO.cond — false") {
      val io = IO(false).cond(
        42,
        anomaly
      )
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }
    }

    it("IO.condWith — true") {
      val io = IO(true).condWith(
        IO(42),
        anomaly
      )
      assert(io.unsafeRunSync() == 42)
    }

    it("IO.condWith — false") {
      val io = IO(false).condWith(
        IO(42),
        anomaly
      )
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }
    }

    it("IO.failOnTrue — true") {
      val io = IO(true).failOnTrue(anomaly)
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }
    }

    it("IO.failOnTrue — false") {
      val io = IO(false).failOnTrue(anomaly)
      io.unsafeRunSync()
    }

    it("IO.failOnFalse — true") {
      val io = IO(true).failOnFalse(anomaly)
      io.unsafeRunSync()
    }

    it("IO.failOnFalse — false") {
      val io = IO(false).failOnFalse(anomaly)
      the[InvalidInputFailure] thrownBy {
        io.unsafeRunSync()
      }
    }

    it("IO.effectOnTrue — true") {
      var sideEffect: Int = 0
      val io = IO(true).effectOnTrue {
        IO {
          sideEffect = 42
        }
      }
      if (sideEffect == 42) fail("IO did not defer side-effect")
      io.unsafeRunSync()
      assert(sideEffect == 42)
    }

    it("IO.effectOnTrue — false") {
      var sideEffect: Int = 0
      val io = IO(false).effectOnTrue {
        IO {
          sideEffect = 42
        }
      }
      if (sideEffect == 42) fail("IO did not defer side-effect")
      io.unsafeRunSync()
      assert(sideEffect == 0, "side effect should have not been applied")
    }

    it("IO.effectOnFalse — false") {
      var sideEffect: Int = 0
      val io = IO(false).effectOnFalse {
        IO {
          sideEffect = 42
        }
      }
      if (sideEffect == 42) fail("IO did not defer side-effect")
      io.unsafeRunSync()
      assert(sideEffect == 42)
    }

    it("IO.effectOnFalse — true") {
      var sideEffect: Int = 0
      val io = IO(true).effectOnFalse {
        IO {
          sideEffect = 42
        }
      }
      if (sideEffect == 42) fail("IO did not defer side-effect")
      io.unsafeRunSync()
      assert(sideEffect == 0, "side effect should have not been applied")
    }
  }

  describe("IO — syntax IO[Option]") {
    it("IO[Option].flatten - some") {
      val flatIO = IO(some).flatten(anomaly)

      assert(flatIO.unsafeRunSync() == 42)
    }

    it("IO[Option].flatten - none") {
      val flatIO = IO(none).flatten(anomaly)
      the[InvalidInputFailure] thrownBy {
        flatIO.unsafeRunSync()
      }
    }

    it("IO[Option].flattenWeak - some") {
      val flatIO = IO(some).flattenWeak(throwable)

      assert(flatIO.unsafeRunSync() == 42)
    }

    it("IO[Option].flattenWeak - none") {
      val flatIO = IO(none).flattenWeak(throwable)
      the[RuntimeException] thrownBy {
        flatIO.unsafeRunSync()
      }
    }
  }
}
