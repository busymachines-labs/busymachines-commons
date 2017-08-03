import sbt.Keys._

organization in ThisBuild := "com.busymachines"

scalaVersion in ThisBuild := "2.12.3"

crossScalaVersions in ThisBuild := Seq("2.11.11", "2.12.3") //"2.13.0-M1" -- scalatest not compiled for this version yet.

name := "busymachines-commons-core"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % Test withSources()
)
