import sbt._
import Keys._

object Dependencies {

  lazy val commonsVersion = "0.2.0-SNAPSHOT"

  //============================================================================================
  //=================================== http://busymachines.com/ ===============================
  //========================================  busymachines =====================================
  //============================================================================================
  lazy val busymachinesCommonsCore: ModuleID = "com.busymachines" %% "busymachines-commons-core" % commonsVersion withSources()

  lazy val busymachinesCommonsJson: ModuleID = "com.busymachines" %% "busymachines-commons-json" % commonsVersion withSources()

  //============================================================================================
  //================================= http://typelevel.org/scala/ ==============================
  //========================================  typelevel ========================================
  //============================================================================================
  lazy val catsVersion = "0.9.0"
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
  //=========================================  testing =========================================
  //============================================================================================
  lazy val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.1"

}
