package com.kentivo.mdm.db

import org.elasticsearch.client.Client
import com.busymachines.commons.dao.elasticsearch.Index

class MdmIndex(client : Client) extends Index(client) {
  val name = "kentivo_mdm" 
}
