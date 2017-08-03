import sbt._
import Keys._

object Dependencies {

  lazy val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.1"

  lazy val busymachinesCommonsCore: ModuleID = "com.busymachines" %% "busymachines-commons-core" % "0.1.0-SNAPSHOT"
}
