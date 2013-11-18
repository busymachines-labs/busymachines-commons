package com.busymachines.commons.spray

import spray.routing.PathMatcher
import spray.routing.PathMatcher.PathMatcher1Ops
import spray.routing.PathMatcher.regex2PathMatcher
import com.busymachines.commons.domain.Id
import spray.routing.PathMatchers.Segment

object CommonDirectives extends CommonDirectives {
  val uuidPathMatcher = PathMatcher("""[\da-fA-F]{8}-[\da-fA-F]{4}-[\da-fA-F]{4}-[\da-fA-F]{4}-[\da-fA-F]{12}""".r)
}

trait CommonDirectives {
  def MatchId[A] = Segment.flatMap { string =>
    try Some(Id[A](string))
    catch {
      case _: IllegalArgumentException => None
    }
  }
}