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
class ResultEffectsTest extends FlatSpec with Matchers {
  private implicit val sc: Scheduler = Scheduler.global

  behavior of "Result — effects ops"

  it should "convert to IO — correct" in {
    assert(Result(42).asIO.unsafeRunSync() == 42)

    assert(Result.asIO(Result(42)).unsafeRunSync() == 42)
  }

  it should "convert to IO — incorrect" in {
    the[InvalidInputFailure] thrownBy {
      Result.fail(InvalidInputFailure("incorrect Result")).asIO.unsafeRunSync()
    }

    the[InvalidInputFailure] thrownBy {
      Result.asIO {
        Result.fail(InvalidInputFailure("incorrect Result"))
      }.unsafeRunSync()
    }
  }

  it should "convert to Task — correct" in {
    assert(Result(42).asTask.runAsync.syncUnsafeGet() == 42)

    assert(Result.asTask(Result(42)).runAsync.syncUnsafeGet() == 42)
  }

  it should "convert to Task — incorrect" in {
    the[InvalidInputFailure] thrownBy {
      Result.fail(InvalidInputFailure("incorrect Result")).asTask.runAsync.syncUnsafeGet()
    }

    the[InvalidInputFailure] thrownBy {
      Result.asTask {
        Result.fail(InvalidInputFailure("incorrect Result"))
      }.runAsync.syncUnsafeGet()
    }
  }

  it should "properly suspend in IO" in {
    var sideEffect = 0

    val suspendedSideEffect: IO[Int] = Result {
      sideEffect = 42
      sideEffect
    }.suspendInIO

    //this is not thrown:
    if (sideEffect == 42) throw CatastrophicError("Side-effects make me sad")

    suspendedSideEffect.unsafeRunSync()

    assert(sideEffect == 42, "side-effect should have been applied")
  }

  it should "properly suspend in Task" in {
    var sideEffect = 0

    val suspendedSideEffect: Task[Int] = Result {
      sideEffect = 42
      sideEffect
    }.suspendInTask

    //this is not thrown:
    if (sideEffect == 42) throw CatastrophicError("Side-effects make me sad")

    suspendedSideEffect.runAsync.syncAwaitReady()

    assert(sideEffect == 42, "side-effect should have been applied")
  }
}
