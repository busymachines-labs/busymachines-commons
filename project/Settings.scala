import sbt._
import Keys._

object Settings {
  lazy val `scala_2.11`: String = "2.11.11"
  lazy val `scala_2.12`: String = "2.12.3"
  lazy val `scala_2.13`: String = "2.13.0-M2"
  lazy val seqOfCrossScalaVersions: Seq[String] = Seq(`scala_2.11`, `scala_2.12`)

  lazy val organizationName: String = "com.busymachines"

  lazy val bmCommonsHomepage: String = "https://github.com/busymachines/busymachines-commons"

  def commonSettings: Seq[Setting[_]] =
    Seq(
      organization in ThisBuild := organizationName,
      homepage := Some(url(bmCommonsHomepage)),
      scalaVersion in ThisBuild := `scala_2.12`,
      crossScalaVersions in ThisBuild := seqOfCrossScalaVersions
    ) ++
      scalaCompilerSettings

  def scalaCompilerSettings: Seq[Setting[_]] = Nil
}
