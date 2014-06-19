package com.busymachines.commons.installer

import java.io.File
import java.nio.file.attribute.PosixFilePermission._

import com.busymachines.commons.Implicits._

object InstallOnUbuntuCommand {
  import com.busymachines.commons.installer.InstallCommand._

  def install(name: String, description: String, version: String, user: Option[String], vmArgs: String, app: App, args: String) {
    try install2(name, description, version, user, vmArgs, app, args)
    catch { 
      case e: Exception =>
        println("Error: " + e.getMessage)
      }
    }
  
  def install2(name: String, description: String, version: String, userName: Option[String], vmArgs: String, app: App, args: String) {
    println(s"Installing $name")
    
    // check user principal
    val user = getUserPrincipal(userName)

    // make sure we have root rights
    val libDir = new File("/var/lib/" + name)
    libDir.mkdirs()
    setOwner(libDir, user)

    // copy jars into lib
    val jars = copyJars(libDir)
    val jarFileNames = jars.map(new File(libDir, _).getAbsolutePath).mkString(",")
    val appClassName = app.getClass.getName.stripSuffix("$")


    // write start script
    val startScript = new File("/usr/bin/" + name)
    if (canOverwrite(startScript)) {
      startScript.getParentFile.mkdirs()

      val startScriptContent = s"""#!/bin/bash
$genmarker
java $vmArgs -Dconfig.file=/etc/$name.conf -Dlogback.configurationFile=/etc/$name.d/cli-logback.xml -cp $jarFileNames $appClassName $$*
"""

      startScriptContent.copyTo(startScript)
      setPermissions(startScript, OWNER_EXECUTE, OWNER_READ, OWNER_WRITE, GROUP_EXECUTE, GROUP_READ, OTHERS_EXECUTE, OTHERS_READ)
      println("Created " + startScript)
    } else {
      println("Warning: existing file not overwritten: " + startScript)
    }

    // write init script
    val initScript = new File("/etc/init.d/", name)
    if (canOverwrite(initScript)) {
      initScript.getParentFile.mkdirs()
      try {

        val command = s"java $vmArgs -Dconfig.file=/etc/$name.conf -Dlogback.configurationFile=/etc/$name.d/logback.xml -cp $jarFileNames $appClassName $args"

        val initScriptContent = mkInitScript(name, description, command, "", userName.getOrElse("root"))
        initScriptContent.copyTo(initScript)
        setPermissions(initScript, OWNER_EXECUTE, OWNER_READ, OWNER_WRITE, GROUP_EXECUTE, GROUP_READ, OTHERS_EXECUTE, OTHERS_READ)
        println("Created " + initScript)
      } catch {
        case e: Exception => println("Warning: couldn't create: " + initScript + ": " + e.getMessage)
      }
    } else {
      println("Warning: existing file not overwritten: " + initScript)
    }

    // write application.conf
    writeApplicationConf(new File("/etc/" + name + ".conf"))

    // write logback.xml
    writeResource("logback.xml", new File("/etc/" + name + ".d/logback.xml"))
    writeResource("cli-logback.xml", new File("/etc/" + name + ".d/cli-logback.xml"))

    // change owner of log file
    val logFile = new File(s"/var/log/$name.log")
    if (!logFile.exists()) "".copyTo(logFile)
    setOwner(logFile, user)
  }
  
  def mkInitScript(name: String, description: String, command: String, args: String, user: String) =
    initScriptTemplate.
      replace("<GEN_MARKER>", genmarker).
      replaceAll("<NAME>", name).
      replaceAll("<DESCRIPTION>", description).
      replaceAll("<COMMAND>", command).
      replaceAll("<USER>", user).
      replace("<ARGS>", args)


  val initScriptTemplate = """#!/bin/bash
<GEN_MARKER>
### BEGIN INIT INFO
# Provides:          <NAME>
# Required-Start:    cassandra elasticsearch networking
# Required-Stop:     cassandra elasticsearch networking
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Description:       <DESCRIPTION>
### END INIT INFO

SCRIPT="<COMMAND> <ARGS>"
RUNAS=<USER>

PIDFILE=/var/run/<NAME>.pid
LOGFILE=/var/log/<NAME>.log

start() {
  if [ -f $PIDFILE ] && kill -0 $(cat $PIDFILE); then
    echo 'Service already running' >&2
    return 1
  fi
  echo 'Starting <NAME>...' >&2
  local CMD="$SCRIPT > \"$LOGFILE\" & echo \$!"
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