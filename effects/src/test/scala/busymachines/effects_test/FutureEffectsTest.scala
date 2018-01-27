package busymachines.effects_test

import busymachines.core._
import busymachines.effects._
import org.scalatest.FlatSpec
import org.scalatest.Matchers

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jan 2018
  *
  */
class FutureEffectsTest extends FlatSpec with Matchers {
  private implicit val sc: Scheduler = Scheduler.global

  behavior of "Future — effects ops"

  it should "convert to IO — correct" in {
    assert(Future.pure(42).asIO.unsafeRunSync() == 42)

    assert(Future.asIO(Future.pure(42)).unsafeRunSync() == 42)
  }

  it should "convert to IO — incorrect" in {
    the[InvalidInputFailure] thrownBy {
      Future.fail(InvalidInputFailure("incorrect Future")).asIO.unsafeRunSync()
    }

    the[InvalidInputFailure] thrownBy {
      Future.asIO {
        Future.fail(InvalidInputFailure("incorrect Future"))
      }.unsafeRunSync()
    }
  }

  it should "convert to Task — correct" in {
    assert(Future.pure(42).asTask.runAsync.syncUnsafeGet() == 42)

    assert(Future.asTask(Future.pure(42)).runAsync.syncUnsafeGet() == 42)
  }

  it should "convert to Task — incorrect" in {
    the[InvalidInputFailure] thrownBy {
      Future.fail(InvalidInputFailure("incorrect Future")).asTask.runAsync.syncUnsafeGet()
    }

    the[InvalidInputFailure] thrownBy {
      Future.asTask {
        Future.fail(InvalidInputFailure("incorrect Future"))
      }.runAsync.syncUnsafeGet()
    }
  }

  it should "properly suspend in IO" in {
    var sideEffect = 0

    val suspendedSideEffect: IO[Int] = Future {
      sideEffect = 42
      sideEffect
    }.suspendInIO

    //this is not thrown:
    if (sideEffect == 42) fail("side-effects make me sad")

    suspendedSideEffect.unsafeRunSync()

    assert(sideEffect == 42, "side-effect should have been applied")
  }

  it should "properly suspend in IO — companion object method" in {
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

    assert(sideEffect == 42, "side-effect should have been applied")
  }

  it should "properly suspend in Task" in {
    var sideEffect = 0

    val suspendedSideEffect: Task[Int] = Future {
      sideEffect = 42
      sideEffect
    }.suspendInTask

    //this is not thrown:
    if (sideEffect == 42) fail("side-effects make me sad")

    suspendedSideEffect.runAsync.syncAwaitReady()

    assert(sideEffect == 42, "side-effect should have been applied")
  }

  it should "properly suspend in Task — companion object method" in {
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

    assert(sideEffect == 42, "side-effect should have been applied")
  }
}
