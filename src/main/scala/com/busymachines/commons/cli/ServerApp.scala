package com.busymachines.commons.cli

import java.net.URLClassLoader
import java.io.File
import com.busymachines.commons.Implicits._
import scala.collection.mutable.ListBuffer
import akka.io.IO
import scopt.OptionParser

class CmdLineArg(description : String)(F : => Unit)
class CmdLineOption(val name: String, val short: Option[Char], val parameters: Seq[String], val f : Seq[String] => Unit)
class CmdLineOption1(name: String, short: Option[Char], parameter: String, f : String => Unit) extends CmdLineOption(name, short, Seq(parameter), pars => f(pars(0)))

/**
 * App that is intended to run as a server process. It will have standard commands:
 * 
 * -install  Installs the application on the system (Ubuntu is supported) 
 * 
 * It add support for graceful shutdowns.
 */
class ServerApp(appName: String) extends App {

  protected def description: String = ""
  

  val installOption = new CmdLineOption1("install", Some('i'), "name", { name =>
    
    def copyJars(dest: File): List[String] = {
      getClass.getClassLoader match {
        case cl : URLClassLoader => 
          for {
            url <- cl.getURLs.toList
            fileName = url.fileName// if fileName.endsWith(".jar")
            file = new File(dest, fileName) 
            _ = println(s"copying file $file")
  //          url.copyTo(new File(dest, url.getFile()))
          } yield fileName
        case _ => Nil
      }
    }
    
    
    if (new File("/etc/init.d").isDirectory) {
      println("Installing application")
      
    }
    else if (new File("/Library/LaunchDaemons").isDirectory) {
      println("Installing application")
      copyJars(new File("target/testjars"))       
    }
    else {
      throw new Exception("Couldn't install application: unrecognized environment")
    }
  })
  
  
  def defaultOptions = installOption :: Nil
  def allOptions = defaultOptions
  
//  println(args.mkString(","))
  
  
  private def processArgs() = {
    var i : Int = 0
    while (i < args.size) {
      val arg = args(i)
      if (arg.startsWith("-")) 
        allOptions.find(_.name == arg.substring(1)) match {
          case None => throw new Exception(s"Unknown option '$arg'")
          case Some(option) =>
            val b = new ListBuffer[String]
            while (b.size < option.parameters.size && i + 1 < args.size && !args(i + 1).startsWith("-")) {
              b += args(i + 1)
              i += 1              
            }
            if (b.size < option.parameters.size)
              throw new Exception(s"Option '${option.name}' is missing parameter '${option.parameters(b.size)}'")
            option.f(b.toList)
        }
      else throw new Exception(s"Unknown argument '$arg'")
      i += 1
    }
  }
  
  def printUsage() = {
    print("Usage: " + appName)
    println(" ")
    val longestNameSize = allOptions.map(_.name.size).max
    for (option <- allOptions) {
      println("  --" + option.name.padTo(longestNameSize, ' ') + option.short.map(" -" + _).mkString.padTo(4, ' ') + option.parameters.mkString(", "))
    }
  }
  
  def process() = {
    try processArgs()
    catch {
      case e: Exception => 
        println(e.getMessage)
        printUsage()
    }
  }
  
}


object TestApp extends ServerApp("testapp") {
//	getClass.getClassLoader match {
//	  case cl : URLClassLoader => println(cl.getURLs.mkString("\n"))
//	}
//	println("CRC" + new Array[Byte](0).crc32)
	process()
}




