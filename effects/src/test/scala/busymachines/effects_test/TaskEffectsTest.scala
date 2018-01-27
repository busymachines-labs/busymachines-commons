package busymachines.effects_test

import busymachines.core._
import busymachines.effects._
import org.scalatest._

import scala.util._

/**
  *
  * We test util methods through syntax, this way we get better test coverage
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 27 Jan 2018
  *
  */
class TaskEffectsTest extends FunSpec with Matchers {
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

  describe("Task — companion methods constructors") {
    it("Task.fail") {
      val io = Task.fail(anomaly)
      the[InvalidInputFailure] thrownBy {
        io.syncUnsafeGet()
      }
    }

    it("Task.fromTry — success") {
      val io = Task.fromTry(tryInt)
      assert(io.syncUnsafeGet() == tryInt.get)
    }

    it("Task.fromTry — failed") {
      val io = Task.fromTry(failedTryAnomaly)
      the[InvalidInputFailure] thrownBy {
        io.syncUnsafeGet()
      }

      val io2 = Task.fromTry(failedTryThrowable)
      the[RuntimeException] thrownBy {
        io2.syncUnsafeGet()
      }
    }

    it("Task.fromTrySuspend — success") {
      var sideEffect: Int = 0
      val io = Task.fromTrySuspend {
        sideEffect = 42
        tryInt
      }
      if (sideEffect == 42) fail("Task failed to suspend side-effect")
      val v = io.syncUnsafeGet()
      assert(v == tryInt.get)
    }

    it("Task.fromTrySuspend — failed") {
      var sideEffect: Int = 0
      val io = Task.fromTrySuspend {
        sideEffect = 42
        failedTryAnomaly
      }
      if (sideEffect == 42) fail("Task did not suspend side-effect properly")
      the[InvalidInputFailure] thrownBy {
        io.syncUnsafeGet()
      }

      val io2 = Task.fromTrySuspend(failedTryThrowable)
      the[RuntimeException] thrownBy {
        io2.syncUnsafeGet()
      }
    }

    it("Task.fromResult — correct") {
      val io = Task.fromResult(correct)
      assert(io.syncUnsafeGet() == 42)
    }

    it("Task.fromResult — incorrect") {
      val io = Task.fromResult(incorrect)
      the[InvalidInputFailure] thrownBy {
        io.syncUnsafeGet()
      }
    }

    it("Task.fromResultSuspend — success") {
      var sideEffect: Int = 0
      val io = Task.fromResultSuspend {
        sideEffect = 42
        correct
      }
      if (sideEffect == 42) fail("Task failed to suspend side-effect")
      val v = io.syncUnsafeGet()
      assert(v == 42)
    }

    it("Task.fromResultSuspend — incorrect") {
      var sideEffect: Int = 0
      val io = Task.fromResultSuspend {
        sideEffect = 42
        incorrect
      }
      if (sideEffect == 42) fail("Task failed to suspend side-effect")
      the[InvalidInputFailure] thrownBy {
        io.syncUnsafeGet()
      }
    }

    it("Task.fromOption — Some") {
      val io = Task.fromOption(some, anomaly)
      assert(io.syncUnsafeGet() == some.get)
    }

    it("Task.fromOption — None") {
      val io = Task.fromOption(none, anomaly)
      the[InvalidInputFailure] thrownBy {
        io.syncUnsafeGet()
      }
    }

    it("Task.fromOptionWeak — Some") {
      val io = Task.fromOptionWeak(some, throwable)
      assert(io.syncUnsafeGet() == some.get)
    }

    it("Task.fromOptionWeak — None") {
      val io = Task.fromOptionWeak(none, throwable)
      the[RuntimeException] thrownBy {
        io.syncUnsafeGet()
      }
    }

    it("Task.fromPureFuture — success") {
      val io = Task.fromPureFuture(Future.pure(42))
      assert(io.syncUnsafeGet() == 42)
    }

    it("Task.fromPureFuture — failed anomaly") {
      val io = Task.fromPureFuture(Future.fail(anomaly))
      the[InvalidInputFailure] thrownBy {
        io.syncUnsafeGet()
      }
    }

    it("Task.fromPureFuture — failed throwable") {
      val io = Task.fromPureFuture(Future.failed(throwable))
      the[RuntimeException] thrownBy {
        io.syncUnsafeGet()
      }
    }

    it("Task.fromFutureSuspend — suspend side-effects") {
      var sideEffect: Int = 0
      val io: Task[Int] = Task.fromFutureSuspend {
        Future {
          sideEffect = 42
          sideEffect
        }
      }
      sleep()
      if (sideEffect == 42) fail("Task did not properly suspend future's side effects")
      val value = io.syncUnsafeGet()
      assert(value == 42,      "the resulting value of the Task is wrong")
      assert(sideEffect == 42, "the side effect ( have been applied after running Task")
    }

    it("Task.fromTask — suspend side-effects") {
      var sideEffect: Int = 0
      val io: Task[Int] = Task.fromIO {
        IO {
          sideEffect = 42
          sideEffect
        }
      }
      sleep()
      if (sideEffect == 42) fail("Task did not properly suspend side effects")
      val value = io.syncUnsafeGet()
      assert(value == 42,      "the resulting value of the Task is wrong")
      assert(sideEffect == 42, "the side effect ( have been applied after running Task")
    }
  }

