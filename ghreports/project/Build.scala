import sbt._
import Keys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseCreateSrc
import com.github.retronym.SbtOneJar
import less.Plugin._

object GhreportsBuild extends Build {

  val commons = "org.scalastuff" %% "busymachines-commons" % "0.0.1-SNAPSHOT" withSources() 

  def defaultSettings =
    Project.defaultSettings ++
    SbtOneJar.oneJarSettings ++
    lessSettings ++ 
      Seq(
//          LessKeys.filter in (Compile, LessKeys.less) := "public/less/ghreports.less",
        sbtPlugin := false,
        organization := "com.busymachines",
        version := "1.0.0-SNAPSHOT",
        scalaVersion := "2.10.1",
        publishMavenStyle := false,
        scalacOptions += "-deprecation",
        scalacOptions += "-unchecked",
        EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource,
        EclipseKeys.withSource := true)

  val ghreports = Project(id = "ghreports", base = file("."), settings = defaultSettings ++ Seq(
 mainClass in (Compile, run) := Some("com.busymachines.ghreports.Main"),
    libraryDependencies ++= Seq(commons)))

}
