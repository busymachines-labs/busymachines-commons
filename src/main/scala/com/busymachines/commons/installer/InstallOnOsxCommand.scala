package com.busymachines.commons.installer

import java.io.File
import scala.io.Source
import com.busymachines.commons.Implicits._

object InstallOnOsxCommand {
  import InstallCommand._
  
  def install(name: String, description: String, user: Option[String]) = {
    
    val installDir = new File("/Applications/" + name.capitalize)
    val libDir = new File(installDir, "lib")
    libDir.mkdirs()
    val jars = copyJars(libDir)       
    val binDir = new File(installDir, "bin")
    binDir.mkdirs()
    
    val startScript = new File(binDir, name + ".sh")
//    val initScript = InstallOnUbuntuCommand.initScript(name, description, startScript.getCanonicalPath, user.getOrElse("root"))
//    initScript.copyTo(new File(binDir, name + ".sh"))
  }
}