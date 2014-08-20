package com.busymachines.commons.logger

import com.busymachines.commons.CommonException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.message.StructuredDataMessage
import org.scalatest.FlatSpec
import sun.reflect.generics.reflectiveObjects.NotImplementedException

/**
 * Created by Alexandru Matei on 14.08.2014.
 */
class LoggerTests extends FlatSpec with Logging {

  //  val logger = LogManager.getLogger()

  behavior of "Logger.error"

  /*
  FACTS:
  ---
  queue size : 50000
  log # : 10000
  bulk size : 1000
  codeLocationInfo : true
  thread sleep time : 300ms
  system: java 1.8_11 maxMetaSpaceSize = 2048 CPU = I5 1.7 GHz
  ---
  queue size : 50000
  log # : 10000
  bulk size : 100
  codeLocationInfo : true
  thread sleep time : 0ms
  system: java 1.8_11 maxMetaSpaceSize = 2048 CPU = I5 1.7 GHz
  ---
  queue size : 1000
  log # : 10000
  bulk size : 100
  codeLocationInfo : true
  thread sleep time : 0ms
  system: java 1.8_11 maxMetaSpaceSize = 2048 CPU = I5 1.7 GHz
  ---
  queue size : 1000
  log # : 50000
  bulk size : 100
  codeLocationInfo : true
  thread sleep time : 0ms
  system: java 1.8_11 maxMetaSpaceSize = 2048 CPU = I5 1.7 GHz

   */
  ignore should "log commons exceptions properly" in {
    var x = 10000;
    while (x > 0) {
      val exc = new CommonException(s"This is a common exception ${x}", Some("12"), Map("party" -> "BusyMachines", "user" -> "Alexandru"), Some(new IndexOutOfBoundsException()))
      //      val map = new java.util.concurrent.ConcurrentHashMap[String,String]()
      //      map.put("party", "KoffiePartners")
      //      map.put("user", "Lorand")
      //      val msg= new StructuredDataMessage("1","yo","la", map)
      logger.error(this.suiteName, exc)
      x -= 1;
    }
    //    Thread.sleep(100)
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
