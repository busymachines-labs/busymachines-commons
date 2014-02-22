package com.busymachines.commons.test

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import java.util.UUID
import com.busymachines.commons.domain.Id

@RunWith(classOf[JUnitRunner])
class IdTests extends FlatSpec {

	"Id" should "handle the original UUID" in {
	  val uuid = UUID.randomUUID()
	  assert(Id[String](uuid).originalUuid === uuid)		  
	}
}