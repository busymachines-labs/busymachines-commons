package com.busymachines.compilerbugs

import org.scalatest.FlatSpec

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 24 Feb 2016
  *
  */

sealed trait ADTWithTwoVariants[T]

case class ADTVariant1[T](i: T) extends ADTWithTwoVariants[T]

case class ADTVariant2[T](i1: T, i2: T) extends ADTWithTwoVariants[T]

case class Container(
  i1: Option[ADTWithTwoVariants[Int]],
  i2: Option[ADTWithTwoVariants[BigDecimal]],
  seq: Seq[Option[BigDecimal]]
)

case class Result(
  i1: Option[ADTWithTwoVariants[Int]],
  i2: Option[ADTWithTwoVariants[BigDecimal]]
)

class TupleAssignmentHoleTest extends FlatSpec {

  def methodWithTypeHole(branch: Boolean, nestedBranch: Boolean, doublyNestedBranch: Boolean): Result = {
    val container = Container(Some(ADTVariant1(1)), Some(ADTVariant2(2, 1)), Seq(Some(1), Some(2)))
    val factor = 1
    //the hole is introduced the moment the type annotation is put on r2
    val (r1, r2: Option[ADTWithTwoVariants[BigDecimal]]) = {
      if (branch) {
        if (nestedBranch) {
          //WRONG! seq.head is Option[BigDecimal], not Option[ADTWithTwoVariants[BigDecimal]]
          (Some(ADTVariant1(factor * container.seq.length)), container.seq.head)
        } else {
          if (doublyNestedBranch) {
            //actually correct
            (Some(ADTVariant1(factor * container.seq.length)), None)
          } else {
            //WRONG! the second element is Option[BigDecimal] not Option[ADVWithTWOVariants[BigDecimal]]
            (None, Some(factor * 42))
          }
        }
      } else {
        //actually correct
        (container.i1, container.i2)
      }
    }
    Result(
      i1 = r1,
      i2 = r2
    )
  }

  behavior of "scalac"

  //===========================================================================
  //===========================================================================

  it should "compile a bad program, run it on a control flow path that gives a bad result" in {
    val wrongResult: Result = methodWithTypeHole(
      branch = true,
      nestedBranch = true,
      doublyNestedBranch = false
    )

    assertResult(expected = "Result(Some(ADTVariant1(2)),Some(1))", "... result toString")(actual = wrongResult.toString)
    assertResult(expected = Some(ADTVariant1[Int](2)), "... result val 1")(actual = wrongResult.i1)
    //WRONG!: this should fail because the type of Result.i2 is Option[ADTWithTwoVariants[BigDecimal]]
    //but you actually get back an object with value Some(1).
    assertResult(expected = Some(1), "... result val 2... which is completely WRONG!")(actual = wrongResult.i2)
  }

  //===========================================================================
  //===========================================================================

  it should "compile a bad program, run it on another control flow path that gives a bad result" in {
    val wrongResult: Result = methodWithTypeHole(
      branch = true,
      nestedBranch = false,
      doublyNestedBranch = false
    )

    assertResult(expected = "Result(None,Some(42))", "... result toString")(actual = wrongResult.toString)
    assertResult(expected = None, "... result val 1")(actual = wrongResult.i1)
    //    //WRONG!: this should fail because the type of Result.i2 is Option[ADTWithTwoVariants[BigDecimal]]
    //    //but you actually get back an object with value Some(1).
    assertResult(expected = Some(42), "... result val 2... which is completely WRONG!")(actual = wrongResult.i2)
  }

  //===========================================================================
  //===========================================================================

  it should "compile a bad program, run it on the control flow path that is actually correct" in {
    val correctResult = methodWithTypeHole(
      branch = false,
      nestedBranch = false,
      doublyNestedBranch = false
    )

    assertResult(expected = "Result(Some(ADTVariant1(1)),Some(ADTVariant2(2,1)))", "... result toString")(actual = correctResult.toString)
    assertResult(expected = Some(ADTVariant1(1)), "... result val 1")(actual = correctResult.i1)
    assertResult(expected = Some(ADTVariant2(2, 1)), "... result val 2... which is correct!")(actual = correctResult.i2)
  }

  //===========================================================================
  //===========================================================================

  it should "not compile the program when there is no type annotation on variable r2" in {
    assertDoesNotCompile {
      """
        |  def noTypeHole(branch: Boolean, nestedBranch: Boolean, doublyNestedBranch: Boolean): Result = {
        |    val container = Container(Some(ADTVariant1(1)), Some(ADTVariant2(2, 1)), Seq(Some(1), Some(2)))
        |    val factor = 1
        |    val (r1, r2) = {
        |      if (branch) {
        |        if (nestedBranch) {
        |          //WRONG! seq.head is Option[BigDecimal], not Option[ADTWithTwoVariants[BigDecimal]]
        |          (Some(ADTVariant1(factor * container.seq.length)), container.seq.head)
        |        } else {
        |          if (doublyNestedBranch) {
        |            //(Some(OneValue(factor * pattern.length)), None)
        |            (Some(ADTVariant1(factor * container.seq.length)), None)
        |          } else {
        |            //WRONG! the second element is Option[BigDecimal] not Option[ADVWithTWOVariants[BigDecimal]]
        |            (None, Some(factor * 42))
        |          }
        |        }
        |      } else {
        |        (container.i1, container.i2)
        |      }
        |    }
        |    Result(
        |      i1 = r1,
        |      i2 = r2
        |    )
        |  }
      """.stripMargin
    }
  }

  //===========================================================================
  //===========================================================================

  it should "not compile the program when there is an annotation on the entire tuple (r1, r2)" in {
    assertDoesNotCompile {
      """
        | def noTypeHole(branch: Boolean, nestedBranch: Boolean, doublyNestedBranch: Boolean): Result = {
        |    val container = Container(Some(ADTVariant1(1)), Some(ADTVariant2(2, 1)), Seq(Some(1), Some(2)))
        |    val factor = 1
        |    val (r1, r2): (Option[ADTWithTwoVariants[Int]], Option[ADTWithTwoVariants[BigDecimal]]) = {
        |      if (branch) {
        |        if (nestedBranch) {
        |          //WRONG! seq.head is Option[BigDecimal], not Option[ADTWithTwoVariants[BigDecimal]]
        |          (Some(ADTVariant1(factor * container.seq.length)), container.seq.head)
        |        } else {
        |          if (doublyNestedBranch) {
        |            //(Some(OneValue(factor * pattern.length)), None)
        |            (Some(ADTVariant1(factor * container.seq.length)), None)
        |          } else {
        |            //WRONG! the second element is Option[BigDecimal] not Option[ADVWithTWOVariants[BigDecimal]]
        |            (None, Some(factor * 42))
        |          }
        |        }
        |      } else {
        |        (container.i1, container.i2)
        |      }
        |    }
        |    Result(
        |      i1 = r1,
        |      i2 = r2
        |    )
        |  }
      """.stripMargin
    }
  }

  //===========================================================================
  //===========================================================================


}