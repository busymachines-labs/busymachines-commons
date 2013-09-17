
import sbt._
import Defaults._

//resolvers += "TypeSafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

//resolvers += Resolver.url("sbt-plugin-releases", url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.1")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.2.0")

//addSbtPlugin("org.scala-sbt" % "sbt-closure" % "0.1.3")

//addSbtPlugin("com.github.retronym" % "sbt-onejar" % "0.8")

addSbtPlugin("me.lessis" % "less-sbt" % "0.1.10")

//addSbtPlugin("in.drajit.sbt" % "sbt-yui-compressor" % "0.2.0")

