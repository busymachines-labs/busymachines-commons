package com.busymachines.commons

trait EnumValue[V <: EnumValue[V]] { 
  def enum: AbstractEnum[V]
  def name: String
  def id: Int 
}

abstract class AbstractEnum[V <: EnumValue[V]] { thisEnum =>
  private val _values = collection.mutable.Set[Value]()
  private var _nextId = 0

  type Value <: V
  
  def values: Set[Value] = _values.toSet
  def apply(id: Int) =  _values.find(_.id == id).getOrElse(throw new Exception(s"Enumeration id $id not defined for enumeration ${getClass.getSimpleName}"))
  def withName(s: String) = _values.find(_.name == s).getOrElse(throw new Exception(s"Enumeration value $s not defined for enumeration ${getClass.getSimpleName}"))
  def withNameOrElse(s: String, v: V) = _values.find(_.name == s).getOrElse(v)

  protected def nextId: Int = { 
    val id = _nextId; 
    _nextId += 1; 
    id 
  }
  protected abstract class Val(val _name: String, val _id: Int = nextId) { this: Value => 
    _values += this
    def enum = thisEnum
    override def toString = name
    override def equals(other: Any) = other match {
      case that: EnumValue[_] => (enum eq that.enum) && id == that.id
      case _ => false
    }
  }
}

trait DefaultEnumValue extends EnumValue[DefaultEnumValue]
abstract class CommonEnum extends AbstractEnum[DefaultEnumValue] {
  case class Value(name: String, id: Int = nextId) extends Val(name, id) with DefaultEnumValue
}

trait MyEnum extends EnumValue[MyEnum]
object MyEnum extends AbstractEnum[MyEnum] {
  case class Value(name: String, id: Int = nextId) extends Val(name, id) with MyEnum
}