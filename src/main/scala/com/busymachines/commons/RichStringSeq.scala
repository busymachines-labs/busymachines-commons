package com.busymachines.commons

class RichStringSeq(val seq: Seq[String]) extends AnyVal {

  def mkPath : String = {
    seq match {
      case Seq() => ""
      case Seq(s) => s.trim
      case ss => 
        val result = new StringBuilder
        var lastIsSlash = false
        seq.map(_.trim).filterNot(_.isEmpty).foreach { s =>
          result.append(if (lastIsSlash && s.startsWith("/")) s.substring(1) else s)
          lastIsSlash = s.endsWith("/")
        }
        result.toString
    }
  }
}