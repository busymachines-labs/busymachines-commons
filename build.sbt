import sbt._
import Keys._

lazy val currentSnapshotVersion = "0.3.0-SNAPSHOT"
addCommandAlias("setSnapshotVersion", s"""set version in ThisBuild := "$currentSnapshotVersion"""")

addCommandAlias("build",           ";compile;Test/compile")
addCommandAlias("rebuild",         ";clean;update;compile;Test/compile")
addCommandAlias("ci",              ";scalafmtCheck;rebuild;coverage;test;coverageReport")
addCommandAlias("ci-quick",        ";scalafmtCheck;build;coverage;test;coverageReport")
addCommandAlias("doLocal",         ";rebuild;publishLocal")
addCommandAlias("doSnapshotLocal", ";rebuild;setSnapshotVersion;publishLocal")

addCommandAlias("mkSite",      ";docs/makeMicrosite")
addCommandAlias("publishSite", ";docs/publishMicrosite")

addCommandAlias("doCoverage",       ";rebuild;coverage;test;coverageReport")
addCommandAlias("doCoverage-quick", ";build;coverage;test;coverageReport")

/**
  * Use with care. Releases a snapshot to sonatype repository.
  *
  * Currently this will not work properly because of an SBT bug where
  * the artifacts are not overriden in the SONATYPE repo:
  * https://github.com/sbt/sbt/issues/3725
  *
  * All instructions for publishing to sonatype can be found in
  * ``z-publishing-artifcats/README.md``.
  */
addCommandAlias("doSnapshotRelease", ";ci;setSnapshotVersion;publishSigned")

/**
  * Use with care.
  *
  * All instructions for publishing to sonatype can be found in
  * ``z-publishing-artifcats/README.md``.
  */
addCommandAlias("doRelease", ";ci;publishSigned;sonatypeRelease")

/**
  * this is a phantom project that is simply supposed to aggregate all modules for convenience,
  * it is NOT published as an artifact. It doesn't have any source files, it is just a convenient
  * way to propagate all commands to the modules via the aggregation
  */
lazy val root = Project(id = "busymachines-commons", base = file("."))
  .settings(PublishingSettings.noPublishSettings)
  .settings(Settings.commonSettings)
  .aggregate(
    core,
    `effects-sync`,
    future,
    effects,
    json,
    `rest-core`,
    `rest-core-testkit`,
    `rest-json`,
    `rest-json-testkit`,
    `semver`,
    `semver-parsers`
  )

lazy val core = project
  .settings(Settings.commonSettings)
  .settings(PublishingSettings.sonatypeSettings)
  .settings(
    name in ThisProject := "busymachines-commons-core",
    libraryDependencies +=
      Dependencies.scalaTest % Test withSources ()
  )

lazy val `effects-sync` = project
  .settings(Settings.commonSettings)
  .settings(PublishingSettings.sonatypeSettings)
  .settings(
    name in ThisProject := "busymachines-commons-effects-sync",
    libraryDependencies ++= Seq(
      Dependencies.scalaTest % Test withSources ()
    )
  )
  .dependsOn(
    core
  )

lazy val future = project
  .settings(Settings.commonSettings)
  .settings(PublishingSettings.sonatypeSettings)
  .settings(
    name in ThisProject := "busymachines-commons-future",
    libraryDependencies ++= Seq(
      Dependencies.scalaTest % Test withSources ()
    )
  )
  .dependsOn(
    core,
    `effects-sync`
  )

lazy val effects = project
  .settings(Settings.commonSettings)
  .settings(PublishingSettings.sonatypeSettings)
  .settings(
    name in ThisProject := "busymachines-commons-effects",
    libraryDependencies ++= Seq(
      Dependencies.catsCore   withSources (),
      Dependencies.catsEffect withSources (),
      Dependencies.monix      withSources (),
      Dependencies.scalaTest  % Test withSources ()
    )
  )
  .dependsOn(
    core,
    `effects-sync`,
    future
  )

lazy val json = project
  .settings(Settings.commonSettings)
  .settings(PublishingSettings.sonatypeSettings)
  .settings(
    name in ThisProject := "busymachines-commons-json",
    libraryDependencies ++=
      Dependencies.circe.map(c => c withSources ()) ++ Seq(
        Dependencies.shapeless withSources (),
        Dependencies.catsCore  withSources (),
        Dependencies.scalaTest % Test withSources ()
      )
  )
  .dependsOn(
    core,
    `effects-sync`
  )

lazy val `rest-core` = project
  .settings(Settings.commonSettings)
  .settings(PublishingSettings.sonatypeSettings)
  .settings(
    name in ThisProject := "busymachines-commons-rest-core",
    libraryDependencies ++= Seq(
      Dependencies.akkaHttp  withSources (),
      Dependencies.akkaActor withSources (),
      /**
        * http://doc.akka.io/docs/akka-http/current/scala/http/introduction.html#using-akka-http
        * {{{
        * Only when running against Akka 2.5 explicitly depend on akka-streams in same version as akka-actor
        * }}}
        */
      Dependencies.akkaStream withSources (),
      //used for building the WebServerIO helpers
      Dependencies.catsEffect withSources ()
    )
  )
  .dependsOn(
    core
  )

lazy val `rest-core-testkit` = project
  .settings(Settings.commonSettings)
  .settings(PublishingSettings.sonatypeSettings)
  .settings(
    name in ThisProject := "busymachines-commons-rest-core-testkit",
    libraryDependencies ++= Seq(
      Dependencies.akkaHttpTestKit withSources (),
      Dependencies.scalaTest       withSources (),
      Dependencies.scalaTest       % Test withSources ()
    )
  )
  .dependsOn(
    core,
    `rest-core`
  )

lazy val `rest-json` = project
  .settings(Settings.commonSettings)
  .settings(PublishingSettings.sonatypeSettings)
  .settings(
    name in ThisProject := "busymachines-commons-rest-json",
    libraryDependencies ++= Seq(
      Dependencies.akkaHttpCirceIntegration withSources ()
    )
  )
  .dependsOn(
    core,
    json,
    `rest-core`
  )

lazy val `rest-json-testkit` = project
  .settings(Settings.commonSettings)
  .settings(PublishingSettings.sonatypeSettings)
  .settings(
    name in ThisProject := "busymachines-commons-rest-json-testkit",
    libraryDependencies ++= Seq(
      Dependencies.scalaTest % Test withSources ()
    )
  )
  .dependsOn(
    core,
    json,
    `rest-core`,
    `rest-json`,
    `rest-core-testkit`
  )

lazy val `semver` = project
  .settings(Settings.commonSettings)
  .settings(PublishingSettings.sonatypeSettings)
  .settings(
    name in ThisProject := "busymachines-commons-semver",
    libraryDependencies ++= Seq(
      Dependencies.scalaTest % Test withSources ()
    )
  )
  .dependsOn()

lazy val `semver-parsers` = project
  .settings(Settings.commonSettings)
  .settings(PublishingSettings.sonatypeSettings)
  .settings(
    name in ThisProject := "busymachines-commons-semver-parsers",
    libraryDependencies ++= Seq(
      Dependencies.attoParser withSources (),
      Dependencies.scalaTest  % Test withSources (),
      Dependencies.scalaCheck % Test withSources ()
    )
  )
  .dependsOn(
    core,
    `effects-sync`,
    `semver`
  )

lazy val docs = project
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(TutPlugin)
  .disablePlugins(ScalafmtPlugin)
  .disablePlugins(ScalafixPlugin)
  .settings(Settings.commonSettings)
  .settings(PublishingSettings.noPublishSettings)
  .settings(micrositeTasksSettings)
  .settings(
    micrositeName             := "busymachines-commmons",
    micrositeDescription      := "Light-weight, modular eco-system of libraries needed to build HTTP web apps in Scala",
    micrositeBaseUrl          := "/busymachines-commons",
    micrositeDocumentationUrl := "/busymachines-commons/docs/",
    micrositeHomepage         := "http://busymachines.github.io/busymachines-commons/",
    micrositeGithubOwner      := "busymachines",
    micrositeGithubRepo       := "busymachines-commons",
    micrositeHighlightTheme   := "atom-one-light",
    //-------------- docs project ------------
    //micrositeImgDirectory := (resourceDirectory in Compile).value / "microsite" / "images",
    //micrositeCssDirectory := (resourceDirectory in Compile).value / "microsite" / "styles"
    //micrositeJsDirectory := (resourceDirectory in Compile).value / "microsite" / "scripts"
    micrositePalette := Map(
      "brand-primary"   -> "#E05236",
      "brand-secondary" -> "#3F3242",
      "brand-tertiary"  -> "#2D232F",
      "gray-dark"       -> "#453E46",
      "gray"            -> "#837F84",
      "gray-light"      -> "#E3E2E3",
      "gray-lighter"    -> "#F4F3F4",
      "white-color"     -> "#FFFFFF"
    ),
    //micrositeFavicons := Seq(
    //  MicrositeFavicon("favicon16x16.png", "16x16"),
    //  MicrositeFavicon("favicon32x32.png", "32x32")
    //),
    micrositeFooterText := Some("""Ⓒ 2018 <a href="https://www.busymachines.com/">BusyMachines</a>"""),
    //------ same as default settings --------
    micrositePushSiteWith      := GHPagesPlugin,
    micrositeGitHostingService := GitHub
  )
  .dependsOn()
