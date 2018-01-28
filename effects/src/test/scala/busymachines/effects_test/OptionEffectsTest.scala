//package busymachines.effects_test
//
//import busymachines.core._
//import busymachines.effects._
//import org.scalatest._
//import busymachines.scalatest.FunSpecAlias
//
//import scala.util.Try
//
///**
//  *
//  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
//  * @since 28 Jan 2018
//  *
//  */
//final class OptionEffectsTest extends FunSpecAlias with Matchers {
//  private implicit val sc: Scheduler = Scheduler.global
//
//  private val anomaly:   InvalidInputFailure = InvalidInputFailure("invalid_input_failure")
//
//  private val none: Option[Int] = Option.empty
//  private val some: Option[Int] = Option(42)
//
//  describe("Option — companion object syntax") {
//    describe("conversion to other effects") {
//      describe("Option.asList") {
//        test("some") {
//          val opt = Option(42)
//          assert(Option.asList(opt) == List(42))
//        }
//
//        test("none") {
//          val opt = none
//          assert(Option.asList(opt) == List.empty[Int])
//        }
//      }
//
//      describe("Option.asTry") {
//        test("some") {
//          val opt = some
//          assert(Option.asTry(opt, anomaly) == Try(42))
//        }
//
//        test("none") {
//          val opt = none
//          the[InvalidInputFailure] thrownBy {
//            Option.asTry(opt, anomaly).get
//          }
//        }
//      }
//
//      describe("Option.asTryWeak") {
//        test("some") {
//          val opt = some
//          assert(Option.asTryWeak(opt, anomaly) == Try(42))
//        }
//
//        test("none") {
//          val opt = none
//          the[InvalidInputFailure] thrownBy {
//            Option.asTryWeak(opt, anomaly).get
//          }
//        }
//      }
//
//      describe("Option.asResult") {
//        test("some") {
//          val opt = some
//          assert(Option.asResult(opt, anomaly).unsafeGet == 42)
//        }
//
//        test("none") {
//          val opt = none
//          the[InvalidInputFailure] thrownBy {
//            Option.asResult(opt, anomaly).unsafeGet
//          }
//        }
//      }
//
//      describe("Option.asIO") {
//        test("some") {
//          val opt = some
//          assert(Option.asIO(opt, anomaly).unsafeRunSync() == 42)
//        }
//
//        test("none") {
//          val opt = none
//          the[InvalidInputFailure] thrownBy {
//            Option.asIO(opt, anomaly).unsafeRunSync()
//          }
//        }
//      }
//
//      describe("Option.asTask") {
//        test("some") {
//          val opt = some
//          assert(Option.asTask(opt, anomaly).syncUnsafeGet() == 42)
//        }
//
//        test("none") {
//          val opt = none
//          the[InvalidInputFailure] thrownBy {
//            Option.asTask(opt, anomaly).syncUnsafeGet()
//          }
//        }
//      }
//
//      describe("Option.asFuture") {
//        test("some") {
//          val opt = some
//          assert(Option.asFuture(opt, anomaly).syncUnsafeGet() == 42)
//        }
//
//        test("none") {
//          val opt = none
//          the[InvalidInputFailure] thrownBy {
//            Option.asFuture(opt, anomaly).syncUnsafeGet()
//          }
//        }
//      }
//    }
//
//    describe("transformers") {
//      describe("Option.morph") {
//        test("some") {
//          val opt = Option.morph(
//            some,
//            (i: Int) => i.toString,
//            "I was none!"
//          )
//          assert(opt.get == "42")
//        }
//
//        test("none") {
//          val opt = Option.morph(
//            none,
//            (i: Int) => i.toString,
//            "I was none!"
//          )
//          assert(opt.get == "I was none!")
//        }
//      }
//    }
//
//    describe("on boolean value") {
//      describe("Option.cond") {
//        test("true") {
//          val opt = Option.cond(
//            true,
//            42
//          )
//          assert(opt.unsafeGet == 42)
//        }
//
//        test("false") {
//          val opt = Option.cond(
//            false,
//            42
//          )
//          assert(opt == None)
//        }
//      }
//
//      describe("Option.condWith") {
//        test("true") {
//          val opt = Option.condWith(
//            true,
//            some
//          )
//          assert(opt.unsafeGet == 42)
//        }
//
//        test("false") {
//          val opt = Option.condWith(
//            false,
//            some
//          )
//          assert(opt == None)
//        }
//      }
//
//      describe("Option.flatCond") {
//        test("Some(true)") {
//          val opt = Option.flatCond(
//            Option(true),
//            42
//          )
//          assert(opt.unsafeGet == 42)
//        }
//
//        test("Some(false)") {
//          val opt = Option.flatCond(
//            Some(false),
//            42
//          )
//          assert(opt == None)
//        }
//
//        test("None") {
//          val opt = Option.flatCond(
//            None,
//            42
//          )
//          assert(opt == None)
//        }
//      }
//
//      describe("Option.flatCondWith") {
//        test("Some(true)") {
//          val opt = Option.flatCondWith(
//            Option(true),
//            some
//          )
//          assert(opt.unsafeGet == 42)
//        }
//
//        test("Some(false)") {
//          val opt = Option.flatCondWith(
//            Option(false),
//            some
//          )
//          assert(opt == None)
//        }
//
//        test("Some(true) -> None") {
//          val opt = Option.flatCondWith(
//            Option(false),
//            none
//          )
//          assert(opt == None)
//        }
//
//        test("None") {
//          val opt = Option.flatCondWith(
//            None,
//            some
//          )
//          assert(opt == None)
//        }
//      }
//    }
//  } //end companion object syntax
//
//  describe("Option — infix object syntax") {
//    describe("conversion to other effects") {
//      describe(".asList") {
//        test("some") {
//          val opt = Option(42)
//          assert(opt.asList == List(42))
//        }
//
//        test("none") {
//          val opt = none
//          assert(opt.asList == List.empty[Int])
//        }
//      }
//
//      describe(".asTry") {
//        test("some") {
//          val opt = some
//          assert(opt.asTry(anomaly) == Try(42))
//        }
//
//        test("none") {
//          val opt = none
//          the[InvalidInputFailure] thrownBy {
//            opt.asTry(anomaly).get
//          }
//        }
//      }
//
//      describe(".asTryWeak") {
//        test("some") {
//          val opt = some
//          assert(opt.asTryWeak(anomaly) == Try(42))
//        }
//
//        test("none") {
//          val opt = none
//          the[InvalidInputFailure] thrownBy {
//            Option.asTryWeak(opt, anomaly).get
//          }
//        }
//      }
//
//      describe(".asResult") {
//        test("some") {
//          val opt = some
//          assert(opt.asResult(anomaly).unsafeGet == 42)
//        }
//
//        test("none") {
//          val opt = none
//          the[InvalidInputFailure] thrownBy {
//            opt.asResult(anomaly).unsafeGet
//          }
//        }
//      }
//
//      describe(".asIO") {
//        test("some") {
//          val opt = some
//          assert(opt.asIO(anomaly).unsafeRunSync() == 42)
//        }
//
//        test("none") {
//          val opt = none
//          the[InvalidInputFailure] thrownBy {
//            opt.asIO(anomaly).unsafeRunSync()
//          }
//        }
//      }
//
//      describe(".asTask") {
//        test("some") {
//          val opt = some
//          assert(opt.asTask(anomaly).syncUnsafeGet() == 42)
//        }
//
//        test("none") {
//          val opt = none
//          the[InvalidInputFailure] thrownBy {
//            opt.asTask(anomaly).syncUnsafeGet()
//          }
//        }
//      }
//
//      describe(".asFuture") {
//        test("some") {
//          val opt = some
//          assert(opt.asFuture(anomaly).syncUnsafeGet() == 42)
//        }
//
//        test("none") {
//          val opt = none
//          the[InvalidInputFailure] thrownBy {
//            opt.asFuture(anomaly).syncUnsafeGet()
//          }
//        }
//      }
//
//      test(".unsafeGet") {
//        assert(some.unsafeGet == 42)
//      }
//    }
//
//    describe("transformers") {
//      describe(".morph") {
//        test("some") {
//          val opt = some.morph(
//            (i: Int) => i.toString,
//            "I was none!"
//          )
//          assert(opt.get == "42")
//        }
//
//        test("none") {
//          val opt = none.morph(
//            (i: Int) => i.toString,
//            "I was none!"
//          )
//          assert(opt.get == "I was none!")
//        }
//      }
//    }
//
//    describe("on boolean value") {
//      describe(".condOption") {
//        test("true") {
//          val opt = true.condOption(
//            42
//          )
//          assert(opt.unsafeGet == 42)
//        }
//
//        test("false") {
//          val opt = false.condOption(
//            42
//          )
//          assert(opt == None)
//        }
//      }
//
//      describe(".condWithOption") {
//        test("true") {
//          val opt = true.condWithOption(
//            some
//          )
//          assert(opt.unsafeGet == 42)
//        }
//
//        test("false") {
//          val opt = false.condWithOption(
//            some
//          )
//          assert(opt == None)
//        }
//      }
//
//      describe("Option[Boolean].cond") {
//        test("Some(true)") {
//          val opt = Option(true).cond(
//            42
//          )
//          assert(opt.unsafeGet == 42)
//        }
//
//        test("Some(false)") {
//          val opt = Option(false) cond (
//            42
//          )
//          assert(opt == None)
//        }
//
//        test("None") {
//          val opt = Option
//            .empty[Boolean]
//            .cond(
//              42
//            )
//          assert(opt == None)
//        }
//      }
//
//      describe("Option[Boolean].condWith") {
//        test("Some(true)") {
//          val opt = Option(true).condWith(
//            some
//          )
//          assert(opt.unsafeGet == 42)
//        }
//
//        test("Some(false)") {
//          val opt = Option(false).condWith(
//            some
//          )
//          assert(opt == None)
//        }
//
//        test("Some(true) -> None") {
//          val opt = Option(false).condWith(
//            none
//          )
//          assert(opt == None)
//        }
//
//        test("None") {
//          val opt = Option
//            .empty[Boolean]
//            .condWith(
//              some
//            )
//          assert(opt == None)
//        }
//      }
//    }
//
//  } //end infix object syntax
//
//}
