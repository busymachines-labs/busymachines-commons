package com.busymachines.commons.dao.elasticsearch

case class Type[A] (
  name : String,
  mapping : Mapping[A]
)