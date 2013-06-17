package com.kentivo.mdm.api

import spray.routing.directives.PathMatchers
import scala.util.matching.Regex
import scala.annotation.tailrec
import shapeless._
import spray.routing.directives.PathMatcherImplicits
import com.kentivo.mdm.commons.InvalidInputException

trait CustomMatchers extends PathMatchers with PathMatcherImplicits {
  /**
   * A PathMatcher that matches and extracts an +/- id integer value from url. We cannot use IntMatcher from spray because that one matches only POSITIVE values.
   */
  val IdMatcher = fromRegex("""[+-]?\d*""".r)
    .flatMap {
      case string :: HNil =>
        try Some(java.lang.Integer.parseInt(string) :: HNil)
        catch { case _: NumberFormatException => None }
    }
}