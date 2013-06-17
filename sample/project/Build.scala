import sbt._
import Keys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseCreateSrc
import com.github.retronym.SbtOneJar
 
object BigSaasBuild extends Build {


  val sprayVersion = "1.1-M7"
 
  val slick = "com.typesafe" % "slick_2.10.0-RC2" % "0.11.2" withSources
  val postgresql = "postgresql" % "postgresql" % "9.1-901.jdbc4" withSources()
  val scalatest = "org.scalatest" % "scalatest_2.10.0-RC2" % "2.0.M4-B2" % "test" withSources()
  val guava = "com.google.guava" % "guava" % "14.0.1" withSources()
  val config = "com.typesafe" % "config" % "1.0.0" withSources()
  val es = "org.elasticsearch" % "elasticsearch" % "0.90.1" withSources()
  val esclient = "org.scalastuff" %% "esclient" % "0.20.3" withSources()
  val grizzled = "org.clapper" %% "grizzled-slf4j" % "1.0.1" withSources()
  val logback = "ch.qos.logback" % "logback-classic" % "1.0.9" withSources()
  val sprayCan = "io.spray" % "spray-can" % sprayVersion  withSources()
  val sprayClient = "io.spray" % "spray-client" % sprayVersion  withSources()
  val sprayRouting = "io.spray" % "spray-routing" % sprayVersion withSources()
  val sprayJson = "io.spray" %% "spray-json" % "1.2.5" withSources()
  val sprayTest = "io.spray" % "spray-testkit" % sprayVersion % "test" withSources()
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % "2.1.2" withSources()
  val akkaRemote = "com.typesafe.akka" %% "akka-remote" % "2.1.2" withSources()
  val hazelcast = "com.hazelcast" % "hazelcast" % "2.5" withSources()
  val joda = "joda-time" % "joda-time" % "2.2" withSources()
  val jodaConvert = "org.joda" % "joda-convert" % "1.3.1" withSources() // for class file error in joda-time
  val uuid = "com.eaio.uuid" % "uuid" % "3.4" withSources()
  val jsr305 = "com.google.code.findbugs" % "jsr305" % "2.0.1" // for class file error in guava

  def defaultSettings =
    Project.defaultSettings ++
    SbtOneJar.oneJarSettings ++
      Seq(
        sbtPlugin := false,
        organization := "com.kentivo",
        version := "1.0.0-SNAPSHOT",
        scalaVersion := "2.10.1",
        publishMavenStyle := false,
        scalacOptions += "-deprecation",
        scalacOptions += "-unchecked",
        libraryDependencies += scalatest,
        EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource,
        EclipseKeys.withSource := true)

  val mdm = Project(id = "mdm", base = file("."), settings = defaultSettings ++ Seq(
 mainClass in (Compile, run) := Some("com.kentivo.mdm.ui.UiServer"),
    libraryDependencies ++= Seq(es, esclient, joda, jodaConvert, /*sprayCan, sprayClient, sprayRouting, sprayJson,*/ hazelcast, akkaActor, akkaRemote, sprayTest, config, grizzled, guava, uuid, logback, jsr305)))

}
