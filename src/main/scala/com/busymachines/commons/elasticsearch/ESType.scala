package com.busymachines.commons.elasticsearch

case class ESType[A] (
  name : String,
  mapping : ESMapping[A]
)