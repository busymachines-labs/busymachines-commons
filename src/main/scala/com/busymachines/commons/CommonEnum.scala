package com.busymachines.commons

import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ArrayBuffer

trait EnumValue[V <: EnumValue[V]] { 
  def enum: Enum[V]
  def name: String
  def id: Int 
}

abstract class Enum[Value <: EnumValue[Value]] { thisEnum =>
  private val _values = new ArrayBuffer[Value]
  private var _nextId = 0

  lazy val values: Set[Value] = _values.toSet
  def apply(id: Int) =  values.find(_.id == id).getOrElse(throw new Exception(s"Enumeration id $id not defined for enumeration ${getClass.getSimpleName}"))
  def withName(s: String) = values.find(_.name == s).getOrElse(throw new Exception(s"Enumeration value $s not defined for enumeration ${getClass.getSimpleName}"))
  def withNameOrElse(s: String, v: Value) = values.find(_.name == s).getOrElse(v)

  protected def nextId: Int = { 
    val id = _nextId; 
    _nextId += 1; 
    id 
  }
  protected abstract class Val { this: Value =>
    _values += this
    def enum = thisEnum
    override def toString = name
    override def equals(other: Any) = other match {
      case that: EnumValue[_] => (enum eq that.enum) && id == that.id
      case _ => false
    }
  }
  
  protected trait NextId { this: Value => 
    val id = nextId
  }
}

trait MyEnum extends EnumValue[MyEnum] {
  def rgb: Int
}
object MyEnum extends Enum[MyEnum] {
  case class Value(name: String, rgb: Int, id: Int = nextId) extends Val with MyEnum 
  val red = Value("red", 0xff0000)
}