  describe("Task — companion methods Task to Task") {

    it("Task.cond — true — w/ side-effects") {
      var sideEffect: Int = 0
      val io = Task.cond(
        true, {
          sideEffect = 42
          sideEffect
        },
        anomaly
      )
      if (sideEffect == 42) fail("Task did not properly suspend side effects")
      val value = io.syncUnsafeGet()
      assert(value == 42,      "the resulting value of the Task is wrong")
      assert(sideEffect == 42, "the side effect ( have been applied after running Task")
    }

    it("Task.cond — false — w/ side-effects") {
      var sideEffect: Int = 0
      val io = Task.cond(
        false, {
          sideEffect = 42
          sideEffect
        },
        anomaly
      )
      if (sideEffect == 42) fail("Task did not properly suspend side effects")
      the[InvalidInputFailure] thrownBy (
        io.syncUnsafeGet()
      )
    }

    it("Task.condWith — true — w/ side-effects") {
      var sideEffect: Int = 0
      val io = Task.condWith(
        true,
        Task {
          sideEffect = 42
          sideEffect
        },
        anomaly
      )
      if (sideEffect == 42) fail("Task did not properly suspend side effects")
      val value = io.syncUnsafeGet()
      assert(value == 42,      "the resulting value of the Task is wrong")
      assert(sideEffect == 42, "the side effect ( have been applied after running Task")
    }

    it("Task.condWith — false — w/ side-effects") {
      var sideEffect: Int = 0
      val io = Task.condWith(
        false,
        Task {
          sideEffect = 42
          sideEffect
        },
        anomaly
      )
      if (sideEffect == 42) fail("Task did not properly suspend side effects")
      the[InvalidInputFailure] thrownBy (
        io.syncUnsafeGet()
      )
    }

    it("Task.failOnTrue — true") {
      val io = Task.failOnTrue(true, anomaly)
      the[InvalidInputFailure] thrownBy {
        io.syncUnsafeGet()
      }
    }

    it("Task.failOnTrue — false") {
      val io = Task.failOnTrue(false, anomaly)
      io.syncUnsafeGet()
    }

    it("Task.failOnFalse — true") {
      val io = Task.failOnFalse(true, anomaly)
      io.syncUnsafeGet()
    }

    it("Task.failOnFalse — false") {
      val io = Task.failOnFalse(false, anomaly)
      the[InvalidInputFailure] thrownBy {
        io.syncUnsafeGet()
      }
    }

    //===========================================

    it("Task.flatCond — true — w/ side-effects") {
      var sideEffect: Int = 0
      val io = Task.flatCond(
        Task(true), {
          sideEffect = 42
          sideEffect
        },
        anomaly
      )
      if (sideEffect == 42) fail("Task did not properly suspend side effects")
      val value = io.syncUnsafeGet()
      assert(value == 42,      "the resulting value of the Task is wrong")
      assert(sideEffect == 42, "the side effect ( have been applied after running Task")
    }

    it("Task.flatCond — false — w/ side-effects") {
      var sideEffect: Int = 0
      val io = Task.flatCond(
        Task(false), {
          sideEffect = 42
          sideEffect
        },
        anomaly
      )
      if (sideEffect == 42) fail("Task did not properly suspend side effects")
      the[InvalidInputFailure] thrownBy (
        io.syncUnsafeGet()
      )
    }

    it("Task.flatCondWith — true — w/ side-effects") {
      var sideEffect: Int = 0
      val io = Task.flatCondWith(
        Task(true),
        Task {
          sideEffect = 42
          sideEffect
        },
        anomaly
      )
      if (sideEffect == 42) fail("Task did not properly suspend side effects")
      val value = io.syncUnsafeGet()
      assert(value == 42,      "the resulting value of the Task is wrong")
      assert(sideEffect == 42, "the side effect ( have been applied after running Task")
    }

    it("Task.flatCondWith — false — w/ side-effects") {
      var sideEffect: Int = 0
      val io = Task.flatCondWith(
        Task(false),
        Task {
          sideEffect = 42
          sideEffect
        },
        anomaly
      )
      if (sideEffect == 42) fail("Task did not properly suspend side effects")
      the[InvalidInputFailure] thrownBy (
        io.syncUnsafeGet()
      )
    }

    it("Task.flatFailOnTrue — true") {
      val io = Task.flatFailOnTrue(Task(true), anomaly)
      the[InvalidInputFailure] thrownBy {
        io.syncUnsafeGet()
      }
    }

    it("Task.flatFailOnTrue — false") {
      val io = Task.flatFailOnTrue(Task(false), anomaly)
      io.syncUnsafeGet()
    }

    it("Task.flatFailOnFalse — true") {
      val io = Task.flatFailOnFalse(Task(true), anomaly)
      io.syncUnsafeGet()
    }

    it("Task.flatFailOnFalse — false") {
      val io = Task.flatFailOnFalse(Task(false), anomaly)
      the[InvalidInputFailure] thrownBy {
        io.syncUnsafeGet()
      }
    }

    it("Task.effectOnTrue — true") {
      var sideEffect: Int = 0
      val io: Task[Unit] = Task.effectOnTrue(
        true,
        Task {
          sideEffect = 42
        }
      )
      if (sideEffect == 42) fail("Task did not suspend side-effect properly")
      io.syncUnsafeGet()
      assert(sideEffect == 42)
    }

    it("Task.effectOnTrue — false") {
      var sideEffect: Int = 0
      val io: Task[Unit] = Task.effectOnTrue(
        false,
        Task {
          sideEffect = 42
        }
      )
      if (sideEffect == 42) fail("Task did not suspend side-effect properly")
      io.syncUnsafeGet()
      assert(sideEffect == 0, "side effect ( have never been executed")
    }

    it("Task.effectOnFalse — true") {
      var sideEffect: Int = 0
      val io: Task[Unit] = Task.effectOnFalse(
        true,
        Task {
          sideEffect = 42
        }
      )
      if (sideEffect == 42) fail("Task did not suspend side-effect properly")
      io.syncUnsafeGet()
      assert(sideEffect == 0, "side effect ( have never been executed")
    }

    it("Task.effectOnFalse — false") {
      var sideEffect: Int = 0
      val io: Task[Unit] = Task.effectOnFalse(
        false,
        Task {
          sideEffect = 42
        }
      )
      if (sideEffect == 42) fail("Task did not suspend side-effect properly")
      io.syncUnsafeGet()
      assert(sideEffect == 42)
    }

    it("Task.flatEffectOnTrue — true") {
      var sideEffect: Int = 0
      val io: Task[Unit] = Task.flatEffectOnTrue(
        Task(true),
        Task {
          sideEffect = 42
        }
      )
      if (sideEffect == 42) fail("Task did not suspend side-effect properly")
      io.syncUnsafeGet()
      assert(sideEffect == 42)
    }

    it("Task.flatEffectOnTrue — false") {
      var sideEffect: Int = 0
      val io: Task[Unit] = Task.flatEffectOnTrue(
        Task(false),
        Task {
          sideEffect = 42
        }
      )
      if (sideEffect == 42) fail("Task did not suspend side-effect properly")
      io.syncUnsafeGet()
      assert(sideEffect == 0, "side effect ( have never been executed")
    }

    it("Task.flatEffectOnFalse — true") {
      var sideEffect: Int = 0
      val io: Task[Unit] = Task.flatEffectOnFalse(
        Task(true),
        Task {
          sideEffect = 42
        }
      )
      if (sideEffect == 42) fail("Task did not suspend side-effect properly")
      io.syncUnsafeGet()
      assert(sideEffect == 0, "side effect ( have never been executed")
    }

    it("Task.flatEffectOnFalse — false") {
      var sideEffect: Int = 0
      val io: Task[Unit] = Task.flatEffectOnFalse(
        Task(false),
        Task {
          sideEffect = 42
        }
      )
      if (sideEffect == 42) fail("Task did not suspend side-effect properly")
      io.syncUnsafeGet()
      assert(sideEffect == 42)
    }

    it("Task.discardContent — but still apply side effects") {
      var sideEffect: Int = 0
      val ioInt: Task[Int] = Task {
        sideEffect = 42
        sideEffect
      }

      val ioUnit = Task.discardContent(ioInt)
      ioUnit.syncUnsafeGet()
      assert(sideEffect == 42)
    }

    it("Task.optionFlatten - some") {
      val io     = Task(some)
      val flatIO = Task.optionFlatten(io, anomaly)

      assert(flatIO.syncUnsafeGet() == 42)
    }

    it("Task.optionFlatten - none") {
      val io     = Task(none)
      val flatIO = Task.optionFlatten(io, anomaly)
      the[InvalidInputFailure] thrownBy {
        flatIO.syncUnsafeGet()
      }
    }

    it("Task.optionFlattenWeak - some") {
      val io     = Task(some)
      val flatIO = Task.optionFlattenWeak(io, throwable)

      assert(flatIO.syncUnsafeGet() == 42)
    }

    it("Task.optionFlattenWeak - none") {
      val io     = Task(none)
      val flatIO = Task.optionFlattenWeak(io, throwable)
      the[RuntimeException] thrownBy {
        flatIO.syncUnsafeGet()
      }
    }

    it("Task.optionFlattenWeak - correct") {
      val io     = Task(Result(42))
      val flatIO = Task.resultFlatten(io)
      assert(flatIO.syncUnsafeGet() == 42)
    }

    it("Task.optionFlattenWeak - incorrect") {
      val io     = Task(Result.fail(anomaly))
      val flatIO = Task.resultFlatten(io)
      the[InvalidInputFailure] thrownBy {
        flatIO.syncUnsafeGet()
      }
    }
  }

