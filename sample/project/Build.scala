
import sbt._

import Keys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseCreateSrc
//import com.github.retronym.SbtOneJar
import less.Plugin._
import com.busymachines.WebPlugin._

object SampleBuild extends Build {

  val commons = "com.busymachines" %% "commons" % "0.0.1-SNAPSHOT" withSources() changing()

  def defaultSettings =
    Project.defaultSettings ++
//    SbtOneJar.oneJarSettings ++
    lessSettings ++ 
//    yuiCompressor.Plugin.yuiSettings ++ 
//    sbtclosure.SbtClosurePlugin.closureSettings ++
    webSettings ++
      Seq(
        sbtPlugin := false,
        organization := "com.busymachines",
        version := "1.0.0-SNAPSHOT",
        scalaVersion := "2.10.1",
        publishMavenStyle := false,
        scalacOptions += "-deprecation",
        scalacOptions += "-unchecked",
//        sourceDirectory in (Compile, sbtclosure.SbtClosurePlugin.ClosureKeys.closure) := file("src/main/resources/public"),
        EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource,
        EclipseKeys.withSource := true)

  val sample = Project(id = "sample", base = file("."), settings = defaultSettings ++ Seq(
 mainClass in (Compile, run) := Some("com.kentivo.mdm.ui.UiServer"),
    libraryDependencies ++= Seq(commons)))

}
