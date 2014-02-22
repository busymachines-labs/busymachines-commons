import sbt._
import Keys._
import com.typesafe.sbt.SbtSite.site
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseCreateSrc

/**
 * gpg --keyserver hkp://pool.sks-keyservers.net  --no-permission-warning --send-keys 331928A8
 */
object BusyMachinesCommonsBuild extends Build {
 
  val sprayVersion = "1.2.0"
  
  lazy val project = Project(id = "busymachines-commons", base = file("."), settings = 
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
    scalaVersion := "2.10.3",
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-encoding", "utf8", "-feature", "-language:implicitConversions", "-language:postfixOps", "-language:reflectiveCall", "-language:higherKinds", "-language:existentials", "-language:reflectiveCalls"),
    EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource,
    EclipseKeys.withSource := true,
    resolvers += Resolver.url("busymachines snapshots", url("http://archiva.busymachines.com/repository/snapshots/"))(Resolver.ivyStylePatterns),
    resolvers += "spray repo" at "http://repo.spray.io",
    resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    libraryDependencies += "junit" % "junit" % "4.11" % "test" withSources(),
    libraryDependencies += "org.scalatest" %% "scalatest" % "2.0" % "test" withSources(),
    libraryDependencies += "org.specs2" %% "specs2" % "2.3.8" % "test" withSources(),
    libraryDependencies += "org.pegdown" % "pegdown" % "1.4.1" % "test" withSources(), // used by scalatest
    libraryDependencies += "org.elasticsearch" % "elasticsearch" % "0.90.10" withSources(),
    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.2.3" withSources(),
    libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.2.3" withSources(),
    libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % "2.2.3" withSources(),
    libraryDependencies += "com.typesafe.akka" %% "akka-contrib" % "2.2.3" withSources(),
    libraryDependencies += "io.spray" % "spray-can" % sprayVersion withSources(),
    libraryDependencies += "io.spray" % "spray-client" % sprayVersion withSources(),
    libraryDependencies += "io.spray" %%  "spray-json" % "1.2.5" withSources(),
    libraryDependencies += "io.spray" % "spray-servlet" % sprayVersion withSources(),
    libraryDependencies += "io.spray" % "spray-routing" % sprayVersion withSources(),
    libraryDependencies += "io.spray" % "spray-testkit" % sprayVersion % "test" withSources(),
    libraryDependencies += "io.spray" % "spray-caching" % sprayVersion withSources(),
    libraryDependencies += "org.scalastuff" %% "esclient" % "0.20.3" withSources(),
    libraryDependencies += "org.clapper" %% "grizzled-slf4j" % "1.0.1" withSources(),
    libraryDependencies += "org.clapper" %% "argot" % "1.0.1" withSources(),
    libraryDependencies +=  "ch.qos.logback" % "logback-classic" % "1.0.13" withSources(),
    libraryDependencies += "com.typesafe" % "config" % "1.0.0" withSources(),
    libraryDependencies += "joda-time" % "joda-time" % "2.3" withSources(),
    libraryDependencies += "org.joda" % "joda-convert" % "1.5" withSources(), // for class file error in joda-time
    libraryDependencies += "org.apache.commons" % "commons-email" % "1.3.2" withSources(),
    libraryDependencies += "javax.mail" % "mail" % "1.4.5" withSources(),
    libraryDependencies += "com.google.guava" % "guava" % "14.0.1" withSources(),
    libraryDependencies += "com.google.code.findbugs" % "jsr305" % "2.0.1", // for class file error in guava
    libraryDependencies += "org.apache.pdfbox" % "pdfbox" % "1.8.3" withSources(),
    libraryDependencies += "com.google.zxing" % "javase" % "2.3.0" withSources()
  ))

  def publishSettings = Seq(
    licenses := Seq("The Apache Software Licence, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    homepage := Some(url("https://github.com/busymachines/busymachines-commons")),
    pomIncludeRepository := { _ => false },
    publishMavenStyle := true,
    publishArtifact in Test := false,
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials_busymachines_snapshots"),
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials_busymachines_releases"),
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
          val nexus = "http://archiva.busymachines.com"
          if (v.trim.endsWith("SNAPSHOT")) 
            Some(Resolver.url("snapshots", new URL(nexus + "/repository/snapshots/"))(Resolver.ivyStylePatterns))
          else
            Some(Resolver.url("snapshots", new URL(nexus + "/repository/releases/"))(Resolver.ivyStylePatterns))
        })
                
}