  describe("Task — companion methods Task to X") {
    it("Task.asIO") {
      val task = Task.asIO(Task(42))
      assert(task.unsafeRunSync() == 42)
    }

    it("Task.asFuture") {
      val f = Task.asFuture(Task(42))
      assert(f.syncUnsafeGet() == 42)
    }

    it("Task.asResult — correct") {
      val ioResult = Task.asResult(Task(42))
      assert(ioResult.syncUnsafeGet() == Correct(42))
    }

    it("Task.asResult — incorrect — anomaly") {
      val ioResult = Task.asResult(Task.fail(anomaly))
      the[InvalidInputFailure] thrownBy {
        ioResult.syncUnsafeGet().unsafeGet
      }
    }

    it("Task.syncUnsafeResult — correct") {
      val io = Task.syncUnsafeAsResult(Task(42))
      assert(io == Correct(42))
    }

    it("Task.syncUnsafeResult — incorrect") {
      val io = Task.syncUnsafeAsResult(Task.fail(anomaly))
      assert(io.isLeft)
    }

    it("Task.syncUnsafeGet") {
      val io = Task.syncUnsafeGet(Task(42))
      assert(io == 42)
    }
  }

  describe("Task — companion methods transformers") {
    it("Task.bimap — good") {
      val io = Task.bimap(
        Task(42),
        (i: Int)       => i.toString,
        (t: Throwable) => InvalidInputFailure(t)
      )
      assert(io.syncUnsafeGet() == "42")
    }

    it("Task.bimap — bad") {
      val io = Task.bimap(
        Task.fail(anomaly),
        (i: Int) => i.toString,
        (t: Throwable) =>
          t match {
            case i: InvalidInputFailure => i
            case _ => fail("got different throwable than put inside")
        }
      )
      the[InvalidInputFailure] thrownBy {
        io.syncUnsafeGet()
      }
    }

    it("Task.bimapWeak — good") {
      val io = Task.bimapWeak(
        Task(42),
        (i: Int)       => i.toString,
        (t: Throwable) => InvalidInputFailure(t)
      )
      assert(io.syncUnsafeGet() == "42")
    }

    it("Task.bimapWeak — bad") {
      val io = Task.bimapWeak(
        Task.fail(anomaly),
        (i: Int) => i.toString,
        (t: Throwable) =>
          t match {
            case i: InvalidInputFailure => i
            case _ => fail("got different throwable than put inside")
        }
      )
      the[InvalidInputFailure] thrownBy {
        io.syncUnsafeGet()
      }
    }

    it("Task.morph — good") {
      val io = Task.morph(
        Task(42),
        (i: Int)       => i.toString,
        (t: Throwable) => t.getMessage
      )
      assert(io.syncUnsafeGet() == "42")
    }

    it("Task.morph — bad") {
      val io = Task.morph(
        Task.fail(anomaly),
        (i: Int) => i.toString,
        (t: Throwable) =>
          t match {
            case i: InvalidInputFailure => i.getMessage
            case _ => fail("got different throwable than put inside")
        }
      )
      assert(io.syncUnsafeGet() == anomaly.getMessage)
    }
  }

