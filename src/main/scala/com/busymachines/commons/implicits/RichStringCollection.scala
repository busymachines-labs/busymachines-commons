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
    collection.map(_.trim).filterNot(_.isEmpty).foldLeft("") { (prev, s) =>
      if (prev.endsWith("/") && s.startsWith("/")) prev + s.substring(1)
      else if (!prev.isEmpty && !prev.endsWith("/") && !s.startsWith("/")) prev + "/" + s
      else prev + s
    }
  }
}