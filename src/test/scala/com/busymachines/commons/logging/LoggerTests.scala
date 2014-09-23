package com.busymachines.commons.logging

import com.busymachines.commons.CommonException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.message.StructuredDataMessage
import org.scalatest.FlatSpec
import sun.reflect.generics.reflectiveObjects.NotImplementedException

/**
 * Created by Alexandru Matei on 14.08.2014.
 */
class LoggerTests extends FlatSpec with Logging {

  override def loggerTag=Some("logger test")

  behavior of "Logger.error"

  ignore should "log commons exceptions properly" in {
    var x = 100000;
    val mp=Map("party" -> "BusyMachines", "user" -> "Alexandru")
    while (x > 0) {
      val exc = new CommonException(s"This is a common exception ${x}", Some("12"), mp, Some(new IndexOutOfBoundsException()))
      logger.info(this.suiteName, exc, "party" -> "BusyMachines", "user" -> "Alexandru")
      Thread.sleep(100)
      x -= 1;
    }
  }

  ignore should "log commons exceptions properly 2" in {
    var x = 10000;
    val mp=Map("party" -> "BusyMachines", "user" -> "Lorand")
    while (x > 0) {
      val exc = new CommonException(s"Second commons exception ${x}", Some("12"), mp, Some(new OutOfMemoryError()))
      logger.debug(this.suiteName, exc, "party" -> "BusyMachines", "user" -> "Lorand")
      x -= 1;
    }
  }

  ignore should "log commons exceptions properly 3" in {
    var x = 100;
    val mp=Map("party" -> "BusyMachines", "user" -> "Paul")
    while (x > 0) {
      val exc = new CommonException(s"Third commons exception ${x}", Some("12"), mp, Some(new NullPointerException()))
      logger.error(this.suiteName, exc, "party" -> "BusyMachines", "user" -> "Paul")
      x -= 1;
    }
  }

  ignore should "test to see how my data is represented" in {
    val exc = new CommonException(s"This is a common exception", Some("12"), Map("party" -> "BusyMachines", "user" -> "Lorand"), Some(new NotImplementedException()))
    logger.trace("trace", exc)
    logger.debug("debug", exc)
    logger.info("info", exc)
    logger.warn("warn", exc)
    logger.error("error", exc)
    logger.fatal("fatal", exc)
  }

  ignore should "log default java exceptions" in {
    val exc = new IllegalArgumentException("Test illegal argument")
    logger.error(this.suiteName, exc)
  }

  behavior of "Logger.debug"

  ignore should "debug stuff" in {
    logger.debug("Debuggin stuff with Lorand")
  }
}