  describe("Task — syntax on simple Task[T] objects") {
    it("Task.asIO") {
      val task = Task(42).asIO
      assert(task.unsafeRunSync() == 42)
    }

    it("Task.asFuture") {
      val f = Task(42).asFuture
      assert(f.syncUnsafeGet() == 42)
    }

    it("Task.asResult — correct") {
      val ioResult = Task(42).asResult
      assert(ioResult.syncUnsafeGet() == Correct(42))
    }

    it("Task.asResult — incorrect — anomaly") {
      val ioResult = Task.fail(anomaly).asResult
      the[InvalidInputFailure] thrownBy {
        ioResult.syncUnsafeGet().unsafeGet
      }
    }

    it("Task.syncUnsafeResult — correct") {
      val io = Task(42).syncUnsafeAsResult()
      assert(io == Correct(42))
    }

    it("Task.syncUnsafeResult — incorrect") {
      val io = Task.fail(anomaly).syncUnsafeAsResult()
      assert(io.isLeft)
    }

    it("Task.syncUnsafeGet") {
      val io = Task(42).syncUnsafeGet()
      assert(io == 42)
    }

    it("Task.discardContent — but still apply side effects") {
      var sideEffect: Int = 0
      val ioInt: Task[Int] = Task {
        sideEffect = 42
        sideEffect
      }

      val ioUnit = ioInt.discardContent
      ioUnit.syncUnsafeGet()
      assert(sideEffect == 42)
    }

    it("Task.bimap — good") {
      val io =
        Task(42).bimap(
          (i: Int)       => i.toString,
          (t: Throwable) => InvalidInputFailure(t)
        )
      assert(io.syncUnsafeGet() == "42")
    }

    it("Task.bimap — bad") {
      val io = Task
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
        io.syncUnsafeGet()
      }
    }

