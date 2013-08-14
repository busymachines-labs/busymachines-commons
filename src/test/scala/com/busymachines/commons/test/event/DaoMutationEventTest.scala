package com.busymachines.commons.test.event

import org.scalatest.FlatSpec
import com.busymachines.commons.event.DaoMutationEvent

class DaoMutationEventTest extends FlatSpec {
	"DaoMutationEvent" should "recognize class tags" in {
	  assert(DaoMutationEvent(this.getClass,"idx","type","id").forEntityType[DaoMutationEventTest])
	}
  
  
}