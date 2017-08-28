import sbt._

object Dependencies {

  lazy val `scala_2.11`: String = "2.11.11"
  lazy val `scala_2.12`: String = "2.12.3"
  lazy val `scala_2.13`: String = "2.13.0-M2"
  lazy val seqOfCrossScalaVersions: Seq[String] = Seq(`scala_2.11`, `scala_2.12`)

  //============================================================================================
  //=================================== http://busymachines.com/ ===============================
  //========================================  busymachines =====================================
  //============================================================================================
  lazy val commonsVersion: String = "0.2.0-SNAPSHOT"

  lazy val busymachinesCommonsCore: ModuleID = "com.busymachines" %% "busymachines-commons-core" % commonsVersion withSources()

  lazy val busymachinesCommonsJson: ModuleID = "com.busymachines" %% "busymachines-commons-json" % commonsVersion withSources()

  //============================================================================================
  //================================= http://typelevel.org/scala/ ==============================
  //========================================  typelevel ========================================
  //============================================================================================

  lazy val catsVersion: String = "0.9.0"
  lazy val shapeless: ModuleID = "com.chuusai" %% "shapeless" % "2.3.2"
  lazy val cats: ModuleID = "org.typelevel" %% "cats-core" % catsVersion

  lazy val circeVersion: String = "0.8.0"

  lazy val circeCore: ModuleID = "io.circe" %% "circe-core" % circeVersion
  lazy val circeGeneric: ModuleID = "io.circe" %% "circe-generic" % circeVersion
  lazy val circeGenericExtras: ModuleID = "io.circe" %% "circe-generic-extras" % circeVersion
  lazy val circeParser: ModuleID = "io.circe" %% "circe-parser" % circeVersion

  lazy val circe: Seq[ModuleID] = Seq(
    circeCore,
    circeGeneric,
    circeGenericExtras,
    circeParser
  )

  //============================================================================================
  //================================= http://akka.io/docs/ =====================================
  //======================================== akka ==============================================
  //============================================================================================

  lazy val akkaVersion: String = "2.5.4"
  lazy val akkaActor: ModuleID = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  lazy val akkaStream: ModuleID = "com.typesafe.akka" %% "akka-stream" % akkaVersion
  lazy val akkaCluster: ModuleID = "com.typesafe.akka" %% "akka-cluster" % akkaVersion
  lazy val akkaClusterSharding: ModuleID = "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion
  lazy val akkaDistributedData: ModuleID = "com.typesafe.akka" %% "akka-distributed-data" % akkaVersion
  lazy val akkaPersistence: ModuleID = "com.typesafe.akka" %% "akka-persistence" % akkaVersion

  lazy val akkaHttpVersion: String = "10.0.9"
  lazy val akkaHttp: ModuleID = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion

  /**
    * https://github.com/hseeberger/akka-http-json
    */
  lazy val akkaHttpCirceIntegration: ModuleID = "de.heikoseeberger" %% "akka-http-circe" % "1.17.0"

  //============================================================================================
  //=========================================  testing =========================================
  //============================================================================================
  lazy val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.3"

  lazy val akkaTestKit: ModuleID = "com.typesafe.akka" %% "akka-testkit" % akkaVersion
  lazy val akkaStreamTestKit: ModuleID = "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion
  lazy val akkaHttpTestKit: ModuleID = "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion

}
