import sbt._
import Keys._

/**
  * this is used when a module depends on another, and it explicitly
  * states that the "compile", i.e. the sources, of a module depend
  * on the sources of another module. And the same thing for tests,
  * otherwise the dependencies between the tests are not created.
  */
val `compile->compile;test->test` = "compile->compile;test->test"

name in ThisProject := "busymachines-commons"

lazy val core = project.settings(Settings.commonSettings)
  .settings(
    libraryDependencies += Dependencies.scalaTest % Test withSources()
  )
