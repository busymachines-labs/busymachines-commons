import sbt._

object Dependencies {

  lazy val `scala_2.12`:            String      = "2.12.4"
  lazy val `scala_2.13`:            String      = "2.13.0-M2"
  lazy val mainScalaVersion:        String      = `scala_2.12`
  lazy val seqOfCrossScalaVersions: Seq[String] = Seq(`scala_2.12`)

  //============================================================================================
  //====================================== Scala things ========================================
  //============================================================================================

  lazy val scalaReflect: ModuleID = "org.scala-lang" % "scala-reflect" % mainScalaVersion

  //============================================================================================
  //=================================== http://busymachines.com/ ===============================
  //========================================  busymachines =====================================
  //============================================================================================
  lazy val bmcv: String = "0.2.0-RC7"

  lazy val bmcCore:       ModuleID = "com.busymachines" %% "busymachines-commons-core"              % bmcv
  lazy val bmcJson:       ModuleID = "com.busymachines" %% "busymachines-commons-json"              % bmcv
  lazy val bmcRestCore:   ModuleID = "com.busymachines" %% "busymachines-commons-rest-core"         % bmcv
  lazy val bmcRestCoreTK: ModuleID = "com.busymachines" %% "busymachines-commons-rest-core-testkit" % bmcv % Test
  lazy val bmcRestJson:   ModuleID = "com.busymachines" %% "busymachines-commons-rest-json"         % bmcv
  lazy val bmcRestJsonTK: ModuleID = "com.busymachines" %% "busymachines-commons-rest-json-testkit" % bmcv % Test

  lazy val bmcSemVer:        ModuleID = "com.busymachines" %% "busymachines-commons-semver"         % bmcv
  lazy val bmcSemVerParsers: ModuleID = "com.busymachines" %% "busymachines-commons-semver-parsers" % bmcv

  //============================================================================================
  //================================= http://typelevel.org/scala/ ==============================
  //========================================  typelevel ========================================
  //============================================================================================

  lazy val shapeless: ModuleID = "com.chuusai" %% "shapeless" % "2.3.2"

  lazy val catsVersion: String = "1.0.0"

  lazy val catsCore:    ModuleID = "org.typelevel" %% "cats-core"    % catsVersion
  lazy val catsMacros:  ModuleID = "org.typelevel" %% "cats-macros"  % catsVersion
  lazy val catsKernel:  ModuleID = "org.typelevel" %% "cats-kernel"  % catsVersion
  lazy val catsLaws:    ModuleID = "org.typelevel" %% "cats-laws"    % catsVersion
  lazy val catsTestkit: ModuleID = "org.typelevel" %% "cats-testkit" % catsVersion

  lazy val catsEffects: ModuleID = "org.typelevel" %% "cats-effect" % "0.6"

  lazy val circeVersion: String = "0.9.0-M3"

  lazy val circeCore:          ModuleID = "io.circe" %% "circe-core"           % circeVersion
  lazy val circeGeneric:       ModuleID = "io.circe" %% "circe-generic"        % circeVersion
  lazy val circeGenericExtras: ModuleID = "io.circe" %% "circe-generic-extras" % circeVersion
  lazy val circeParser:        ModuleID = "io.circe" %% "circe-parser"         % circeVersion

  lazy val circe: Seq[ModuleID] = Seq(
    circeCore,
    circeGeneric,
    circeGenericExtras,
    circeParser
  )

  lazy val attoParser: ModuleID = "org.tpolecat" %% "atto-core" % "0.6.1-M7"

  //============================================================================================
  //================================= http://akka.io/docs/ =====================================
  //======================================== akka ==============================================
  //============================================================================================

  lazy val akkaVersion: String = "2.5.8"

  lazy val akkaActor:  ModuleID = "com.typesafe.akka" %% "akka-actor"  % akkaVersion
  lazy val akkaStream: ModuleID = "com.typesafe.akka" %% "akka-stream" % akkaVersion

  lazy val akkaHttpVersion: String   = "10.0.11"
  lazy val akkaHttp:        ModuleID = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion

  /**
    * https://github.com/hseeberger/akka-http-json
    */
  lazy val akkaHttpCirceIntegration: ModuleID = "de.heikoseeberger" %% "akka-http-circe" % "1.19.0-M3"
  //FIXME: required only while circe is at version 0.9.0-M2
  lazy val akkaCirceIntegrationResolver: MavenRepository = Resolver.bintrayRepo("hseeberger", "maven")

  lazy val sprayJsonVersion = "1.3.4"

  @scala.deprecated("seriously, migrate to circe, and use the json module", "0.2.0-RC6")
  lazy val sprayJson: ModuleID = "io.spray" %% "spray-json" % sprayJsonVersion

  @scala.deprecated("seriously, migrate to circe, and use the json module", "0.2.0-RC6")
  lazy val akkaHttpSprayJson: ModuleID = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion

  //============================================================================================
  //=========================================  testing =========================================
  //============================================================================================

  lazy val scalaTest:  ModuleID = "org.scalatest"  %% "scalatest"  % "3.0.4"
  lazy val scalaCheck: ModuleID = "org.scalacheck" %% "scalacheck" % "1.13.4"

  lazy val akkaTestKit:       ModuleID = "com.typesafe.akka" %% "akka-testkit"        % akkaVersion
  lazy val akkaStreamTestKit: ModuleID = "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion
  lazy val akkaHttpTestKit:   ModuleID = "com.typesafe.akka" %% "akka-http-testkit"   % akkaHttpVersion

}
