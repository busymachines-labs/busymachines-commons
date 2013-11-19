package com.busymachines.commons.spray

import spray.routing.PathMatcher
import spray.routing.PathMatcher._
import com.busymachines.commons.domain.Id
import spray.routing.PathMatchers.Segment
import spray.routing.PathMatchers
import spray.routing.Route
import spray.routing.Directives
import spray.routing.Directive
import shapeless.HNil
import shapeless.HList
import spray.http.Uri.Path

object CommonDirectives extends CommonDirectives {
  val uuidPathMatcher = PathMatcher("""[\da-fA-F]{8}-[\da-fA-F]{4}-[\da-fA-F]{4}-[\da-fA-F]{4}-[\da-fA-F]{12}""".r)
}

trait CommonDirectives extends Directives {
  def MatchId[A] = Segment.flatMap { string =>
    try Some(Id[A](string))
    catch {
      case _: IllegalArgumentException => None
    }
  }

  def noPrefixMatcher(prefixes : String*) = new PathMatcher[HNil] {
    def apply(path: Path) = 
      path match {
      case Path.Segment(segment, tail) if prefixes.contains(segment) => Unmatched
      case _ => Matched.Empty
    }
  }

  def noPathPrefix(prefixes : String*) : Directive[HNil] = 
    pathPrefixTest(noPrefixMatcher(prefixes:_*))

}