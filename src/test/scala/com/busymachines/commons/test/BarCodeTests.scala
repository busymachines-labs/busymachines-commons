package com.busymachines.commons.test

import org.scalatest.FlatSpec
import com.busymachines.commons.{BarCode, Logging}

class BarCodeTests extends FlatSpec with Logging {

  "BarCode" should "create EAN13 checksum" in {
    assert(BarCode.ean13(590123412345l).code === "5901234123457")
    assert(BarCode.ean13(1234).code === "0000000012348")
  }
}
