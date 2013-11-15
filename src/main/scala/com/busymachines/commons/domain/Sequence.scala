package com.busymachines.commons.domain

case class Sequence(
  id: Id[Sequence],
  value: Long) extends HasId[Sequence]