package com.kentivo.mdm.domain

import com.busymachines.commons.domain.Id
import com.busymachines.commons.domain.HasId

/**
 * Constraint: Source properties must exist in source types.
 * Constraint: Target properties must exist in either repository items or source types.
 */
case class Source(
  id: Id[Source] = Id.generate,
  name : String,
  repository: Id[Repository],
  model: List[Item] = List.empty,
  mappings: List[Mapping] = List.empty,
  importSchedule : List[Schedule] = Nil,
  exportSchedule : List[Schedule] = Nil) extends HasId[Source]

case class Mapping(
  id: Id[Mapping] = Id.generate,
  matchingFactor: Option[Double],
  default: Option[DefaultMapping] = None,
  validator: Option[Validator] = None,
  extractStreetHouseNumber: Option[ExtractStreetHouseNumber] = None)

/**
 * Bidirectional: yes
 * Matching: yes
 */
case class DefaultMapping(
  source: Id[Property],
  target: Id[Property],
  matcher: Option[Matcher] = None
)
  
/**
 * Bidirectional: no
 * Matching: no
 */
case class Validator(
    source: Id[Property]
)

/**
 * Bidirectional: yes
 * Matching: yes
 * Constraint: targetStreet must be of string-like type
 */
case class ExtractStreetHouseNumber(
  source: Id[Property],
  targetStreet: Id[Property],
  targetHouseNumber: Id[Property],
  streetMatcher: Matcher = Matcher(streetMatcher = Some(StreetMatcher())),
  houseNumberMatcher: Matcher = Matcher(houseNumberMatcher = Some(HouseNumberMatcher())))

case class Matcher(
  streetMatcher: Option[StreetMatcher] = None,
  houseNumberMatcher: Option[HouseNumberMatcher] = None)


case class StreetMatcher(dummy : Option[Unit] = None)
case class HouseNumberMatcher(dummy : Option[Unit] = None)

/**
 * Remarks: Source and target will be coerced into Real type.
 */
case class DeltaMatcher(
  source: Id[Property],
  target: Id[Property],
  delta: Double)

case class SourceValidation(
  source: Id[Source],
  item: Option[Id[Item]],
  property: Option[Id[Property]],
  mapping: Option[Id[Mapping]],
  message: String)