/**
  * Copyright (c) 2017-2018 BusyMachines
  *
  * See company homepage at: https://www.busymachines.com/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
import sbt._

object Dependencies {

  lazy val `scala_2.12`:            String      = "2.12.8"
  lazy val `scala_2.13`:            String      = "2.13.0-M3"
  lazy val mainScalaVersion:        String      = `scala_2.12`
  lazy val seqOfCrossScalaVersions: Seq[String] = Seq(`scala_2.12`)

  //============================================================================================
  //====================================== Scala things ========================================
  //============================================================================================

  //lazy val scalaReflect: ModuleID = "org.scala-lang" % "scala-reflect" % mainScalaVersion

  //============================================================================================
  //================================= http://typelevel.org/scala/ ==============================
  //========================================  typelevel ========================================
  //============================================================================================

  lazy val shapeless: ModuleID = "com.chuusai" %% "shapeless" % "2.3.3"

  lazy val catsVersion: String = "1.6.0"

  lazy val catsCore:    ModuleID = "org.typelevel" %% "cats-core"    % catsVersion
  lazy val catsMacros:  ModuleID = "org.typelevel" %% "cats-macros"  % catsVersion
  lazy val catsKernel:  ModuleID = "org.typelevel" %% "cats-kernel"  % catsVersion
  lazy val catsLaws:    ModuleID = "org.typelevel" %% "cats-laws"    % catsVersion
  lazy val catsTestkit: ModuleID = "org.typelevel" %% "cats-testkit" % catsVersion

  lazy val catsEffect: ModuleID = "org.typelevel" %% "cats-effect" % "1.3.0"

  lazy val circeVersion: String = "0.11.1"

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

  lazy val attoParser: ModuleID = "org.tpolecat" %% "atto-core" % "0.6.5"

  lazy val monix: ModuleID = "io.monix" %% "monix" % "3.0.0-RC2"

  //============================================================================================
  //================================= http://akka.io/docs/ =====================================
  //======================================== akka ==============================================
  //============================================================================================

  lazy val akkaVersion: String = "2.5.23"

  lazy val akkaActor:  ModuleID = "com.typesafe.akka" %% "akka-actor"  % akkaVersion
  lazy val akkaStream: ModuleID = "com.typesafe.akka" %% "akka-stream" % akkaVersion

  lazy val akkaHttpVersion: String   = "10.1.8"
  lazy val akkaHttp:        ModuleID = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion

  /**
    * https://github.com/hseeberger/akka-http-json
    */
  lazy val akkaHttpCirceIntegration: ModuleID = "de.heikoseeberger" %% "akka-http-circe" % "1.23.0"

  //============================================================================================
  //=========================================  testing =========================================
  //============================================================================================

  lazy val scalaTest:  ModuleID = "org.scalatest"  %% "scalatest"  % "3.0.5"
  lazy val scalaCheck: ModuleID = "org.scalacheck" %% "scalacheck" % "1.14.0"

  lazy val akkaTestKit:       ModuleID = "com.typesafe.akka" %% "akka-testkit"        % akkaVersion
  lazy val akkaStreamTestKit: ModuleID = "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion
  lazy val akkaHttpTestKit:   ModuleID = "com.typesafe.akka" %% "akka-http-testkit"   % akkaHttpVersion

}
