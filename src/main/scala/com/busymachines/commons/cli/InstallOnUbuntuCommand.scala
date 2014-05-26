package com.busymachines.commons.cli

import java.io.File
import com.busymachines.commons.Implicits._

object InstallOnUbuntuCommand {
  import InstallCommand._

  def install(name: String, description: String, user: Option[String], vmArgs: String, app: App, args: String) = {
    println(s"Installing $name")
    
    val appDir = new File("/opt", name)
    val libDir = new File(appDir, "lib")
    val binDir = new File(appDir, "bin")
    libDir.mkdirs()
    binDir.mkdirs()
    val jars = copyJars(libDir)
    val startScriptContent = mkStartScript(vmArgs, new File(appDir, "log/output.log").toString, jars.mkString(","), app.getClass.getName, args)
    val startScript = new File(binDir, name + ".sh")
    startScriptContent.copyTo(startScript)
    val initScriptContent = mkInitScript(name, description, startScript.getCanonicalPath, user.getOrElse("root"))
    val initScript = new File("/etc/init.d/", name)
    initScriptContent.copyTo(initScript)

  }

  def mkStartScript(vmArgs: String, logfile: String, jars: String, mainClass: String, args: String) = {
    startScriptTemplate.
      replace("<VM_ARGS>", vmArgs).
      replace("<LOGFILE>", logfile).
      replace("<JARS>", vmArgs).
      replace("<MAIN_CLASS>", mainClass).
      replace("<ARGS>", args)
  }

  def mkInitScript(name: String, description: String, command: String, user: String) =
    initScriptTemplate.
      replaceAll("<NAME>", name).
      replaceAll("<DESCRIPTION>", description).
      replaceAll("<COMMAND>", command).
      replaceAll("<USERNAME>", user)

  val startScriptTemplate = """
#!/bin/sh
java <VM_ARGS> -Dlogfile=<LOGFILE> -cp <JARS> <MAIN_CLASS> <ARGS>
"""

  val initScriptTemplate = """
#!/bin/sh
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