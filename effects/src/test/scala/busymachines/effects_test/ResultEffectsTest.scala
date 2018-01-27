package busymachines.effects_test

import busymachines.core._
import busymachines.effects._
import org.scalatest._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jan 2018
  *
  */
final class ResultEffectsTest extends FunSpec with Matchers {
  private implicit val sc: Scheduler = Scheduler.global

  describe("Result — effects ops") {
    it("convert to IO — correct") {
      assert(Result(42).asIO.unsafeRunSync() == 42)

      assert(Result.asIO(Result(42)).unsafeRunSync() == 42)
    }

    it("convert to IO — incorrect") {
      the[InvalidInputFailure] thrownBy {
        Result.fail(InvalidInputFailure("incorrect Result")).asIO.unsafeRunSync()
      }

      the[InvalidInputFailure] thrownBy {
        Result.asIO {
          Result.fail(InvalidInputFailure("incorrect Result"))
        }.unsafeRunSync()
      }
    }

    it("convert to Task — correct") {
      assert(Result(42).asTask.runAsync.syncUnsafeGet() == 42)

      assert(Result.asTask(Result(42)).runAsync.syncUnsafeGet() == 42)
    }

    it("convert to Task — incorrect") {
      the[InvalidInputFailure] thrownBy {
        Result.fail(InvalidInputFailure("incorrect Result")).asTask.runAsync.syncUnsafeGet()
      }

      the[InvalidInputFailure] thrownBy {
        Result.asTask {
          Result.fail(InvalidInputFailure("incorrect Result"))
        }.runAsync.syncUnsafeGet()
      }
    }

    it("properly suspend in IO") {
      var sideEffect = 0

      val suspendedSideEffect: IO[Int] = Result {
        sideEffect = 42
        sideEffect
      }.suspendInIO

      //this is not thrown:
      if (sideEffect == 42) fail("side-effects make me sad")

      suspendedSideEffect.unsafeRunSync()

      assert(sideEffect == 42, "side-effect ( have been applied")
    }

    it("properly suspend in IO — companion object method") {
      var sideEffect = 0

      val suspendedSideEffect: IO[Int] = Result.suspendInIO {
        Result {
          sideEffect = 42
          sideEffect
        }
      }

      //this is not thrown:
      if (sideEffect == 42) fail("side-effects make me sad")

      suspendedSideEffect.unsafeRunSync()

      assert(sideEffect == 42, "side-effect ( have been applied")
    }

    it("properly suspend in Task") {
      var sideEffect = 0

      val suspendedSideEffect: Task[Int] = Result {
        sideEffect = 42
        sideEffect
      }.suspendInTask

      //this is not thrown:
      if (sideEffect == 42) fail("side-effects make me sad")

      suspendedSideEffect.runAsync.syncAwaitReady()

      assert(sideEffect == 42, "side-effect ( have been applied")
    }

    it("properly suspend in Task — companion object method") {
      var sideEffect = 0

      val suspendedSideEffect: Task[Int] = Result.suspendInTask {
        Result {
          sideEffect = 42
          sideEffect
        }
      }

      //this is not thrown:
      if (sideEffect == 42) fail("side-effects make me sad")

      suspendedSideEffect.runAsync.syncAwaitReady()

      assert(sideEffect == 42, "side-effect ( have been applied")
    }
  }

}