    it("Task.bimapWeak — good") {
      val io = Task(42).bimapWeak(
        (i: Int)       => i.toString,
        (t: Throwable) => InvalidInputFailure(t)
      )
      assert(io.syncUnsafeGet() == "42")
    }

    it("Task.bimapWeak — bad") {
      val io = Task
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
        io.syncUnsafeGet()
      }
    }

    it("Task.morph — good") {
      val io = Task(42).morph(
        (i: Int)       => i.toString,
        (t: Throwable) => t.getMessage
      )
      assert(io.syncUnsafeGet() == "42")
    }

    it("Task.morph — bad") {
      val io = Task
        .fail(anomaly)
        .morph(
          (i: Int) => i.toString,
          (t: Throwable) =>
            t match {
              case i: InvalidInputFailure => i.getMessage
              case _ => fail("got different throwable than put inside")
          }
        )
      assert(io.syncUnsafeGet() == anomaly.getMessage)
    }

  }

  describe("Task — syntax on Boolean") {
    it("Task.condTask — true") {
      val io = true.condTask(
        42,
        anomaly
      )
      assert(io.syncUnsafeGet() == 42)
    }

    it("Task.condTask — false") {
      val io = false.condTask(
        42,
        anomaly
      )
      the[InvalidInputFailure] thrownBy {
        io.syncUnsafeGet()
      }
    }

    it("Task.condWithIO — true") {
      val io = true.condWithTask(
        Task(42),
        anomaly
      )
      assert(io.syncUnsafeGet() == 42)
    }

    it("Task.condWithIO — false") {
      val io = false.condWithTask(
        Task(42),
        anomaly
      )
      the[InvalidInputFailure] thrownBy {
        io.syncUnsafeGet()
      }
    }

    it("Task.failOnTrueTask — true") {
      val io = true.failOnTrueTask(anomaly)
      the[InvalidInputFailure] thrownBy {
        io.syncUnsafeGet()
      }
    }

    it("Task.failOnTrueTask — false") {
      val io = false.failOnTrueTask(anomaly)
      io.syncUnsafeGet()
    }

    it("Task.failOnFalseTask — true") {
      val io = true.failOnFalseTask(anomaly)
      io.syncUnsafeGet()
    }

    it("Task.failOnFalseTask — false") {
      val io = false.failOnFalseTask(anomaly)
      the[InvalidInputFailure] thrownBy {
        io.syncUnsafeGet()
      }
    }

    it("Task.effectOnTrueTask — true") {
      var sideEffect: Int = 0
      val io = true.effectOnTrueTask {
        Task {
          sideEffect = 42
        }
      }
      if (sideEffect == 42) fail("Task did not defer side-effect")
      io.syncUnsafeGet()
      assert(sideEffect == 42)
    }

    it("Task.effectOnTrueTask — false") {
      var sideEffect: Int = 0
      val io = false.effectOnTrueTask {
        Task {
          sideEffect = 42
        }
      }
      if (sideEffect == 42) fail("Task did not defer side-effect")
      io.syncUnsafeGet()
      assert(sideEffect == 0, "side effect should have not been applied")
    }

    it("Task.effectOnFalseTask — false") {
      var sideEffect: Int = 0
      val io = false.effectOnFalseTask {
        Task {
          sideEffect = 42
        }
      }
      if (sideEffect == 42) fail("Task did not defer side-effect")
      io.syncUnsafeGet()
      assert(sideEffect == 42)
    }

    it("Task.effectOnFalseTask — true") {
      var sideEffect: Int = 0
      val io = true.effectOnFalseTask {
        Task {
          sideEffect = 42
        }
      }
      if (sideEffect == 42) fail("Task did not defer side-effect")
      io.syncUnsafeGet()
      assert(sideEffect == 0, "side effect should have not been applied")
    }
  }

  describe("Task — syntax on Task[Boolean]") {
    it("Task.cond — true") {
      val io = Task(true).cond(
        42,
        anomaly
      )
      assert(io.syncUnsafeGet() == 42)
    }

    it("Task.cond — false") {
      val io = Task(false).cond(
        42,
        anomaly
      )
      the[InvalidInputFailure] thrownBy {
        io.syncUnsafeGet()
      }
    }

    it("Task.condWith — true") {
      val io = Task(true).condWith(
        Task(42),
        anomaly
      )
      assert(io.syncUnsafeGet() == 42)
    }

    it("Task.condWith — false") {
      val io = Task(false).condWith(
        Task(42),
        anomaly
      )
      the[InvalidInputFailure] thrownBy {
        io.syncUnsafeGet()
      }
    }

    it("Task.failOnTrue — true") {
      val io = Task(true).failOnTrue(anomaly)
      the[InvalidInputFailure] thrownBy {
        io.syncUnsafeGet()
      }
    }

    it("Task.failOnTrue — false") {
      val io = Task(false).failOnTrue(anomaly)
      io.syncUnsafeGet()
    }

    it("Task.failOnFalse — true") {
      val io = Task(true).failOnFalse(anomaly)
      io.syncUnsafeGet()
    }

    it("Task.failOnFalse — false") {
      val io = Task(false).failOnFalse(anomaly)
      the[InvalidInputFailure] thrownBy {
        io.syncUnsafeGet()
      }
    }

    it("Task.effectOnTrue — true") {
      var sideEffect: Int = 0
      val io = Task(true).effectOnTrue {
        Task {
          sideEffect = 42
        }
      }
      if (sideEffect == 42) fail("Task did not defer side-effect")
      io.syncUnsafeGet()
      assert(sideEffect == 42)
    }

    it("Task.effectOnTrue — false") {
      var sideEffect: Int = 0
      val io = Task(false).effectOnTrue {
        Task {
          sideEffect = 42
        }
      }
      if (sideEffect == 42) fail("Task did not defer side-effect")
      io.syncUnsafeGet()
      assert(sideEffect == 0, "side effect should have not been applied")
    }

    it("Task.effectOnFalse — false") {
      var sideEffect: Int = 0
      val io = Task(false).effectOnFalse {
        Task {
          sideEffect = 42
        }
      }
      if (sideEffect == 42) fail("Task did not defer side-effect")
      io.syncUnsafeGet()
      assert(sideEffect == 42)
    }

    it("Task.effectOnFalse — true") {
      var sideEffect: Int = 0
      val io = Task(true).effectOnFalse {
        Task {
          sideEffect = 42
        }
      }
      if (sideEffect == 42) fail("Task did not defer side-effect")
      io.syncUnsafeGet()
      assert(sideEffect == 0, "side effect should have not been applied")
    }
  }

  describe("Task — syntax Task[Option]") {
    it("Task[Option].flattenOpt - some") {
      val flatIO = Task(some).flattenOpt(anomaly)

      assert(flatIO.syncUnsafeGet() == 42)
    }

    it("Task[Option].flattenOpt - none") {
      val flatIO = Task(none).flattenOpt(anomaly)
      the[InvalidInputFailure] thrownBy {
        flatIO.syncUnsafeGet()
      }
    }

    it("Task[Option].flattenWeak - some") {
      val flatIO = Task(some).flattenOptWeak(throwable)

      assert(flatIO.syncUnsafeGet() == 42)
    }

    it("Task[Option].flattenWeak - none") {
      val flatIO = Task(none).flattenOptWeak(throwable)
      the[RuntimeException] thrownBy {
        flatIO.syncUnsafeGet()
      }
    }
  }
}
