import sbt._

object Dependencies {

  //FIXME: need to wait for sbt 1.0.3 to have a bug fixed before we can use 2.12.4 and IntelliJ
  lazy val `scala_2.12`: String = "2.12.3"
  lazy val `scala_2.13`: String = "2.13.0-M2"
  lazy val seqOfCrossScalaVersions: Seq[String] = Seq(`scala_2.12`)

  //============================================================================================
  //=================================== http://busymachines.com/ ===============================
  //========================================  busymachines =====================================
  //============================================================================================
  lazy val bmCommonsVersion: String = "0.2.0-RC4"

  lazy val busymachinesCommonsCore: ModuleID = "com.busymachines" %% "busymachines-commons-core" % bmCommonsVersion withSources()
  lazy val busymachinesCommonsJson: ModuleID = "com.busymachines" %% "busymachines-commons-json" % bmCommonsVersion withSources()
  lazy val busymachinesCommonsRestCore: ModuleID = "com.busymachines" %% "busymachines-commons-rest-core" % bmCommonsVersion withSources()
  lazy val busymachinesCommonsRestCoreTestkit: ModuleID = "com.busymachines" %% "busymachines-commons-rest-core-testkit" % bmCommonsVersion % Test withSources()
  lazy val busymachinesCommonsRestJson: ModuleID = "com.busymachines" %% "busymachines-commons-rest-json" % bmCommonsVersion withSources()
  lazy val busymachinesCommonsRestJsonTestkit: ModuleID = "com.busymachines" %% "busymachines-commons-rest-Json-testkit" % bmCommonsVersion % Test withSources()


  //============================================================================================
  //================================= http://typelevel.org/scala/ ==============================
  //========================================  typelevel ========================================
  //============================================================================================

  lazy val shapeless: ModuleID = "com.chuusai" %% "shapeless" % "2.3.2"

  lazy val catsVersion: String = "1.0.0-MF"
  lazy val catsCore: ModuleID = "org.typelevel" %% "cats-core" % catsVersion
  lazy val catsMacros: ModuleID = "org.typelevel" %% "cats-macros" % catsVersion
  lazy val catsKernel: ModuleID = "org.typelevel" %% "cats-kernel" % catsVersion
  lazy val catsLaws: ModuleID = "org.typelevel" %% "cats-laws" % catsVersion
  lazy val catsTestkit: ModuleID = "org.typelevel" %% "cats-testkit" % catsVersion

  lazy val circeVersion: String = "0.9.0-M1"
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

  lazy val akkaHttpVersion: String = "10.0.10"
  lazy val akkaHttp: ModuleID = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion

  /**
    * https://github.com/hseeberger/akka-http-json
    */
  lazy val akkaHttpCirceIntegration: ModuleID = "de.heikoseeberger" %% "akka-http-circe" % "1.19.0-M1"
  //required only while circe is at version 0.9.0-M1
  lazy val akkaCirceIntegrationResolver: MavenRepository = Resolver.bintrayRepo("hseeberger", "maven")


  //============================================================================================
  //=========================================  testing =========================================
  //============================================================================================
  lazy val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.4"

  lazy val akkaTestKit: ModuleID = "com.typesafe.akka" %% "akka-testkit" % akkaVersion
  lazy val akkaStreamTestKit: ModuleID = "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion
  lazy val akkaHttpTestKit: ModuleID = "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion

}
