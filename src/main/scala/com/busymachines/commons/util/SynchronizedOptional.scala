package com.busymachines.commons.util

/**
 * Synchronized optional.  
 * @tparam A
 */
class SynchronizedOptional[A] {

  private var value : Option[A] = None
  
  def get : Option[A] =
    synchronized {
      value
    }

  def getOrElse(f : => A) : A =
    synchronized {
      value match {
        case Some(value) => value
        case None => f
      }
    }
  
  def getOrElseUpdate(f : => A) : A =
    synchronized {
      value match {
        case Some(value) => value
        case None =>
          val newValue = f
          value = Some(newValue)
          newValue
      }
    }
  
  def set(a : A) : Option[A] =
    synchronized {
      val oldValue = value
      value = Some(a)
      oldValue
    }

  def clear = 
    synchronized {
      value = None
    }
  
}