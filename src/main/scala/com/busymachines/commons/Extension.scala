package com.busymachines.commons

import _root_.spray.json.{JsValue, JsField, JsObject, JsonFormat}
import scala.collection.concurrent.TrieMap
import scala.reflect.ClassTag

object Extensions {
  val empty = new Extensions(Map.empty)

  def apply[A, E <: Extension[_, A]](keyValue: (E, A)) = new Extensions(Map(keyValue))

  private[commons] val registeredExtensions = new TrieMap[Extension[_, _], Extension[_, _]]

  private[commons] def read[P : ClassTag](value: JsValue): Extensions =
    new Extensions(registeredExtensions.keys.filter(_.parentClass == scala.reflect.classTag[P]).map { e =>
      e.asInstanceOf[Extension[P, _]] -> e.format.read(value)
    }.toMap)

  private[commons] def write(extensions: Extensions): List[JsField] =
    extensions.map.toList.flatMap {
      case (e, a) =>
      e.format.asInstanceOf[JsonFormat[Any]].write(a.asInstanceOf[Any]) match {
        case JsObject(fields) => fields.toList
        case value => Nil
      }
    }
}

class Extensions(val map: Map[Extension[_, _], _]) {
  def + [A, E <: Extension[_, A]](keyValue: (E, A)) = new Extensions(map + keyValue)
  def apply[A](e: Extension[_, A]) : A = map.getOrElse(e, e.getDefault).asInstanceOf[A]
  def copy[A](e: Extension[_, A], f: A => A) = new Extensions(map + (e -> f(this(e))))
  override def toString = map.toString
}

class Extension[P, A](default: => A)(implicit val parentClass: ClassTag[P], val format: JsonFormat[A]) {

  def register(): Unit =
    Extensions.registeredExtensions += (this -> this)

  override def toString = getClass.getSimpleName

  private[commons] def getDefault = default
}
