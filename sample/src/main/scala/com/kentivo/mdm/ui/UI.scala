package com.kentivo.mdm.ui

import net.scalaleafs.ScalaLeafsSprayRoute
import net.scalaleafs.Server
import net.scalaleafs.Configuration

class UI() {

  val config = new Configuration()
  val server = new Server("ui" :: Nil, config)
  val route = new ScalaLeafsSprayRoute(server) ( trail =>
    <h1>hi</h1>
  )
  
}