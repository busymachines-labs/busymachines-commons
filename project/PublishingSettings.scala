import sbt._
import Keys._
import com.typesafe.sbt.SbtPgp.autoImportImpl._
import xerial.sbt.Sonatype.SonatypeKeys._

/**
  * All instructions for publishing to sonatype can be found on the sbt-plugin page:
  * http://www.scala-sbt.org/release/docs/Using-Sonatype.html
  *
  * and some here:
  *
  * https://github.com/xerial/sbt-sonatype
  *
  * VERY IMPORTANT!!! You need to add credentials as described here:
  * http://www.scala-sbt.org/release/docs/Using-Sonatype.html#Second+-+Configure+Sonatype+integration
  *
  * The username and password are the same as those to the Sonatype JIRA account.
  * https://issues.sonatype.org/browse/OSSRH-33718
  *
  * And then you need to ensure PGP keys to sign the maven artifacts:
  * http://www.scala-sbt.org/release/docs/Using-Sonatype.html#PGP+Tips%E2%80%99n%E2%80%99tricks
  * Which means that you need to have PGP installed
  *
  * To avoid pushing sensitive information to github you must put all PGP related things in your local configs:
  * as described here:
  *
  * https://github.com/sbt/sbt-pgp/issues/69
  */
object PublishingSettings {

  def sonatypeSettings: Seq[Setting[_]] = Seq(
    useGpg                     := true,
    sonatypeProfileName        := Settings.organizationName,
    publishArtifact in Compile := true,
    publishArtifact in Test    := false,
    publishMavenStyle          := true,
    pomIncludeRepository       := (_ => false),
    publishTo := Option {
      if (isSnapshot.value)
        Opts.resolver.sonatypeSnapshots
      else
        Opts.resolver.sonatypeStaging
    },
    licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    scmInfo := Option(
      ScmInfo(
        url("https://github.com/busymachines/busymachines-commons"),
        "scm:git@github.com:busymachines/busymachines-commons.git"
      )
    ),
    developers := List(
      Developer(
        id    = "lorandbm",
        name  = "Lorand Szakacs",
        email = "lorand.szakacs@busymachines.com",
        url   = url("https://github.com/lorandszakacs")
      )
    )
  )

  def noPublishSettings = Seq(
    publish              := {},
    publishLocal         := {},
    skip in publishLocal := true,
    skip in publish      := true,
    publishArtifact      := false
  )

}
