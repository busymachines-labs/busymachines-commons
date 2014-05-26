package com.busymachines.commons.cli

import com.busymachines.commons.Implicits._
import java.io.File
import java.net.URLClassLoader

object InstallCommand {
  
  def install(name: String, description: String, user: Option[String], vmArgs: String, app: App, args: String) = {
    if (new File("/etc/init.d").isDirectory) {
      InstallOnUbuntuCommand.install(name, description, user, vmArgs, app, args)
    }
    else if (new File("/Library/LaunchDaemons").isDirectory) {
      InstallOnUbuntuCommand.install(name, description, user, vmArgs, app, args)
//      InstallOnOsxCommand.install(name, description, user)
    }
    else {
      throw new Exception("Couldn't install application: unrecognized environment")
    }
  }
   
 def copyJars(dest: File): List[String] = {
    getClass.getClassLoader match {
      case cl : URLClassLoader => 
        for {
          url <- cl.getURLs.toList
          fileName = url.fileName if fileName.endsWith(".jar")
          file = new File(dest, fileName) 
          _ = println(s"copying file $fileName")
          _ = url.copyTo(new File(dest, fileName))
        } yield fileName
      case _ => Nil
    }
  }
}


