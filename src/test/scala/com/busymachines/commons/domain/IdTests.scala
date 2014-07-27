package com.busymachines.commons.domain

import java.util.UUID

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class IdTests extends FlatSpec {

	"Id" should "handle the original UUID" in {
	  val uuid = UUID.randomUUID()
	  assert(Id[String](uuid).originalUuid === uuid)		  
	}
}