import sbt._
import Keys._

object Settings {
  lazy val organizationName: String = "com.busymachines"

  lazy val bmCommonsHomepage: String = "https://github.com/busymachines/busymachines-commons"

  def commonSettings: Seq[Setting[_]] =
    Seq(
      organization in ThisBuild := organizationName,
      homepage                  := Some(url(bmCommonsHomepage)),
      scalaVersion              := Dependencies.mainScalaVersion,
      //      crossScalaVersions := Dependencies.seqOfCrossScalaVersions,

      /**
        * akka http is binary compatible both with 2.5.4 (which we have), but it is openly dependent on 2.4.19
        * that's why we can safely force our version in order to avoid those super annoying eviction warnings
        */
      dependencyOverrides += Dependencies.akkaStream,
      dependencyOverrides += Dependencies.akkaActor,
    ) ++ scalaCompilerSettings

  def scalaCompilerSettings: Seq[Setting[_]] = Seq(
    scalacOptions ++= Seq(
      "-Ywarn-unused-import",
      "-Ypartial-unification",
      "-deprecation"
    )
  )
}
