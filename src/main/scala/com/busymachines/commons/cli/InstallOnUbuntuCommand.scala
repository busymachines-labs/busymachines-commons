package com.busymachines.commons.cli

import java.io.File
import com.busymachines.commons.Implicits._
import scala.io.Source
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import java.nio.file.attribute.PosixFilePermission._
import scala.collection.JavaConversions._
import java.nio.file.attribute.UserPrincipal
import java.nio.file.FileSystem
import java.nio.file.FileSystems

object InstallOnUbuntuCommand {
  import InstallCommand._

  def install(name: String, description: String, version: String, user: Option[String], vmArgs: String, app: App, args: String) = {
    println(s"Installing $name")
    
    // check user principal
    val principalLookupService = FileSystems.getDefault.getUserPrincipalLookupService()
    val userPrincipal = 
      try user.map(principalLookupService.lookupPrincipalByName)
      catch {
        case e: Exception => 
          println("Warning: user '" + user.get + "' does not exist: " + user.get) 
          None 
      }
    
    val appDir = new File("/opt", name)
    val libDir = new File(appDir, "lib")
    val binDir = new File(appDir, "bin")
    libDir.mkdirs()
    val jars = copyJars(libDir)
    
    // write start script
    val startScript = new File(binDir, name + ".sh")
    try {
      if (canOverwrite(startScript)) {
        startScript.getParentFile.mkdirs()
        val startScriptContent = mkStartScript(appDir, vmArgs, jars.mkString(","), app.getClass.getName.stripSuffix("$"), args)
        startScriptContent.copyTo(startScript)
        Files.setPosixFilePermissions(startScript.toPath, Set(OWNER_EXECUTE, OWNER_READ, OWNER_WRITE))
        if (userPrincipal.isDefined)
          Files.setOwner(startScript.toPath, userPrincipal.get)
        println("Created: " + startScript)
      } else {
        println("Warning: existing file not overwritten: " + startScript)
      }
    } catch {
      case e: Exception => println("Warning: couldn't create: " + startScript + ": " + e.getMessage)
    }

    // write init script
    val initScript = new File("/etc/init.d/", name)
    try {
      if (canOverwrite(initScript)) {
        val initScriptContent = mkInitScript(name, description, startScript.getCanonicalPath, user.getOrElse("root"))
        initScriptContent.copyTo(initScript)
        println("Created: " + initScript)
      } else {
        println("Warning: existing file not overwritten: " + initScript)
      }
    } catch {
      case e: Exception => println("Warning: couldn't create: " + initScript + ": " + e.getMessage)
    }
  }
  
  def mkStartScript(appDir: File, vmArgs: String, jars: String, mainClass: String, args: String) = {
    startScriptTemplate.
      replace("<GEN_MARKER>", genmarker).
      replaceAll("<APP_DIR>", appDir.toString).
      replace("<VM_ARGS>", vmArgs).
      replace("<JARS>", jars).
      replace("<MAIN_CLASS>", mainClass).
      replace("<ARGS>", args)
  }

  def mkInitScript(name: String, description: String, command: String, user: String) =
    initScriptTemplate.
      replace("<GEN_MARKER>", genmarker).
      replaceAll("<NAME>", name).
      replaceAll("<DESCRIPTION>", description).
      replaceAll("<COMMAND>", command).
      replaceAll("<USERNAME>", user)

  val startScriptTemplate = """#!/bin/sh
<GEN_MARKER>
cd <APP_DIR>
java <VM_ARGS> -Dconfig.file=conf/application.conf -Dlogback.configurationFile=conf/logback.xml -cp <JARS> <MAIN_CLASS> <ARGS>
"""

  val initScriptTemplate = """#!/bin/sh
<GEN_MARKER>
### BEGIN INIT INFO
# Provides:          <NAME>
# Required-Start:    $local_fs $network $named $time $syslog
# Required-Stop:     $local_fs $network $named $time $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Description:       <DESCRIPTION>
### END INIT INFO

SCRIPT=<COMMAND>
RUNAS=<USERNAME>

PIDFILE=/var/run/<NAME>.pid
LOGFILE=/var/log/<NAME>.log

start() {
  if [ -f $PIDFILE ] && kill -0 $(cat $PIDFILE); then
    echo 'Service already running' >&2
    return 1
  fi
  echo 'Starting <NAME>...' >&2
  local CMD="$SCRIPT &> \"$LOGFILE\" & echo \$!"
  su -c "$CMD" $RUNAS > "$PIDFILE"
  echo 'Service started' >&2
}

stop() {
  if [ ! -f "$PIDFILE" ] || ! kill -0 $(cat "$PIDFILE"); then
    echo '<NAME> not running' >&2
    return 1
  fi
  echo 'Stopping <NAME>...' >&2
  kill -15 $(cat "$PIDFILE") && rm -f "$PIDFILE"
  echo 'Service stopped' >&2
}

uninstall() {
  echo -n "Are you really sure you want to uninstall <NAME>? That cannot be undone. [yes|No] "
  local SURE
  read SURE
  if [ "$SURE" = "yes" ]; then
    stop
    rm -f "$PIDFILE"
    echo "Notice: log file is not be removed: '$LOGFILE'" >&2
    update-rc.d -f <NAME> remove
    rm -fv "$0"
  fi
}

case "$1" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  uninstall)
    uninstall
    ;;
  restart)
    stop
    start
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|uninstall}"
esac
"""

}