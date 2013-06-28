package com.kentivo.mdm.api

import spray.routing.PathMatcher
import spray.routing.PathMatcher.regex2PathMatcher
import shapeless._

trait CustomMatchers {
  /**
   * A PathMatcher that matches and extracts an +/- id integer value from url. We cannot use IntMatcher from spray because that one matches only POSITIVE values.
   */
  
   val IdMatcher = PathMatcher("""[+-]?\d*\.?\d*""".r)
    .flatMap {
      case string :: HNil =>
        try Some(java.lang.Integer.parseInt(string) :: HNil)
        catch { case _: NumberFormatException => None }
    }
}