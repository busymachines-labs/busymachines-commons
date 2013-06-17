
import sbt._
import Defaults._

resolvers += "TypeSafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.1.0")

addSbtPlugin("com.github.retronym" % "sbt-onejar" % "0.8")

