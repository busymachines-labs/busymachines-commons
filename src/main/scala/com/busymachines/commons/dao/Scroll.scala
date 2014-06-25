package com.busymachines.commons.dao

import com.busymachines.commons.domain.{HasId, Id}

import scala.concurrent.duration.{FiniteDuration}

import scala.concurrent.duration.DurationInt

/**
 * Created by alex on 25.06.2014.
 */
case class Scroll (id:String,duration:FiniteDuration=5 minutes, size:Int=100)

