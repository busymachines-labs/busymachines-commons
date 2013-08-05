package com.busymachines.commons.test

import org.scalatest.FlatSpec
import com.busymachines.commons.Digester

class DigesterTests extends FlatSpec {
  "Digester" should "digest plain text & match digested text" in {
    assert(new Digester().matches("Hello World", new Digester().digest("Hello World").toString) === true)
    assert(new Digester().matches("admin", new Digester().digest("admin").toString) === true)
  }

  it should "digest plain text & not match digested text when it's different" in {
    assert(new Digester().matches("Hello World 1", new Digester().digest("Hello World 2").toString) === false)
  }
}