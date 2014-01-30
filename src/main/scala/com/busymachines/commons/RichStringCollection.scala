package com.busymachines.commons

import scala.collection.generic.CanBuildFrom

class RichStringCollection[C <: Iterable[String]](collection: C)(implicit cbf: CanBuildFrom[C, String, C]) {

  def trim : C  = {
    val builder = cbf()
    builder.sizeHint(collection.size)
    collection.foreach { s =>
      val t = s.trim
      if (t.nonEmpty)
        builder.+=(t)
    }
    builder.result()
  }

  def mkPath : String = {
    collection match {
      case Seq() => ""
      case Seq(s) => s.trim
      case ss => 
        val result = new StringBuilder
        var lastIsSlash = false
        ss.map(_.trim).filterNot(_.isEmpty).foreach { s =>
          result.append(if (lastIsSlash && s.startsWith("/")) s.substring(1) else s)
          lastIsSlash = s.endsWith("/")
        }
        result.toString
    }
  }
}