package busymachines.effects_test

import busymachines.core.InvalidInputFailure
import busymachines.effects
import org.scalatest.FunSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 02 Feb 2018
  *
  */
final class EffectsPackageTest extends FunSpec {
  private def test:         ItWord            = it
  private implicit val sch: effects.Scheduler = effects.Scheduler.global

  describe("effects") {

    test("package") {
      assert(effects.Result.pure(1) == effects.Result.pure(1))
      assert(effects.Correct(1) == effects.Correct(1))
      assert(effects.Incorrect(InvalidInputFailure) == effects.Incorrect(InvalidInputFailure))
    }

    test("option") {
      import effects.option._
      assert(Option.pure(1) == Option.pure(1))
    }

    test("try") {
      import effects.tr._
      assert(Try.pure(1) == Try.pure(1))
    }

    test("either") {
      import effects.either._
      assert(
        Either.asIO(Right[Int,   Int](1), (_: Int) => InvalidInputFailure).unsafeRunSync() ==
          Either.asIO(Right[Int, Int](1), (_: Int) => InvalidInputFailure).unsafeRunSync()
      )
    }

    test("result") {
      import effects.result._

      assert(Result.pure(1) == Result.pure(1))
      assert(Correct(1) == Correct(1))
      assert(Incorrect(InvalidInputFailure) == Incorrect(InvalidInputFailure))
    }

    test("io") {
      import effects.io._
      assert(IO.pure(1).unsafeSyncGet() == IO.pure(1).unsafeSyncGet())
    }

    test("task") {
      import effects.task._
      assert(Task.pure(1).unsafeSyncGet() == Task.pure(1).unsafeSyncGet())
    }

    test("future") {
      import effects.future._
      assert(Future.pure(1).unsafeSyncGet() == Future.pure(1).unsafeSyncGet())
    }

  }

}
