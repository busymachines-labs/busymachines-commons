package busymachines.effects.async_test

import busymachines.effects.async._

import org.scalatest.funspec.AnyFunSpec

/**
  *
  * If this test ever fails to compile it means that somehow the plugin
  * "better-monadic-for" is disabled. The plugin ought to reside here:
  *
  * https://github.com/oleg-py/better-monadic-for
  *
  * Please make sure to enable in the build. Since STD scala for kinda sucks
  * and does way too much magic with its unwanted .withFilter calls on
  * <- expressions in the for.
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 18 Apr 2018
  *
  */
class BetterMonadicForTest extends AnyFunSpec {
  private def test: ItWord = it

  describe("no withFilter for ought to work w/ IO which has no .withFilter method") {
    test("destructure tuple directly from IO") {
      val ioTuple: IO[(Int, Int)] = IO.pure((1, 2))

      val r = for {
        (x, y) <- ioTuple
      } yield x + y

      assert(r.unsafeRunSync() == 3)
    }

    test("type ascription on right-hand-side of <- actually type ascription, not instanceOf check") {
      val r = for {
        x: List[Int] <- IO.pure(List(42))
      } yield x

      assert(r.unsafeRunSync() == List(42))
    }
  }

}
