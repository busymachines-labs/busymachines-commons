package com.kentivo.mdm.ui

import net.scalaleafs.Server
import net.scalaleafs.Configuration
import net.scalaleafs.AbstractSprayServer

class UI() {

  val config = new Configuration()
  val server = new Server("ui" :: Nil, config)
  val route = new AbstractSprayServer(config) {
    override def render = { trail =>
      <h1>hi</h1>
    }
  }
}