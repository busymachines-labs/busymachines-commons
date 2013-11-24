package com.busymachines.commons

import java.net.URLClassLoader
import java.io.File

/**
 * App that is intended to run as a server process. It will have standard commands:
 * 
 * -install  Installs the application on the system (Ubuntu is supported)
 * 
 * It add support for graceful shutdowns.
 */
class ServerApp extends App {

  case class CmdLineArg(description : String)(F : => Unit) 
  case class CmdLineOption(name: String, short: Option[Char], parameterDescription : Option[String])(f : => Unit)
  
  val installOption = CmdLineOption("install", None, Some("name")) {
    if (new File("/etc/init.d").isDirectory) {
      println("Installing application")
      
    }
    else {
      throw new Exception("Couldn't install application: unrecognized environment")
    }
  }
  
  val logfileOption = CmdLineOption("logfile", None, Some("name")) {
    if (new File("/etc/init.d").isDirectory) {
      println("Installing application")
      
    }
    else {
      throw new Exception("Couldn't install application: unrecognized environment")
    }
  }
  
  def defaultOptions = installOption :: Nil
  def allArgs = defaultOptions
  
  println(args)
  
  def processArgs = {
    var arg : Int = 0
    while (arg < args.size) {
      
    }
  }
}


object TestApp extends ServerApp {
	println("args" + args.mkString(" "))
	getClass.getClassLoader match {
	  case cl : URLClassLoader => println(cl.getURLs.mkString("\n"))
	}
	import implicits._
	println("CRC" + new Array[Byte](0).crc32)
}