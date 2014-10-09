package com.busymachines.commons

import com.busymachines.commons.Implicits._
import scala.concurrent.ExecutionContext.Implicits.global

import org.scalatest.FlatSpec

import scala.concurrent.Future

class IterableTraverseTest extends FlatSpec {


  "Traversed futures" should "be executed serially" in {

    val collection = "one" :: "two" :: "three" :: Nil

    var current = ""

    collection.traverse { value =>
      Future {
        current = value
        Thread.sleep(100)
        assert(current == value)
        ""
      }
    }.await

  }
}