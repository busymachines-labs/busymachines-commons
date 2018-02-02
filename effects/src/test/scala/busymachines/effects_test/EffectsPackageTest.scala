/**
  * Copyright (c) 2017-2018 BusyMachines
  *
  * See company homepage at: https://www.busymachines.com/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
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
