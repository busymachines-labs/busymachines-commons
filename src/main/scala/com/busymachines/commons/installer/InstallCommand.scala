package com.busymachines.commons.installer

import java.io.{File, FileWriter}
import java.net.{URL, URLClassLoader}
import java.nio.file.attribute.{PosixFilePermission, UserPrincipal}
import java.nio.file.{FileSystems, Files}

import com.busymachines.commons.Implicits._

import scala.collection.JavaConversions._
import scala.collection.mutable.HashSet
import scala.io.Source

object InstallCommand {
  
  val genmarker = "### This file has been generated ###"

  def install(name: String, description: String, version: String, user: Option[String], vmArgs: String, app: App, args: String, appendToLog: Boolean = false) = {
    if (new File("/etc/init.d").isDirectory) {
      InstallOnUbuntuCommand.install(name, description, version, user, vmArgs, app, args, appendToLog)
    }
    else if (new File("/Library/LaunchDaemons").isDirectory) {
      InstallOnUbuntuCommand.install(name, description, version, user, vmArgs, app, args, appendToLog)
//      InstallOnOsxCommand.install(name, description, user)
    }
    else {
      throw new Exception("Couldn't install application: unrecognized environment")
    }
  }

  def getUserPrincipal(user: Option[String]) = {
    val principalLookupService = FileSystems.getDefault.getUserPrincipalLookupService
    try user.map(principalLookupService.lookupPrincipalByName)
    catch {
      case e: Exception => throw new Exception("user '" + user.get + "' does not exist")
    }
  }

  def setOwner(file: File, user: Option[UserPrincipal]) =
    if (user.isDefined)
      try Files.setOwner(file.toPath, user.get)
      catch { case e: Exception => throw new Exception("couldn't set owner of " + file + ": " + e.getMessage) }

  def setPermissions(file: File, permissions: PosixFilePermission*) =
    try Files.setPosixFilePermissions(file.toPath, Set(permissions:_*))
    catch { case e: Exception => throw new Exception("couldn't set permissions of " + file + ": " + e.getMessage) }

  def copyJars(dest: File): List[String] = {
    var bootEntries = new HashSet[URL]
    ClassLoader.getSystemClassLoader.getParent match {
      case cl: URLClassLoader =>
//        for (entry <- cl.getURLs) println("boot entry: " + entry)
        bootEntries ++= cl.getURLs
      case _ =>
    }
    getClass.getClassLoader match {
      case cl: URLClassLoader =>
        for {
          url <- cl.getURLs.toList if !bootEntries.contains(url)
          fileName = url.fileName if !url.toString.contains("/jre/lib/")
          file = new File(dest, fileName)
          _ = println(s"Copying $file")
          _ = if (!file.exists || file.isFile) {
            dest.mkdirs()
            url.copyTo(new File(dest, fileName))
          }
        } yield fileName
      case _ => Nil
    }
  }

  // Copy application.conf
  def writeApplicationConf(dest: File) {
    if (!dest.exists) {
      dest.getParentFile.mkdirs()
      val writer = new FileWriter(dest)
      try {
        for (line <- Source.fromURL(getClass.getClassLoader.getResource("application.conf")).getLines()) {
          if (line.trim.nonEmpty && !line.trim.startsWith("#"))
            writer.append("#")
          writer.append(line).append("\n")
        }
        println(s"Created $dest")
      } finally {
        writer.close()
      }
    } else {
      println(s"Kept existing file $dest")
    }
  }

  def writeResource(resource: String, dest: File) {
    if (!dest.exists) {
      dest.getParentFile.mkdirs()
      val cpResource = getClass.getClassLoader.getResource(resource)
      if (cpResource == null)
        throw new Exception(s"Classpath resource not found: $resource")
      cpResource.copyTo(dest)
      println(s"Created $dest")
    } else {
      println(s"Kept existing file $dest")
    }
  }

  def canOverwrite(file: File) =
    !file.exists || hasGenMarker(file)
    
  def hasGenMarker(file: File) = 
    file.isFile && Source.fromFile(file).getLines().exists(_.contains(genmarker))

}


