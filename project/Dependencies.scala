import sbt._
import Keys._

object Dependencies {

  lazy val commonsVersion = "0.1.0-SNAPSHOT"

  lazy val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.1" withSources()

  lazy val busymachinesCommonsCore: ModuleID = "com.busymachines" %% "busymachines-commons-core" % commonsVersion withSources()
}
