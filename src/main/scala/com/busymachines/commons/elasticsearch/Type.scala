package com.busymachines.commons.elasticsearch

case class Type[A] (
  name : String,
  mapping : Mapping[A]
)