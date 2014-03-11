package com.busymachines.commons.elasticsearch2

case class ESType[A] (
  name : String,
  mapping : ESMapping[A]
)