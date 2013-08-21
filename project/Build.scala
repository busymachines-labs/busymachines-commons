import sbt._
import Keys._
import com.typesafe.sbt.SbtSite.site
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseCreateSrc

/**
 * gpg --keyserver hkp://pool.sks-keyservers.net  --no-permission-warning --send-keys 331928A8
 */
object BusyMachinesCommonsBuild extends Build {
 
  lazy val project = Project(id = "commons", base = file("."), settings = 
    Project.defaultSettings ++ 
    publishSettings ++
    site.settings ++ 
    site.sphinxSupport() ++ site.includeScaladoc() ++
    Seq(
    sbtPlugin := false,
    publishMavenStyle := false,
    exportJars := true,      
    organization := "com.busymachines",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.10.2",
    scalacOptions += "-deprecation",
    scalacOptions += "-unchecked",
    EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource,
    EclipseKeys.withSource := true,
    resolvers += "spray repo" at "http://nightlies.spray.io",
    resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0.M5b" withSources(),
    libraryDependencies +=  "org.elasticsearch" % "elasticsearch" % "0.90.2" withSources(),
    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.2.0-RC2" withSources(),
    libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % "2.2.0-RC2" withSources(),
    libraryDependencies += "com.typesafe.akka" %% "akka-contrib" % "2.2.0-RC2" withSources(),
    libraryDependencies += "io.spray" % "spray-can" % "1.2-20130719" withSources(),
    libraryDependencies += "io.spray" % "spray-routing" % "1.2-20130719" withSources(),
    libraryDependencies += "io.spray" % "spray-client" % "1.2-20130719" withSources(),
    libraryDependencies += "io.spray" %%  "spray-json" % "1.2.5" withSources(),
    libraryDependencies += "io.spray" % "spray-servlet" % "1.2-20130719" withSources(),
    libraryDependencies += "io.spray" % "spray-routing" % "1.2-20130719" withSources(),
    libraryDependencies += "io.spray" % "spray-testkit" % "1.2-20130719" % "test" withSources(),
    libraryDependencies += "io.spray" % "spray-caching" % "1.2-20130719" withSources(),
    libraryDependencies += "org.scalastuff" %% "esclient" % "0.20.3" withSources(),
    libraryDependencies += "org.clapper" %% "grizzled-slf4j" % "1.0.1" withSources(),
    libraryDependencies += "org.clapper" %% "argot" % "1.0.1" withSources(),
    libraryDependencies +=  "ch.qos.logback" % "logback-classic" % "1.0.13" withSources(),
    libraryDependencies += "com.typesafe" % "config" % "1.0.0" withSources(),
    libraryDependencies += "joda-time" % "joda-time" % "2.2" withSources(),
    libraryDependencies += "org.joda" % "joda-convert" % "1.3.1" withSources(), // for class file error in joda-time
    libraryDependencies += "com.google.guava" % "guava" % "14.0.1" withSources(),
    libraryDependencies += "com.google.code.findbugs" % "jsr305" % "2.0.1" // for class file error in guava
))

  def publishSettings = Seq(
    licenses := Seq("The Apache Software Licence, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    homepage := Some(url("https://github.com/busymachines/busymachines-commons")),
    pomIncludeRepository := { _ => false },
    publishMavenStyle := true,
    publishArtifact in Test := false,
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    pomExtra := <scm>
                  <connection>scm:git:git@github.com:busymachines/busymachines-commons.git</connection>
                  <url>https://github.com/busymachines/busymachines-commons</url>
                </scm>
                <developers>
                  <developer>
                    <id>ruudditerwich</id>
                    <name>Ruud Diterwich</name>
                    <url>https://github.com/rditerwich</url>
                  </developer>
                  <developer>
                    <id>paulsabou</id>
                    <name>Paul Sabou</name>
                    <url>https://github.com/paulsabou</url>
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



