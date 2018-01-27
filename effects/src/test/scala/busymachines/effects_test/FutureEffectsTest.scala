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
class FutureEffectsTest extends FunSpec with Matchers {
  private implicit val sc: Scheduler = Scheduler.global

  describe("Future — effects ops") {
    it("convert to IO — correct") {
      assert(Future.pure(42).asIO.unsafeRunSync() == 42)

      assert(Future.asIO(Future.pure(42)).unsafeRunSync() == 42)
    }

    it("convert to IO — incorrect") {
      the[InvalidInputFailure] thrownBy {
        Future.fail(InvalidInputFailure("incorrect Future")).asIO.unsafeRunSync()
      }

      the[InvalidInputFailure] thrownBy {
        Future.asIO {
          Future.fail(InvalidInputFailure("incorrect Future"))
        }.unsafeRunSync()
      }
    }

    it("convert to Task — correct") {
      assert(Future.pure(42).asTask.runAsync.syncUnsafeGet() == 42)

      assert(Future.asTask(Future.pure(42)).runAsync.syncUnsafeGet() == 42)
    }

    it("convert to Task — incorrect") {
      the[InvalidInputFailure] thrownBy {
        Future.fail(InvalidInputFailure("incorrect Future")).asTask.runAsync.syncUnsafeGet()
      }

      the[InvalidInputFailure] thrownBy {
        Future.asTask {
          Future.fail(InvalidInputFailure("incorrect Future"))
        }.runAsync.syncUnsafeGet()
      }
    }

    it("properly suspend in IO") {
      var sideEffect = 0

      val suspendedSideEffect: IO[Int] = Future {
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

      val suspendedSideEffect: IO[Int] = Future.suspendInIO {
        Future {
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

      val suspendedSideEffect: Task[Int] = Future {
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

      val suspendedSideEffect: Task[Int] = Future.suspendInTask {
        Future {
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
