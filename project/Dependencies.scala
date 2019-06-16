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
  lazy val `scala_2.13`:            String      = "2.13.0"
  lazy val mainScalaVersion:        String      = `scala_2.12`
  lazy val seqOfCrossScalaVersions: Seq[String] = Seq(`scala_2.12`, `scala_2.13`)

  lazy val scalaCompatVersion:   String = "2.0.0"        //https://github.com/scala/scala-collection-compat/releases
  lazy val shapelessVersion:     String = "2.3.3"        //https://github.com/milessabin/shapeless/releases
  lazy val catsVersion:          String = "2.0.0-M4"     //https://github.com/typelevel/cats/releases
  lazy val catsEffectVersion:    String = "2.0.0-M4"     //https://github.com/typelevel/cats-effect/releases
  lazy val circeVersion:         String = "0.12.0-M3"    //https://github.com/circe/circe/releases
  lazy val attoCoreVersion:      String = "0.6.5"        //https://github.com/tpolecat/atto/releases
  lazy val akkaVersion:          String = "2.5.23"       //https://github.com/akka/akka/releases
  lazy val akkaHttpVersion:      String = "10.1.8"       //https://github.com/akka/akka-http/releases
  lazy val akkaHttpCirceVersion: String = "1.26.0"       //https://github.com/hseeberger/akka-http-json/releases
  lazy val scalaTestVersion:     String = "3.1.0-SNAP13" //https://github.com/scalatest/scalatest/releases
  //============================================================================================
  //====================================== Scala things ========================================
  //============================================================================================

  //lazy val scalaReflect: ModuleID = "org.scala-lang" % "scala-reflect" % mainScalaVersion
  lazy val scalaCompat: ModuleID = "org.scala-lang.modules" %% "scala-collection-compat" % scalaCompatVersion

  //============================================================================================
  //================================= http://typelevel.org/scala/ ==============================
  //========================================  typelevel ========================================
  //============================================================================================

  lazy val shapeless: ModuleID = "com.chuusai" %% "shapeless" % shapelessVersion

  lazy val catsCore:    ModuleID = "org.typelevel" %% "cats-core"    % catsVersion
  lazy val catsMacros:  ModuleID = "org.typelevel" %% "cats-macros"  % catsVersion
  lazy val catsKernel:  ModuleID = "org.typelevel" %% "cats-kernel"  % catsVersion
  lazy val catsLaws:    ModuleID = "org.typelevel" %% "cats-laws"    % catsVersion
  lazy val catsTestkit: ModuleID = "org.typelevel" %% "cats-testkit" % catsVersion

  lazy val catsEffect: ModuleID = "org.typelevel" %% "cats-effect" % catsEffectVersion

  lazy val circeCore:          ModuleID = "io.circe" %% "circe-core"           % circeVersion
  lazy val circeGeneric:       ModuleID = "io.circe" %% "circe-generic"        % circeVersion
  lazy val circeGenericExtras: ModuleID = "io.circe" %% "circe-generic-extras" % circeVersion
  lazy val circeParser:        ModuleID = "io.circe" %% "circe-parser"         % circeVersion

  lazy val circe: Seq[ModuleID] = Seq(
    circeCore,
    circeGeneric,
    circeGenericExtras,
    circeParser,
  )

  lazy val attoParser: ModuleID = "org.tpolecat" %% "atto-core" % attoCoreVersion

  //============================================================================================
  //================================= http://akka.io/docs/ =====================================
  //======================================== akka ==============================================
  //============================================================================================

  lazy val akkaActor:  ModuleID = "com.typesafe.akka" %% "akka-actor"  % akkaVersion
  lazy val akkaStream: ModuleID = "com.typesafe.akka" %% "akka-stream" % akkaVersion

  lazy val akkaHttp: ModuleID = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion

  /**
    * https://github.com/hseeberger/akka-http-json
    */
  lazy val akkaHttpCirceIntegration: ModuleID = "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceVersion

  //============================================================================================
  //=========================================  testing =========================================
  //============================================================================================

  lazy val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % scalaTestVersion

  lazy val akkaTestKit:       ModuleID = "com.typesafe.akka" %% "akka-testkit"        % akkaVersion
  lazy val akkaStreamTestKit: ModuleID = "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion
  lazy val akkaHttpTestKit:   ModuleID = "com.typesafe.akka" %% "akka-http-testkit"   % akkaHttpVersion

}
