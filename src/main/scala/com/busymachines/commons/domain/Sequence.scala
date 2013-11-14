package com.busymachines.commons.domain

case class Sequence(id: Id[Sequence],
  name: String,
  value: Long) extends HasId[Sequence]