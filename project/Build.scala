import sbt._
import Keys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseCreateSrc
 
/**
 * gpg --keyserver hkp://pool.sks-keyservers.net  --no-permission-warning --send-keys 331928A8
 */
object BusyMachinesCommonsBuild extends Build {
 
  val es = "org.elasticsearch" % "elasticsearch" % "0.90.1" 
          
  lazy val project = Project(id = "busymachines-commons", base = file("."), settings = Project.defaultSettings ++ publishSettings ++ Seq(
    sbtPlugin := false,
    organization := "org.scalastuff",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.10.2",
    scalacOptions += "-deprecation",
    scalacOptions += "-unchecked",
    EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource,
    EclipseKeys.withSource := true,
    libraryDependencies += es,
    libraryDependencies += "io.spray" %% "spray-json" % "1.2.5" withSources(),
    libraryDependencies += "org.scalastuff" %% "esclient" % "0.20.3" withSources(),
    libraryDependencies += "org.clapper" %% "grizzled-slf4j" % "1.0.1" withSources(),
    libraryDependencies += "com.typesafe" % "config" % "1.0.0" withSources(),
    libraryDependencies += "joda-time" % "joda-time" % "2.2" withSources(),
    libraryDependencies += "org.joda" % "joda-convert" % "1.3.1" withSources() // for class file error in joda-time
    ))

  def publishSettings = Seq(
    licenses := Seq("The Apache Software Licence, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    homepage := Some(url("https://github.com/scalastuff/busymachines-commons")),
    pomIncludeRepository := { _ => false },
    publishMavenStyle := true,
    publishArtifact in Test := false,
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    pomExtra := <scm>
                  <connection>scm:git:git@github.com:scalastuff/busymachines-commons.git</connection>
                  <url>https://github.com/scalastuff/busymachines-commons</url>
                </scm>
                <developers>
                  <developer>
                    <id>ruudditerwich</id>
                    <name>Ruud Diterwich</name>
                    <url>https://github.com/rditerwich</url>
                  </developer>
                </developers>,
    publishTo <<= version { (v: String) =>
          val nexus = "https://oss.sonatype.org/"
          if (v.trim.endsWith("SNAPSHOT")) 
            Some("snapshots" at nexus + "content/repositories/snapshots") 
          else
            Some("releases"  at nexus + "service/local/staging/deploy/maven2")
        })        
}



