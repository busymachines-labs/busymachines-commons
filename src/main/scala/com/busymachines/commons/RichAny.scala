package com.busymachines.commons

class RichAny[A](val a: A) extends AnyVal {
  def toOption(f : A => Boolean) = 
    if (f(a)) Some(a)
    else None
}