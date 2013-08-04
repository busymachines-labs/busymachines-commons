package com.kentivo.mdm.domain

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import scala.concurrent.duration.Deadline
import com.busymachines.commons.domain.HasId
import org.joda.time.DateTime
import com.busymachines.commons.domain.Id
import com.busymachines.commons.domain.Unit

case class Repository(

    id: Id[Repository] = Id.generate,
  name: Map[Locale, String] = Map.empty)

case class Item(
  repository: Id[Repository],
  mutation : Id[Mutation],
  id: Id[Item] = Id.generate,
  owner: Option[Id[Party]] = None,
  name: Map[Locale, String] = Map.empty,
  parents: List[Id[Item]] = Nil,
  knownParentExtent: List[Id[Item]] = Nil,
  properties: List[Property] = Nil,
  values: List[PropertyValue] = Nil,
  isCategory: Boolean = false,
  groups : List[PropertyGroup] = Nil,
  rules: List[ItemRule] = Nil) extends HasId[Item] 

case class Property(
  repository: Id[Repository],
  mutation : Id[Mutation],
  id: Id[Property] = Id.generate,
  name: Map[Locale, String] = Map.empty,
  scope : PropertyScope.Value = PropertyScope.Item,
  `type`: PropertyType.Value = PropertyType.String,
  defaultUnit: Option[Unit] = None,
  itemValueBase : Option[Id[Item]] = None, // only if type == Item
  mandatory: Boolean = false,
  multiValue : Boolean = false,
  multiLingual: Boolean = false,
  groups : List[Id[PropertyGroup]] = Nil,
  rules: List[PropertyRule] = List.empty) extends HasId[Property]

case class PropertyGroup(
  id : Id[PropertyGroup] = Id.generate,
  name : Map[Locale, String] = Map.empty)

object PropertyType extends Enumeration {
  val String = Value("string")
  val Int = Value("int")
  val Real = Value("real")
  val DateTime = Value("datetime")
  val Media = Value("media")
  val Item = Value("item")
  val Property = Value("property")
}

object PropertyScope extends Enumeration {
  val Category = Value("category")
  val Item = Value("item")
  val Instance = Value("instance")
  val Dynamic = Value("dynamic")
}

case class PropertyValue(
  property: Id[Property],
  mutation: Id[Mutation],
  value: String,
  locale: Option[Locale] = None,
  unit : Option[Unit] = None)

case class Mutation(
  description: String,
  date: DateTime,
  id: Id[Mutation] = Id.generate,
  user: Option[Id[User]] = None,
  source: Option[Source] = None) extends HasId[Mutation]


object Item2 {
  val dateTimeFormat = {
    val df = new SimpleDateFormat("dd-MM-yyyy HH:mm")
    df.setTimeZone(TimeZone.getTimeZone("UTC"))
    df
  }
  def parseValue(value: String, `type`: PropertyType.Value): Option[Any] = {
    `type` match {
      case PropertyType.String => Some(value)
      case PropertyType.Int => try {
        Some(java.lang.Long.parseLong(value))
      } catch {
        case _: java.lang.NumberFormatException => None
      }
      case PropertyType.Real => try {
        Some(java.lang.Double.parseDouble(value))
      } catch {
        case _: java.lang.NumberFormatException => None
      }
      case PropertyType.DateTime => try {
        Some(dateTimeFormat.parse(value))
      } catch {
        case _: Throwable => None
      }
      case _ => None
    }
  }
  def toString(value: Any, `type`: PropertyType.Value): String =
    `type` match {
      case PropertyType.DateTime =>
        if (value.isInstanceOf[Deadline]) {
          dateTimeFormat.format(value.asInstanceOf[Deadline].time.length)
        } else {
          value.toString
        }
      case _ => value.toString
    }

  def convertValue(value: Any, fromType: PropertyType.Value, toType: PropertyType.Value): Option[Any] =
    parseValue(toString(value, fromType), toType)
}
