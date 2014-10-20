import sbt._
import Keys._
import com.typesafe.sbt.SbtSite.site
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseCreateSrc

object BusyMachinesCommonsBuild extends Build {

  lazy val project = Project(id = "busymachines-commons", base = file("."), settings = Seq(
    sbtPlugin := false,
    organization := "com.busymachines",
    version := "0.6.1-SNAPSHOT"
  ) ++
    Defaults.defaultSettings ++
    compilerSettings ++
    eclipseSettings ++
    publishSettings ++
    dependencies ++
    runSettings ++
    site.settings ++
    site.sphinxSupport() ++ site.includeScaladoc()
  )

  def compilerSettings = Seq(
    scalaVersion := "2.11.2",
    crossScalaVersions := Seq("2.11.2", "2.10.4"),
    javacOptions in Compile ++= Seq("-encoding", "utf8", "-g"),
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-encoding", "utf8"),
    //    scalacOptions ++= Seq("-deprecation", "-unchecked", "-encoding", "utf8", "-feature", "-language:implicitConversions", "-language:postfixOps", "-language:higherKinds", "-language:existentials", "-language:reflectiveCalls"),
    //    scalacOptions <++= scalaBinaryVersion map {
    //      case "2.11" => Seq("-Ydelambdafy:method")
    //      case _ => Nil
    //    },
    incOptions := incOptions.value.withNameHashing(nameHashing = true)
  )

  def runSettings = Seq(
    parallelExecution in Test := false)

  def eclipseSettings = Seq(
    EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource,
    EclipseKeys.withSource := true
  )

  def dependencies = Seq(
    resolvers += Resolver.url("busymachines snapshots", url("http://archiva.busymachines.com/repository/snapshots/"))(Resolver.ivyStylePatterns),
    resolvers += "spray repo" at "http://repo.spray.io",
    resolvers += "spray nightlies repo" at "http://nightlies.spray.io",
    resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    libraryDependencies += "junit" % "junit" % "4.11" % "test" withSources(),
    libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test" withSources(),
    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.4" withSources(),
    libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.3.4" withSources(),
    libraryDependencies += "com.typesafe.akka" %% "akka-contrib" % "2.3.3" withSources(),
    libraryDependencies += "org.elasticsearch" % "elasticsearch" % "1.3.0" withSources(),
    libraryDependencies += "org.scalastuff" %% "esclient" % "1.3.0" withSources(),
    libraryDependencies += "org.scalastuff" %% "json-parser" % "2.0.2" withSources(),
    libraryDependencies += "org.clapper" %% "grizzled-slf4j" % "1.0.2" withSources(),

    libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.0.1" withSources(),
    libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % "2.0.1" withSources(),
    libraryDependencies += "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.0" withSources(),
    libraryDependencies += "joda-time" % "joda-time" % "2.3" withSources(),
    libraryDependencies += "org.joda" % "joda-convert" % "1.6" withSources(), // for class file error in joda-time
    libraryDependencies += "javax.mail" % "mail" % "1.4.5" withSources(),
    libraryDependencies += "commons-codec" % "commons-codec" % "1.9", // just for base64, can be removed when we switch to java 1.8
    libraryDependencies += "com.netaporter.salad" %% "salad-metrics-core" % "0.2.7" withSources(),
    libraryDependencies <++= scalaBinaryVersion {
      case "2.11" => Seq(
        "io.spray" %% "spray-json" % "1.2.6" withSources(),
        "io.spray" %% "spray-can" % "1.3.1" withSources(),
        "io.spray" %% "spray-http" % "1.3.1" withSources(),
        "io.spray" %% "spray-client" % "1.3.1" withSources(),
        "io.spray" %% "spray-servlet" % "1.3.1" withSources(),
        "io.spray" %% "spray-routing" % "1.3.1" withSources(),
        "io.spray" %% "spray-caching" % "1.3.1" withSources(),
        "io.spray" %% "spray-testkit" % "1.3.1" % "test" withSources())
      case _ => Seq(
        "io.spray" %% "spray-json" % "1.2.6" withSources(),
        "io.spray" % "spray-can" % "1.3.1" withSources(),
        "io.spray" % "spray-http" % "1.3.1" withSources(),
        "io.spray" % "spray-client" % "1.3.1" withSources(),
        "io.spray" % "spray-servlet" % "1.3.1" withSources(),
        "io.spray" % "spray-routing" % "1.3.1" withSources(),
        "io.spray" % "spray-caching" % "1.3.1" withSources(),
        "io.spray" % "spray-testkit" % "1.3.1" % "test" withSources())
    }
  )

  def publishSettings = Seq(
    licenses := Seq("The Apache Software Licence, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    homepage := Some(url("https://github.com/busymachines/busymachines-commons")),
    pomIncludeRepository := { _ => false },
    publishMavenStyle := true,
    publishArtifact in Test := false,
    publishMavenStyle := false,
    exportJars := true,
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
        <developer>
          <id>lorandszakacs</id>
          <name>Lorand Szakacs</name>
          <url>https://github.com/lorandszakacs</url>
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



