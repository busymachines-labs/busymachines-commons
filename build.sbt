import sbt._
import Keys._

/**
  * * All instructions for publishing to sonatype can be found in the
  * ``z-publishing-artifcats/README.md`` folder.
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 31 Jul 2017
  */


/**
  * this is used when a module depends on another, and it explicitly
  * states that the "compile", i.e. the sources, of a module depend
  * on the sources of another module. And the same thing for tests,
  * otherwise the dependencies between the tests are not created.
  */
val `compile->compile;test->test` = "compile->compile;test->test"

/**
  * this is a phantom project that is simply supposed to aggregate all modules for convenience,
  * it is NOT published as an artifact. It doesn't have any source files, it is just a convenient
  * way to propagate all commands to the modules via the aggregation
  */
lazy val root = Project(
  id = "busymachines-commons",
  base = file("."))
  .settings(
    publishArtifact in ThisProject := false
  )
  .aggregate(
    core,
    json
  )

lazy val core = project
  .settings(Settings.commonSettings)
  .settings(PublishingSettings.sonatypeSettings)
  .settings(
    name in ThisProject := "busymachines-commons-core",
    libraryDependencies += Dependencies.scalaTest % Test withSources()
  )

lazy val json = project
  .settings(Settings.commonSettings)
  .settings(PublishingSettings.sonatypeSettings)
  .settings(
    name in ThisProject := "busymachines-commons-json",
    libraryDependencies ++=
      Dependencies.circe.map(c => c withSources()) ++ Seq(
        Dependencies.shapeless withSources(),
        Dependencies.cats withSources(),

        Dependencies.scalaTest % Test withSources()
      )
  )
  .dependsOn(
    core
  )
  .aggregate(
    core
  )

lazy val rest = project
  .settings(Settings.commonSettings)
  .settings(PublishingSettings.sonatypeSettings)
  .settings(
    name in ThisProject := "busymachines-commons-rest",
    libraryDependencies ++= Nil
  )
  .dependsOn(
    core
  )
  .aggregate(
    core
  )
